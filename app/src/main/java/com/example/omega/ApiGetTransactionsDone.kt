package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ScopeFields.*
import com.example.omega.ApiConsts.ApiReqFields.*

class ApiGetTransactionsDone(activity: Activity, token: Token) {
	private val token = token
	private val callerActivity = activity

	fun run(accNumber: String? = null): Boolean {
		var isSuccess = false
		val thread = Thread{
			try {
				isSuccess = sendRequest(accNumber!!)
			}catch (e: Exception) {
				Log.e(TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		return isSuccess
	}

	private fun getPaymentAccHistoryRequest(accNumber: String) : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val headersJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTimeStr)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, token.getAccessToken())
			.put(IsDirectPsu.text,false)
			//.put("callbackURL",ApiConsts.REDIRECT_URI)//??
			//.put("apiKey", ApiConsts.appSecret_ALIOR)//??

		val requestBodyJson = JSONObject()
				.put(RequestHeader.text, headersJson)
				.put(AccountNumberField.text, accNumber)
				//.put("itemIdFrom","5989073072160768")//??
				//.put("transactionDateFrom","Thu Apr 30")//??
				//.put("transactionDateTo","Thu Feb 06")//??
				//.put("bookingDateFrom","Thu Feb 03")//??
				//.put("bookingDateTo","Mon Feb 03")//??
				//.put("minAmount","0.0")//??
				//.put("maxAmount","99999.99")//??
				//.put("pageId","1")//??
				//.put("perPage",10)//??
				.put("type","DEBIT")//??

			val additionalHeaderList = arrayListOf(Pair(Authorization.text,token.getAuthFieldValue()))
			return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTransactionsDone.text, requestBodyJson, uuidStr, additionalHeaderList)
		}
	private fun sendRequest(accNumber: String) : Boolean{
		return try {
			val request = getPaymentAccHistoryRequest(accNumber)
			val response = OkHttpClient().newCall(request).execute()
			val responseCode = response.code
			if(responseCode != ApiConsts.responseOkCode){
				val logMsg = "[getAccHistory/${this.javaClass.name}] getHistory return code error $${ApiFunctions.getErrorTextOfRequestToLog(responseCode)}"
				Log.e(TagProduction, logMsg)
				return false
			}

			val responseBodyJson = JSONObject(response.body?.string()!!)
			parseResponseJson(responseBodyJson)
		}
		catch (e : Exception){
			val logMsg = "[getAccHistory/${this.javaClass.name}] wrong json struct \ne=$e"
			Log.e(TagProduction, logMsg)
			false
		}
	}
	private fun parseResponseJson(obj : JSONObject) : Boolean{
		//todo implement
		return false
	}
}
