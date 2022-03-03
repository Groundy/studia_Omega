package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception

class API_getPaymentAccDetails {
	private lateinit var activity : Activity
	private lateinit var accNumber: String

	enum class responseFields{
		accounts, accountNumber, accountType, accountTypeName
	}
	constructor(activity: Activity, accNumber : String){
		this.activity = activity
		this.accNumber = accNumber
	}

	fun run() {

		val thread = Thread{
			try {
				val t = getAccInfo()
				val f = 3*8
				val g =2
			} catch (e: Exception) {
				Log.e("WookieTag",e.toString())//todo
			}
		}
		thread.start()
		thread.join(ApiFuncs.requestTimeOut)
	}

	private fun getPaymentAccDetailsRequest() : Request {
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccount"
		val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
		val uuidStr = ApiFuncs.getUUID()
		val currentTimeStr = ApiFuncs.getCurrentTimeStr()

		val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
		val requestBodyJson = JSONObject()
			.put("requestHeader", JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFuncs.getUserAgent())
				.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", "requiredValueThatIsNotValidated")
				.put("token", authFieldValue)
				.put("isDirectPsu", false)
				.put("directPsu", false)
				.put("accountNumber", accNumber)
			)

		val requestBody = requestBodyJson.toString().toByteArray().toRequestBody(mediaType)
		val request = Request.Builder()
			.url(url)
			.post(requestBody)
			.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
			.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
			.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
			.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
			.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
			.addHeader("AUTHORIZATION", authFieldValue)
			//.addHeader("x-jws-signature", ApiFuncs.getJWS(bodyStr))
			.addHeader("x-request-id", uuidStr)
			.addHeader("content-type", ApiConsts.CONTENT_TYPE)
			.addHeader("accept", ApiConsts.CONTENT_TYPE)
			.build()
		return request
	}

	private fun getAccInfo() : ArrayList<UserData.Account>?{
		val request = getPaymentAccDetailsRequest()
		val response = OkHttpClient().newCall(request).execute()

		val t = response.body?.string()
		val objTEST = JSONObject(t)

		val responseCodeOk = response.code == 200
		if(!responseCodeOk){
			return null//todo
		}

		val bodyStr = response.body?.string()
		if(bodyStr.isNullOrEmpty()){
			return null//todo
		}

		try {
			val responseJson = JSONObject(bodyStr)
			return null
		}catch (e : Exception){
			Log.e("WookieTag",e.toString())
			return null
		}
	}
}