package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenApiGetPayment{
	/*
	suspend fun run(callerActivity : Activity, token: Token) : Boolean{
		Log.i(Utilities.TagProduction, "get payment status started")
		val request = getRequest(callerActivity, token)
		val okResponse = sendRequest(request)

		if(okResponse == null){
			Log.e(Utilities.TagProduction, "get payment status ended with error")
			return false
		}

		val success = handleOkResponse(okResponse)
		if(!success){
			Log.e(Utilities.TagProduction, "get payment status ended with error")
			return false
		}


		Log.i(Utilities.TagProduction, "get payment status ended with sucess")
		return true
	}

	private fun getRequest(callerActivity: Activity, token : Token) : Request{
		val uuid = ApiFunctions.getUUID()
		val authFieldValue = token.getAuthFieldValue()

		val bodyHeaders = JSONObject()
		with(bodyHeaders){
			put(ApiConsts.ApiReqFields.RequestId.text, uuid)
			put(ApiConsts.ApiReqFields.UserAgent.text, ApiFunctions.getUserAgent())
			put(ApiConsts.ApiReqFields.IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			put(ApiConsts.ApiReqFields.SendDate.text, OmegaTime.getCurrentTime())
			put(ApiConsts.ApiReqFields.TppId.text, ApiConsts.TTP_ID)
			put(ApiConsts.ApiReqFields.TokenField.text, authFieldValue)
		}

		val body = JSONObject()
		with(body){
			put(ApiConsts.ApiReqFields.RequestHeader.text,bodyHeaders)
			put("paymentId","5319233007255552")
			put("tppTransactionId","5236361476964352")
		}

		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.getPaymentStstus, body, uuid, authFieldValue)
	}
	fun sendRequest(request: Request) : JSONObject?{
		return try {
			val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
			val response = client.newCall(request).execute()
			if(response.code!= ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.logResponseError(response, this.javaClass.name)
				return null
			}
			val responseBody = response.body?.string()
			val responseJsonObject = JSONObject(responseBody!!)
			responseJsonObject
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			null
		}
	}
	fun handleOkResponse(response : JSONObject) : Boolean{
		return false
	}
	 */
}