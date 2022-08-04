package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.Request
import org.json.JSONObject
import com.example.omega.ApiConsts.ApiReqFields.*
import okhttp3.OkHttpClient

class OpenApiDomesticPayment(activity: Activity, token: Token) {
	private val callerActivity = activity
	private val token = token

	fun run() : Boolean{
		Log.i(Utilities.TagProduction, "Domestic Payement started")
		var success = false
		val thread = Thread{
			val request = getRequest()
			success = sendRequest(request) ?: return@Thread
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(success)
			Log.i(Utilities.TagProduction, "Domestic Payement ended with sucess")
		else
			Log.e(Utilities.TagProduction, "Domestic Payement ended with error")
		return success
	}
	private fun getRequest() : Request{
		val uuidStr = ApiFunctions.getUUID()
		val currentTime = OmegaTime.getCurrentTime()
		val authFieldValue = token.getAuthFieldValue()

		val requestHeaders = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTime)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, authFieldValue)


		val transferDataFromToken = TransferData(token)
		val requestBodyJsonObj = DomesticPaymentSupportClass(transferDataFromToken).getBodyForTokenRequest(requestHeaders)
		val additionalHeaderList = arrayListOf(Pair(Authorization.text, authFieldValue))
		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.SinglePayment, requestBodyJsonObj, uuidStr, additionalHeaderList)
	}
	private fun sendRequest(request : Request) : Boolean{
		return try{
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.LogResponseError(response, this.javaClass.name)
				return false
			}
 			//val responseBody = response.body?.string()
			//val responseJsonObject = JSONObject(responseBody!!)
			true
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			false
		}
	}
}