package com.example.omega

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class API_authorize {
	private var permissionsList : List<ApiConsts.priviliges>? = null
	fun run(stateValue : String,  permissions : List<ApiConsts.priviliges>? = null) : String?{
		var authUrl : String? = null
		permissionsList = permissions

		val thread = Thread{
			authUrl = startAuthorize(stateValue)
		}
		thread.start()
		thread.join(ApiFuncs.requestTimeOut)
		return authUrl
	}

	private fun startAuthorize(stateValue : String) : String?{
		try{
			val request = getAuthRequest(stateValue)
			val response = OkHttpClient().newCall(request).execute()

			val responseCodeOk = response.code == 200
			return if(responseCodeOk){
				val responseBody = response.body?.string()
				val responseJsonObject = JSONObject(responseBody)
				val fieldName = "aspspRedirectUri"
				val authUrl = responseJsonObject.get(fieldName).toString()
				authUrl
			} else{
				Log.e(Utilites.TagProduction, "Got auth response, Code=${response.code}, body=${response.body?.byteString()}")
				null
			}
		}catch (e : Exception){
			Log.e(Utilites.TagProduction,e.toString())
			return null
		}
	}
	private fun getAuthRequest(stateStr : String) : Request {
		val uuidStr = ApiFuncs.getUUID()
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
		val currentTimeStr = ApiFuncs.getCurrentTimeStr()
		val endValidityTimeStr = ApiFuncs.getCurrentTimeStr(65 * 60)

		val requestBodyJson = JSONObject()
			.put("requestHeader",JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFuncs.getUserAgent())
				.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", ApiConsts.TTP_ID)
				.put("isCompanyContext", false)
			)
			.put("response_type","code")
			.put("client_id", ApiConsts.userId_ALIOR)
			.put("scope","ais")
			.put("scope_details",getScopeDetailsObject(endValidityTimeStr))
			.put("redirect_uri", ApiConsts.REDIRECT_URI)
			.put("state",stateStr)
		return ApiFuncs.bodyToRequest(url, requestBodyJson, uuidStr)
	}
	private fun getScopeDetailsObject(expTimeStr : String) : JSONObject{
		var permissionListArray = JSONArray()
		if(!permissionsList.isNullOrEmpty()){
			var toAddObject = JSONObject()

			if(permissionsList!!.contains(ApiConsts.priviliges.accountsHistory)){
				toAddObject.put("ais:getTransactionsDone",JSONObject()
					.put("scopeUsageLimit","multiple")
					.put("maxAllowedHistoryLong",11)
				)
			}
			if(permissionsList!!.contains(ApiConsts.priviliges.accountsDetails)){
				toAddObject.put("ais:getAccount",JSONObject()
					.put("scopeUsageLimit","multiple")
				)
			}

			permissionListArray.put(toAddObject)
		}
		else{
			//test
			permissionListArray.put(JSONObject()
				/*
				.put("ais:getAccount",JSONObject()
					.put("scopeUsageLimit","multiple")
				)
				*/
				.put("ais:getTransactionsDone",JSONObject()
					.put("scopeUsageLimit","multiple")
					.put("maxAllowedHistoryLong",11)
				)
			)
		}

		var scopeDetailsObjToRet = JSONObject()
			.put("privilegeList", permissionListArray)
			.put("scopeGroupType", "ais")
			.put("consentId", "123456789")
			.put("scopeTimeLimit", expTimeStr)
			.put("throttlingPolicy", "psd2Regulatory")
		return scopeDetailsObjToRet
	}
}