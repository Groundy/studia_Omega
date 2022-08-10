package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*

class OpenApiGetAccDetails(private var token: Token, private val callerActivity: Activity) {
	private var accountToSet: ArrayList<PaymentAccount> = ArrayList()
	private var limitExceeded = false

	fun run(accNumbers: List<String>): Boolean {
		Log.i(TagProduction, "getPaymentAccountDetails started")
		if (accNumbers.isEmpty()){
			Log.i(TagProduction, "getPaymentAccountDetails failed")
			return false
		}

		val success = getAccDetailsForAllAccounts(accNumbers)
		if(!success) {
			if(limitExceeded){
				val msg = callerActivity.resources.getString(R.string.AccHistoryAct_UserMsg_TooManyResuest)
				Utilities.showToast(callerActivity, msg)
			}
			Log.i(TagProduction, "getPaymentAccountDetails failed")
			return false
		}

		token.updateListOfAccountWithDetails(accountToSet)
		Log.i(TagProduction, "getPaymentAccountDetails sucessfully")
		return true
	}
	fun runForAllAccounts(): Boolean {
		Log.i(TagProduction, "getPaymentAccountDetails started")
		val accNumbers = token.getListOfAccountsNumbers()
		if (accNumbers.isNullOrEmpty()){
			Log.i(TagProduction, "getPaymentAccountDetails failed")
			return false
		}

		val success = getAccDetailsForAllAccounts(accNumbers)
		if(!success){
			Log.i(TagProduction, "getPaymentAccountDetails failed")
			return false
		}

		Log.i(TagProduction, "getPaymentAccountDetails sucessfully")
		token.updateListOfAccountWithDetails(accountToSet)
		return true
	}

	private fun getRequestForSignleAcc(accNumber: String): Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val authFieldValue = token.getAuthFieldValue()

		val requestJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTimeStr)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, authFieldValue)
			.put(IsDirectPsu.text, false)
			.put(DirectPsu.text, false)

		val requestBodyJson = JSONObject()
			.put(RequestHeader.text, requestJson)
			.put(AccountNumberField.text, accNumber)

		val additionalHeaderList = arrayListOf(Pair(Authorization.text, authFieldValue))
		return ApiFunctions.bodyToRequest( ApiConsts.BankUrls.GetPaymentAccount, requestBodyJson, uuidStr, additionalHeaderList)
	}
	private fun sendSingleRequest(accNumber: String): Boolean {
		val request = getRequestForSignleAcc(accNumber)
		val response = OkHttpClient().newCall(request).execute()
		if (response.code != ApiConsts.ResponseCodes.OK.code) {
			ApiFunctions.logResponseError(response, this.javaClass.name)
			if(response.code == ApiConsts.ResponseCodes.LimitExceeded.code)
				limitExceeded = true
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
		if(!tmpPaymentAcc.isValid())
			return false

		this.accountToSet.add(tmpPaymentAcc)
		return true
	}
	private fun getAccDetailsForAllAccounts(accNumbers: List<String>): Boolean {
		return try {
			val listOfThreadCheckingAccInfo = arrayListOf<Thread>()
			val boolsOfThreadsSuccessfullness = ArrayList<Boolean>()
			for (i in accNumbers.indices) {
				val thread = Thread {
					val success = sendSingleRequest(accNumbers[i])
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
			val msg = "Failed to obtain information for at account with numbers[$accNumbers] [$e]"
			Log.e(TagProduction,msg)
			false
		}
	}

}
