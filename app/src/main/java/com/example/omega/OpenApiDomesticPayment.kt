package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.Request
import org.json.JSONObject
import com.example.omega.ApiConsts.ApiReqFields.*
import okhttp3.OkHttpClient

class OpenApiDomesticPayment(private val callerActivity: Activity, val token: Token) {
	suspend fun run() : Boolean{
		Log.i(Utilities.TagProduction, "Domestic Payement started")
		val request = getRequest()
		val success = sendRequest(request)
		if(success)
			Log.i(Utilities.TagProduction, "[run/${this.javaClass.name}] Domestic Payement ended with sucess")
		else
			Log.e(Utilities.TagProduction, "[run/${this.javaClass.name}] Domestic Payement ended with error")
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
	private suspend fun sendRequest(request : Request) : Boolean{
		return try{
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.logResponseError(response, this.javaClass.name)
				return false
			}
			true
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			false
		}
	}
}