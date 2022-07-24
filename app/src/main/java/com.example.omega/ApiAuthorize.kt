package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


class ApiAuthorize {
	private var permissionsList : PermissionList? = null
	private lateinit var callerActivity : Activity
	private var stateValue = String()
	fun run(activity: Activity, permisionListObject : PermissionList? = null) : Boolean{
		Log.i(Utilities.TagProduction, "Authorize started")
		var success = false
		permissionsList = permisionListObject
		callerActivity = activity
		stateValue = ApiFunctions.getRandomStateValue()

		val thread = Thread{
			success = startAuthorize(stateValue)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		return success
	}

	private fun startAuthorize(stateValue : String) : Boolean{
		return try{
			val request = getAuthRequest(stateValue)
			val response = OkHttpClient().newCall(request).execute()

			val responseCodeOk = response.code == 200
			return if(responseCodeOk){
				val responseBody = response.body?.string()
				val responseJsonObject = JSONObject(responseBody!!)
				val authUrl = responseJsonObject.get("aspspRedirectUri").toString()
				if(!authUrl.isNullOrEmpty()){//save to prefs
					PreferencesOperator.savePref(callerActivity, R.string.PREF_authURL, authUrl)
					PreferencesOperator.savePref(callerActivity, R.string.PREF_lastRandomValue, stateValue)
					PreferencesOperator.savePref(callerActivity, R.string.PREF_lastUsedPermissionsForAuth, permissionsList.toString())
					val validityTime = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)
					PreferencesOperator.savePref(callerActivity, R.string.PREF_authUrlValidityTimeEnd, validityTime)
					true
				}
				else
					false
			} else{
				Log.e(Utilities.TagProduction, "Got auth response, Code=${response.code}, body=${response.body?.byteString()}")
				false
			}
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,e.toString())
			false
		}
	}
	private fun getAuthRequest(stateStr : String) : Request {
		val uuidStr = ApiFunctions.getUUID()
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
		val currentTimeStr = OmegaTime.getCurrentTime()
		val endValidityTimeStr = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)

		val requestBodyJson = JSONObject()
			.put("requestHeader", JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFunctions.getUserAgent())
				.put("ipAddress", ApiFunctions.getPublicIPByInternetService())
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
		return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr)
	}
	private fun getScopeDetailsObject(expTimeStr : String) : JSONObject {
		val permissionListArray = JSONArray()
		if(!permissionsList!!.permissionsArray.isNullOrEmpty()){
			val toAddObject = JSONObject()

			if(permissionsList!!.permissionsArray.contains(ApiConsts.Privileges.AccountsHistory)){
				toAddObject.put("ais:getTransactionsDone", JSONObject()
					.put("scopeUsageLimit","multiple")
					.put("maxAllowedHistoryLong",11)
				)
			}
			if(permissionsList!!.permissionsArray.contains(ApiConsts.Privileges.AccountsDetails)){
				toAddObject.put("ais:getAccount", JSONObject()
					.put("scopeUsageLimit","multiple")
				)
			}

			permissionListArray.put(toAddObject)
		}
		else{
			//test
			permissionListArray.put(
				JSONObject()
				/*
				.put("ais:getAccount",JSONObject()
					.put("scopeUsageLimit","multiple")
				)
				*/
				.put("ais:getTransactionsDone", JSONObject()
					.put("scopeUsageLimit","multiple")
					.put("maxAllowedHistoryLong",11)
				)
			)
		}

		return JSONObject() // scopeDetailsObj
			.put("privilegeList", permissionListArray)
			.put("scopeGroupType", "ais")
			.put("consentId", "123456789")
			.put("scopeTimeLimit", expTimeStr)
			.put("throttlingPolicy", "psd2Regulatory")
	}

	companion object{
		fun obtainingNewAuthUrlIsNecessary(activity: Activity, permisionListObject: PermissionList?) : Boolean{
			val authUrl = PreferencesOperator.readPrefStr(activity, R.string.PREF_authURL)
			if(authUrl.isNullOrEmpty())
				return true

			val lastPermissionListStr= PreferencesOperator.readPrefStr(activity, R.string.PREF_lastUsedPermissionsForAuth)
			if(lastPermissionListStr.isNullOrEmpty())
				return true
			if(lastPermissionListStr != permisionListObject.toString())
				return true

			val lastAuthUrlValidityTime = PreferencesOperator.readPrefStr(activity, R.string.PREF_authUrlValidityTimeEnd)
			if(lastAuthUrlValidityTime.isNullOrEmpty())
				return true
			val authTimeIsStillValid = OmegaTime.timestampIsValid(lastAuthUrlValidityTime)
			if(!authTimeIsStillValid)
				return true

			val lastTimeUsedRandomStateValue = PreferencesOperator.readPrefStr(activity, R.string.PREF_lastRandomValue)
			if(lastTimeUsedRandomStateValue.isNullOrEmpty())
				return true

			return false
		}
	}
}