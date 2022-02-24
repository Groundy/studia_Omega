package com.example.omega

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.JsonToken
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception
import okhttp3.OkHttpClient
import okio.Utf8
import org.json.JSONArray
import java.lang.StringBuilder

class TestClass {
	companion object{
		fun doTestRequestInThread(activity : Activity){
			val thread = Thread {
				try {
					authorize(activity)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			thread.start()
		}

		fun authorize(activity : Activity) : String?{
			val uuidStr = ApiFuncs.getUUID()
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
			val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
			val state = "wergh3w4hg3q1^g3g"
			val body = getBodyForAuthRequest(uuidStr, state).toByteArray().toRequestBody(mediaType)

			val request = Request.Builder()
				.url(url)
				.post(body)
				.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
				.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
				.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
				.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
				.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
				//.addHeader("x-jws-signature", ApiFuncs.getJWS(bodyStr))
				.addHeader("x-request-id", uuidStr)
				.addHeader("content-type", ApiConsts.CONTENT_TYPE)
				.addHeader("accXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXept", ApiConsts.CONTENT_TYPE)
				.build()

			//val client = OkHttpClient()
			val response = OkHttpClient().newCall(request).execute()

			val responseCodeOk = response.code == 200
			if(responseCodeOk){
				try {
					val responseBody = response.body?.string()
					val responseJsonObject = JSONObject(responseBody)
					val authUrl = responseJsonObject.get("aspspRedirectUri").toString()
					ActivityStarter.openBrowserForLogin(activity,authUrl, state)
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
		private fun getBodyForAuthRequest(UUID : String, state : String) : String {
			val currentTimeStr = ApiFuncs.getCurrentTimeStr()
			val endValidityTimeStr = ApiFuncs.getCurrentTimeStr(600)

			var headersArray = JSONObject()
				headersArray.put("requestId", UUID)
				headersArray.put("userAgent", ApiFuncs.getUserAgent())
				headersArray.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				headersArray.put("sendDate", currentTimeStr)
				headersArray.put("tppId", "requiredValueThatIsNotValidated")
				headersArray.put("isCompanyContext", false)
				//headersArray.put("psuIdentifierType", "P")
				//headersArray.put("psuIdentifierValue", "12345678")


			var privilegeObj = JSONObject()
				//privilegeObj.put("accountNumber", "132516")//????????????
			 	privilegeObj.put("ais-accounts:getAccounts",
				    JSONObject()
					    .put("scopeUsageLimit","multiple")
			    )

			var privilegeArray = JSONArray()
				//.put(JSONObject().put("accountNumber", ApiConsts.testAliorBankNr))
				.put(JSONObject().put("ais-accounts:getAccounts",JSONObject().put("scopeUsageLimit","multiple")))


			var scopeDetailsObj = JSONObject()
				scopeDetailsObj.put("privilegeList",privilegeArray)
				scopeDetailsObj.put("scopeGroupType", "ais-accounts")
				scopeDetailsObj.put("consentId", "486153511763968")
				scopeDetailsObj.put("scopeTimeLimit", endValidityTimeStr)
				scopeDetailsObj.put("throttlingPolicy", "psd2Regulatory")



			var bodyJsonObj = JSONObject()
			bodyJsonObj.put("requestHeader",headersArray)
			bodyJsonObj.put("response_type","code")
			bodyJsonObj.put("client_id",ApiConsts.userId_ALIOR)
			bodyJsonObj.put("scope","ais-accounts")
			bodyJsonObj.put("scope_details",scopeDetailsObj)
			bodyJsonObj.put("redirect_uri",ApiConsts.REDIRECT_URI)
			bodyJsonObj.put("state",state)

			var toRet = bodyJsonObj.toString().replace("\\/","/")
			//toRet = "{${toRet}}"//wrapping obj to another JsonObj
			return  toRet
		}

	}
}