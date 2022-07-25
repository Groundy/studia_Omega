package com.example.omega

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception


class ApiGetPaymentAccDetails {
	lateinit var token: Token
	constructor(token: Token) {
		this.token = token
	}
	private var accountToSet : ArrayList<PaymentAccount> = ArrayList()

	fun run(accNumbers: List<String>): Boolean {
		if(accNumbers.isNullOrEmpty())
			return false

		val success = getAccDetailsForAllAccounts(accNumbers)
		return if(success){
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
			.put(
				"requestHeader", JSONObject()
					.put("requestId", uuidStr)
					.put("userAgent", ApiFunctions.getUserAgent())
					.put("ipAddress", ApiFunctions.getPublicIPByInternetService())
					.put("sendDate", currentTimeStr)
					.put("tppId", ApiConsts.TTP_ID)
					.put("token", authFieldValue)
					.put("isDirectPsu", false)
					.put("directPsu", false)
			)
			.put("accountNumber", accNumber)

		val additionalHeaderList = arrayListOf(Pair("AUTHORIZATION", authFieldValue))
		return ApiFunctions.bodyToRequest(
			ApiConsts.BankUrls.GetPaymentAccount.text,
			requestBodyJson,
			uuidStr,
			additionalHeaderList
		)
	}
	private fun getAccInfo(accNumber: String): Boolean {
		Log.i(Utilities.TagProduction, "Started checking bank proccedure for details for acc $accNumber")

		val request = getPaymentAccDetailsRequest(accNumber)
		val response = OkHttpClient().newCall(request).execute()
		val responseCodeOk = response.code == 200
		if (!responseCodeOk) {
			Log.e(Utilities.TagProduction,"[getAccInfo/${this.javaClass.name}] returned code ${response.code} for accInfo $accNumber")
			return false
		}
		return try {
			val responseBodyJson = JSONObject(response.body?.string()!!)
			parseResponseJson(responseBodyJson)
		} catch (e: Exception) {
			Log.e(Utilities.TagProduction, e.toString())
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
			Log.i(Utilities.TagProduction, "started checking")//todo tmp
			for (i in 0 until listOfThreadCheckingAccInfo.size)
				listOfThreadCheckingAccInfo[i].start()
			for (i in 0 until listOfThreadCheckingAccInfo.size)
				listOfThreadCheckingAccInfo[i].join(ApiConsts.requestTimeOut)
			Log.i(Utilities.TagProduction, "ended checking")//todo tmp
			return !boolsOfThreadsSuccessfullness.contains(false)
		} catch (e: Exception) {
			Log.e(
				Utilities.TagProduction,
				"Failed to obtain information for at account with numbers[$accNumbers] [$e]"
			)
			false
		}
	}
}
