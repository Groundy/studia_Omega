package com.example.omega

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception
import okhttp3.OkHttpClient
import org.json.JSONArray

class TestClass {
	companion object{
		fun doTestRequestInThread(){
			val thread = Thread {
				try {
					test_authorize()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			thread.start()
		}

		fun test_authorize(){
			val uuidStr = ApiFuncs.getUUID()
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
			val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
			var bodyStr = test_getAuthorizeRequestBody(uuidStr)

			val body = bodyStr.toByteArray().toRequestBody(mediaType)
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
				.addHeader("accept", ApiConsts.CONTENT_TYPE)
				.build()

			val client = OkHttpClient()
			val response = client.newCall(request).execute()

			val bodyResponse = response.body
			val c = bodyResponse?.contentLength()
			val str = bodyResponse?.byteString()
			val g = 2+2
		}
		fun test_getAuthorizeRequestBody(UUID : String) : String {
			val currentTimeStr = ApiFuncs.getCurrentTimeStr()
			val endValidityTimeStr = ApiFuncs.getCurrentTimeStr(600)

			var headersArray = JSONObject()
				headersArray.put("requestId", UUID)
				headersArray.put("userAgent", ApiFuncs.getUserAgent())
				headersArray.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				headersArray.put("sendDate", currentTimeStr)
				headersArray.put("tppId", ApiConsts.userId_ALIOR)
				headersArray.put("isCompanyContext", false)
				//headersArray.put("psuIdentifierType", "3784901864194048")
				//headersArray.put("psuIdentifierValue", "6.02")


			var privilegeObj = JSONObject()
				privilegeObj.put("accountNumber", ApiConsts.testAliorBankNr)//????????????
			 	privilegeObj.put("ais-accounts:getAccounts",
				    JSONObject()
					    .put("scopeUsageLimit","multiple")
			    )
			var privelegeListStr = "[${privilegeObj}]".replace("\\","")


			var scopeDetailsObj = JSONObject()
				scopeDetailsObj.put("privilegeList", privelegeListStr)
				scopeDetailsObj.put("scopeGroupType", "ais-accounts")
				//scopeDetailsObj.put("consentId", "MYTPP-b3ae3d34")
				scopeDetailsObj.put("scopeTimeLimit", endValidityTimeStr)
				scopeDetailsObj.put("throttlingPolicy", "psd2Regulatory")



			var bodyJsonObj = JSONObject()
			bodyJsonObj.put("requestHeader",headersArray)
			bodyJsonObj.put("response_type","code")
			bodyJsonObj.put("client_id",ApiConsts.userId_ALIOR)
			bodyJsonObj.put("scope","ais-accounts")
			bodyJsonObj.put("scope_details",scopeDetailsObj)
			bodyJsonObj.put("redirect_uri",ApiConsts.REDIRECT_URI)
			bodyJsonObj.put("state",ApiConsts.testAliorState)//?????

			var toRet = bodyJsonObj.toString().replace("\\/","/")
			//toRet = "{${toRet}}"//wrapping obj to another JsonObj
			return  toRet
		}

	}
}