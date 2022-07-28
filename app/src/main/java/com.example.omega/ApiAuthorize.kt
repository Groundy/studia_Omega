package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.ApiConsts.*
import com.example.omega.Utilities.Companion.TagProduction


class ApiAuthorize(activity: Activity, permisionListObject : PermissionList) {
	private var permissionsList : PermissionList = permisionListObject
	private var callerActivity : Activity = activity
	private var stateValue = String()

	fun run() : Boolean{
		if (permissionsList.permissionsArray.isEmpty()) {
			Log.e(TagProduction, "Error, passed null or empty permissionListObject to ApiAuthorized")
			return false
		}
		Log.i(TagProduction, "Authorize started")
		var success = false
		val thread = Thread{
			stateValue = ApiFunctions.getRandomStateValue()
			success = startAuthorize(stateValue)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(success)
			Log.i(TagProduction, "Authorize ended with sucess")
		else
			Log.e(TagProduction, "Authorize ended with error")
		return success
	}
	private fun startAuthorize(stateValue : String) : Boolean{
		return try{
			val request = getAuthRequest(stateValue)
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.responseOkCode){
				Log.e(TagProduction, "[startAuthorize/${this.javaClass.name}] Error ${ApiFunctions.getErrorTextOfRequestToLog(response.code)}")
				return false
			}
			val responseBody = response.body?.string()
			val responseJsonObject = JSONObject(responseBody!!)
			val authUrl = responseJsonObject.get(ApiReqFields.AspspRedirectUri.text).toString()
			if(authUrl.isEmpty())
				return false
			saveDataToPrefs(authUrl)
			true
		}catch (e : Exception){
			Log.e(TagProduction,"[startAuthorize/${this.javaClass.name}] Error catch $e")
			false
		}
	}
	private fun getAuthRequest(stateStr : String) : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()
		val endValidityTimeStr = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)

		val requestBodyJson = JSONObject()
			.put(ApiReqFields.RequestHeader.text, JSONObject()
				.put(ApiReqFields.RequestId.text, uuidStr)
				.put(ApiReqFields.UserAgent.text, ApiFunctions.getUserAgent())
				.put(ApiReqFields.IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
				.put(ApiReqFields.SendDate.text, currentTimeStr)
				.put(ApiReqFields.TppId.text, ApiConsts.TTP_ID)
				.put(ApiReqFields.IsCompanyContext.text, false)
			)
			.put(ApiReqFields.ResponseType.text,ResponseTypes.Code.text)
			.put(ApiReqFields.ClientId.text, ApiConsts.userId_ALIOR)
			.put(ApiReqFields.Scope.text, ScopeValues.Ais.text)
			.put(ApiReqFields.ScopeDetails.text,getScopeDetailsObject(endValidityTimeStr))
			.put(ApiReqFields.RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ApiReqFields.State.text,stateStr)
		return ApiFunctions.bodyToRequest(BankUrls.AuthUrl.text, requestBodyJson, uuidStr)
	}
	private fun getScopeDetailsObject(expTimeStr : String) : JSONObject? {
		val privListCpy = permissionsList.permissionsArray
		val privilegesListJsonObj = JSONObject()
		if(privListCpy.contains(Privileges.AccountsHistory)){
			privilegesListJsonObj.put(ApiMethodes.AisGetTransactionsDone.text, JSONObject()
				.put(ScopeDetailsFields.ScopeUsageLimit.text,ScopeUsageLimit.Multiple.text)
				.put(ScopeDetailsFields.MaxAllowedHistoryLong.text,11)
			)
		}
		if(privListCpy.contains(Privileges.AccountsDetails)){
			privilegesListJsonObj.put(ApiMethodes.AisGetAccount.text, JSONObject()
				.put(ScopeDetailsFields.ScopeUsageLimit.text,ScopeUsageLimit.Multiple.text)
			)
		}

		return JSONObject() //scopeDetailObject
			.put(ScopeFields.PrivilegeList.text, JSONArray().put(privilegesListJsonObj))
			.put(ScopeFields.ScopeGroupType.text, ScopeValues.Ais.text)
			.put(ScopeFields.ConsentId.text,  ApiConsts.ConsentId)
			.put(ScopeFields.ScopeTimeLimit.text, expTimeStr)
			.put(ScopeFields.ThrottlingPolicy.text, ApiConsts.ThrottlingPolicyVal)
	}
	 private fun saveDataToPrefs(authUrl :String){
		PreferencesOperator.savePref(callerActivity, R.string.PREF_authURL, authUrl)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_lastRandomValue, stateValue)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_lastUsedPermissionsForAuth, permissionsList.toString())
		val validityTime = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_authUrlValidityTimeEnd, validityTime)
	}

	companion object{
		fun obtainingNewAuthUrlIsNecessary(activity: Activity, permisionListObject: PermissionList?) : Boolean{
			val authUrl = PreferencesOperator.readPrefStr(activity, R.string.PREF_authURL)
			if(authUrl.isEmpty())
				return true

			val lastPermissionListStr= PreferencesOperator.readPrefStr(activity, R.string.PREF_lastUsedPermissionsForAuth)
			if(lastPermissionListStr.isEmpty())
				return true

			if(lastPermissionListStr != permisionListObject.toString())
				return true

			val lastAuthUrlValidityTime = PreferencesOperator.readPrefStr(activity, R.string.PREF_authUrlValidityTimeEnd)
			if(lastAuthUrlValidityTime.isEmpty())
				return true

			val authTimeIsStillValid = OmegaTime.timestampIsValid(lastAuthUrlValidityTime)
			if(!authTimeIsStillValid)
				return true

			val lastTimeUsedRandomStateValue = PreferencesOperator.readPrefStr(activity, R.string.PREF_lastRandomValue)
			if(lastTimeUsedRandomStateValue.isEmpty())
				return true

			return false
		}
	}

}