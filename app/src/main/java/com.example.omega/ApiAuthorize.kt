package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


class ApiAuthorize(activity: Activity, permisionListObject : PermissionList) {
	private var permissionsList : PermissionList = permisionListObject
	private var callerActivity : Activity = activity
	private var stateValue = String()

	fun run() : Boolean{
		if (permissionsList.permissionsArray.isNullOrEmpty()) {
			Log.e(Utilities.TagProduction, "Error, passed null or empty permissionListObject to ApiAuthorized")
			return false
		}
		else
			Log.i(Utilities.TagProduction, "Authorize started")

		var success = false
		stateValue = ApiFunctions.getRandomStateValue()
		val thread = Thread{
			success = startAuthorize(stateValue)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		Log.i(Utilities.TagProduction, "Authorize ended")
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
				val authUrl = responseJsonObject.get(ApiConsts.ApiReqFields.AspspRedirectUri.text).toString()
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
			.put("scope",ApiConsts.ScopeValues.Ais.text)
			.put("scope_details",getScopeDetailsObject(endValidityTimeStr))
			.put("redirect_uri", ApiConsts.REDIRECT_URI)
			.put("state",stateStr)
		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.AuthUrl.text, requestBodyJson, uuidStr)
	}
	private fun getScopeDetailsObject(expTimeStr : String) : JSONObject? {
		val privListCpy = permissionsList!!.permissionsArray
		val privilegesListJsonObj = JSONObject()
		if(privListCpy.contains(ApiConsts.Privileges.AccountsHistory)){
			privilegesListJsonObj.put(ApiConsts.ApiMethodes.AisGetTransactionsDone.text, JSONObject()
				.put("scopeUsageLimit",ApiConsts.ScopeUsageLimit.Multiple.text)
				.put("maxAllowedHistoryLong",11)
			)
		}
		if(privListCpy.contains(ApiConsts.Privileges.AccountsDetails)){
			privilegesListJsonObj.put(ApiConsts.ApiMethodes.AisGetAccount.text, JSONObject()
				.put("scopeUsageLimit",ApiConsts.ScopeUsageLimit.Multiple.text)
			)
		}

		val scopeDetailObject = JSONObject()
			.put(ApiConsts.ScopeFields.PrivilegeList.text, JSONArray().put(privilegesListJsonObj))
			.put(ApiConsts.ScopeFields.ScopeGroupType.text, ApiConsts.ScopeValues.Ais.text)
			.put(ApiConsts.ScopeFields.ConsentId.text,  ApiConsts.ConsentId)
			.put(ApiConsts.ScopeFields.ScopeTimeLimit.text, expTimeStr)
			.put(ApiConsts.ScopeFields.ThrottlingPolicy.text, ApiConsts.ThrottlingPolicyVal)
		return scopeDetailObject
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