package com.example.omega

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.wait
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class API_authorize {
	private lateinit var parentActivity : Activity
	constructor(activity: Activity){
		this.parentActivity = activity
	}
	private enum class bankJsonRespFields {aspspRedirectUri}

	fun run(){
		val thread = Thread{
			try {
				startAuthorize()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		thread.start()
		thread.join(ApiFuncs.requestTimeOut)
	}

	private fun startAuthorize() : String?{
		val state = ApiFuncs.getRandomStateValue()
		val request = getAuthRequest(state)
		val response = OkHttpClient().newCall(request).execute()

		val responseCodeOk = response.code == 200
		if(responseCodeOk){
			try {
				val responseBody = response.body?.string()
				val responseJsonObject = JSONObject(responseBody)
				val fieldName = bankJsonRespFields.aspspRedirectUri.toString()
				val authUrl = responseJsonObject.get(fieldName).toString()
				ActivityStarter.openBrowserForLogin(parentActivity,authUrl, state)
				return authUrl
			}catch (e : Exception){
				Log.e("WookieTag", e.toString())
				return null
			}
		}
		else{
			Log.e("WookieTag", response.body?.byteStream().toString())
			return null
		}
	}
	private fun getAuthRequest(stateStr : String) : Request {
		val uuidStr = ApiFuncs.getUUID()
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
		val currentTimeStr = ApiFuncs.getCurrentTimeStr()
		val endValidityTimeStr = ApiFuncs.getCurrentTimeStr(300)

		val requestBodyJson = JSONObject()
			.put("requestHeader",JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFuncs.getUserAgent())
				.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", "requiredValueThatIsNotValidated")
				.put("isCompanyContext", false)
			)
			.put("response_type","code")
			.put("client_id",ApiConsts.userId_ALIOR)
			.put("scope","ais")
			.put("scope_details",JSONObject()
				.put("privilegeList", JSONArray()
					.put(JSONObject()
						//.put("ais-accounts:getAccounts",JSONObject()
						//	.put("scopeUsageLimit","multiple")
						//)
						.put("ais:getAccount",JSONObject()
							.put("scopeUsageLimit","multiple")
						)
					)
				)
				.put("scopeGroupType", "ais")
				.put("consentId", "486153511763968")
				.put("scopeTimeLimit", endValidityTimeStr)
				.put("throttlingPolicy", "psd2Regulatory")
			)
			.put("redirect_uri",ApiConsts.REDIRECT_URI)
			.put("state",stateStr)

		val request = ApiFuncs.bodyToRequest(url, requestBodyJson,uuidStr)
		return request
	}
}