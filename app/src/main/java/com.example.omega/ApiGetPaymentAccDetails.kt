package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*

class ApiGetPaymentAccDetails(var token: Token, activity: Activity) {
	private var accountToSet: ArrayList<PaymentAccount> = ArrayList()
	private var callerActivity = activity
	fun run(accNumbers: List<String>): Boolean {
		Log.i(TagProduction, "getPaymentAccountDetails started")
		if (accNumbers.isNullOrEmpty())
			return false

		val success = getAccDetailsForAllAccounts(accNumbers)
		return if (success) {
			token.updateListOfAccountWithDetails(accountToSet)
			true
		}
		else
			false
	}

	private fun getPaymentAccDetailsRequest(accNumber: String): Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val authFieldValue = token.getAuthFieldValue()
		val requestBodyJson = JSONObject()
			.put(RequestHeader.text, JSONObject()
				.put(RequestId.text, uuidStr)
				.put(UserAgent.text, ApiFunctions.getUserAgent())
				.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
				.put(SendDate.text, currentTimeStr)
				.put(TppId.text, ApiConsts.TTP_ID)
				.put(TokenField.text, authFieldValue)
				.put(IsDirectPsu.text, false)
				.put(DirectPsu.text, false)
			).put(AccountNumberField.text, accNumber)

		val additionalHeaderList = arrayListOf(
			Pair(Authorization.text, authFieldValue))
		return ApiFunctions.bodyToRequest(
			ApiConsts.BankUrls.GetPaymentAccount.text,
			requestBodyJson,
			uuidStr,
			additionalHeaderList
		)
	}
	private fun getAccInfo(accNumber: String): Boolean {
		Log.i(
			TagProduction,
			"Started checking bank proccedure for details for acc $accNumber"
		)

		val request = getPaymentAccDetailsRequest(accNumber)
		val response = OkHttpClient().newCall(request).execute()
		val responseCodeOk = response.code == 200
		if (!responseCodeOk) {
			val logTxt = "[getAccInfo/${this.javaClass.name}] returned code ${response.code} for accInfo $accNumber"
			Log.e(TagProduction,logTxt)
			return false
		}
		return try {
			val responseBodyJson = JSONObject(response.body?.string()!!)
			parseResponseJson(responseBodyJson)
		} catch (e: Exception) {
			Log.e(TagProduction, e.toString())
			false
		}
	}
	private fun parseResponseJson(obj: JSONObject): Boolean {
		val tmpPaymentAcc = PaymentAccount(obj)
		return if (tmpPaymentAcc.isValid()) {
			this.accountToSet.add(tmpPaymentAcc)
			true
		} else
			false
	}
	private fun getAccDetailsForAllAccounts(accNumbers: List<String>): Boolean {
		//starts many threads, each of them ask Bank for details of specific accNumbe
		return try {
			val listOfThreadCheckingAccInfo = arrayListOf<Thread>()
			val boolsOfThreadsSuccessfullness = ArrayList<Boolean>()
			for (i in accNumbers.indices) {
				val thread = Thread {
					val success = getAccInfo(accNumbers[i])
					boolsOfThreadsSuccessfullness.add(success)
				}
				listOfThreadCheckingAccInfo.add(thread)
			}
			for (i in 0 until listOfThreadCheckingAccInfo.size)
				listOfThreadCheckingAccInfo[i].start()
			for (i in 0 until listOfThreadCheckingAccInfo.size)
				listOfThreadCheckingAccInfo[i].join(ApiConsts.requestTimeOut)
			return !boolsOfThreadsSuccessfullness.contains(false)
		} catch (e: Exception) {
			Log.e(
				TagProduction,
				"Failed to obtain information for at account with numbers[$accNumbers] [$e]"

			)
			false
		}
	}
}
