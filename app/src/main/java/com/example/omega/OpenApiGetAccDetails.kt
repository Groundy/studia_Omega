package com.example.omega

import android.app.Activity
import android.util.Log
import com.example.omega.ApiConsts.ApiReqFields.*
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class OpenApiGetAccDetails(private var token: Token, private val callerActivity: Activity) {
	private var accountToSet: ArrayList<PaymentAccount> = ArrayList()
	private var limitExceeded = false

	suspend fun run(accNumbers: List<String>): Boolean {
		Log.i(TagProduction, "getPaymentAccountDetails started")
		if (accNumbers.isEmpty()){
			Log.i(TagProduction, "getPaymentAccountDetails failed")
			return false
		}

		val success = getAccDetailsForAccountsInSeparteThreads(accNumbers)
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
	private suspend fun sendSingleRequest(request: Request): Boolean {
		return try {
			val response = OkHttpClient().newCall(request).execute()
			if (response.code != ApiConsts.ResponseCodes.OK.code) {
				ApiFunctions.logResponseError(response, this.javaClass.name)
				if(response.code == ApiConsts.ResponseCodes.LimitExceeded.code)
					limitExceeded = true
				 return false
			}
			val responseBodyJson = JSONObject(response.body?.string()!!)
			val successParsing = parseResponseJson(responseBodyJson)
			successParsing
		}catch (e : Exception){
			Log.e(TagProduction, e.toString())
			false
		}
	}
	private fun parseResponseJson(obj: JSONObject): Boolean {
		try {
			val tmpPaymentAcc = PaymentAccount(obj)
			if(!tmpPaymentAcc.isValid())
				return false

			this.accountToSet.add(tmpPaymentAcc)
			return true
		}catch (e : Exception){
			//todo
			return false
		}
	}
	private suspend fun getAccDetailsForAccountsInSeparteThreads(accNumbers: List<String>): Boolean {
		return try {
			val boolsOfThreadsSuccessfullness = ArrayList<Boolean>()
			for (i in accNumbers.indices) {
				val request = getRequestForSignleAcc(accNumbers[i])
				val success = sendSingleRequest(request)
				boolsOfThreadsSuccessfullness.add(success)
			}
			return !boolsOfThreadsSuccessfullness.contains(false)
		} catch (e: Exception) {
			val msg = "Failed to obtain information for at account with numbers[$accNumbers] [$e]"
			Log.e(TagProduction,msg)
			false
		}
	}


	//Those func need ais-account scope
	/* fun runForAllAccounts(): Boolean {
		Log.i(TagProduction, "getPaymentAccountDetails started")

		val request = getRequestForAllAccounts()
		val response = sendAllAccRequest(request)
		if(response == null){
			Log.e(TagProduction, "getPaymentAccountDetails with failure")
			return false
		}

		token.updateListOfAccountWithDetails(accountToSet)
		Log.e(TagProduction, "getPaymentAccountDetails with success")
		return true
	} */
	/*private fun getRequestForAllAccounts(): Request {
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

		val additionalHeaderList = arrayListOf(Pair(Authorization.text, authFieldValue))
		return ApiFunctions.bodyToRequest( ApiConsts.BankUrls.GetPaymentAccounts, requestBodyJson, uuidStr, additionalHeaderList)
	}*/
	/*private fun sendAllAccRequest(request : Request) : JSONObject?{
		try {
			val response = OkHttpClient().newCall(request).execute()
			if (response.code != ApiConsts.ResponseCodes.OK.code) {
				if (response.code == ApiConsts.ResponseCodes.LimitExceeded.code) {
					limitExceeded = true
				}
				ApiFunctions.logResponseError(response, this.javaClass.name)
				return null
			}
			val responseBody = response.body?.string()
			return JSONObject(responseBody!!)
		}catch (e : Exception){
			Log.e(TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			return null
		}
	}
	*/
}
