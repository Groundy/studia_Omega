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

class API_getToken {
	private var parentActivity: Activity

	constructor(activity: Activity){
		this.parentActivity = activity
	}

	fun run(){
		var tmpTokenStruct : UserData.AccessTokenStruct? = null

		val thread = Thread{
			try {
				tmpTokenStruct = getToken()
			} catch (e: Exception) {
				Log.e("WookieTag",e.toString())
			}
		}
		thread.start()
		thread.join(ApiFuncs.requestTimeOut)
		if(tmpTokenStruct != null)
			UserData.accessTokenStruct = tmpTokenStruct
		else{
			Log.e("WookieTag","Error in obtainging token")
			//Comunicate error
		}
	}


	private fun getToken() : UserData.AccessTokenStruct?{
		val request = getTokenRequest()
		val response = OkHttpClient().newCall(request).execute()

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
		    val tokenType = responseJson.get(ApiConsts.responseField.token_type.toString())
			val accessToken = responseJson.get(ApiConsts.responseField.access_token.toString())
			val refresToken = responseJson.get(ApiConsts.responseField.refresh_token.toString())
			val expiresIn = responseJson.get(ApiConsts.responseField.expires_in.toString())
			val scope = responseJson.get(ApiConsts.responseField.scope.toString())
			val scopeDetails = responseJson.get(ApiConsts.responseField.scope_details.toString())
			val responseHeader = responseJson.get(ApiConsts.responseField.responseHeader.toString())
			val dataOk = !(tokenType.toString().isNullOrEmpty() || accessToken.toString().isNullOrEmpty())

			if(dataOk){
				return UserData.AccessTokenStruct()
					.setTokenContent(accessToken.toString())
					.setTokenType(tokenType.toString())
			}
			else
				return null

		}catch (e : Exception){
			Log.e("WookieTag",e.toString())
			return null
		}
	}

	private fun getTokenRequest() : Request {
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token"
		val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
		val uuidStr = ApiFuncs.getUUID()
		val currentTimeStr = ApiFuncs.getCurrentTimeStr()

		val requestBodyJson = JSONObject()
			.put("requestHeader", JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFuncs.getUserAgent())
				.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", "requiredValueThatIsNotValidated")
				.put("isCompanyContext", false))
			.put("Code",UserData.authCode)
			.put("grant_type","authorization_code")
			.put("redirect_uri",ApiConsts.REDIRECT_URI)
			.put("client_id",ApiConsts.userId_ALIOR)
			.put("client_secret",ApiConsts.appSecret_ALIOR)

		val requestBody = requestBodyJson.toString().toByteArray().toRequestBody(mediaType)
		val request = Request.Builder()
			.url(url)
			.post(requestBody)
			.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
			.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
			.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
			.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
			.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
			//.addHeader("x-jws-signature", ApiFuncs.getJWS(bodyStr))
			.addHeader("x-request-id", uuidStr)
			.addHeader("content-type", ApiConsts.CONTENT_TYPE)
			.addHeader("accept", ApiConsts.CONTENT_TYPE)
			.build()
		return request
	}
}
