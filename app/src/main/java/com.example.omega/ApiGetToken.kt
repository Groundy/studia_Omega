package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*

class ApiGetToken(activity: Activity) {
	private var callerActivity = activity
	private var errorToDisplay = String()

	fun run() : Boolean{
		Log.i(TagProduction, "GetToken started")
		var success = false
		val thread = Thread{
			try {
				val responseJson = getTokenJson()
				if(responseJson != null)
					success = parseJsonResponse(responseJson)
				return@Thread
			} catch (e: Exception) {
				Log.e(TagProduction,e.toString())
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(errorToDisplay.isNotEmpty())
			Utilities.showToast(callerActivity, errorToDisplay)
		Log.i(TagProduction, "GetToken ended")
		return success
	}
	private fun getTokenRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()
		val requestBodyJson = JSONObject()
			.put(RequestHeader.text, JSONObject()
				.put(RequestId.text, uuidStr)
				.put(UserAgent.text, ApiFunctions.getUserAgent())
				.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
				.put(SendDate.text, currentTimeStr)
				.put(TppId.text, ApiConsts.TTP_ID)
				.put(IsCompanyContext.text, false))
			.put(Code.text, PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_authCode))
			.put(GrantType.text,ApiConsts.GrantTypes.AuthorizationCode.text)
			.put(RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ClientId.text, ApiConsts.userId_ALIOR)
			.put(ClientSecret.text, ApiConsts.appSecret_ALIOR)

		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl.text, requestBodyJson, uuidStr)
	}
	private fun parseJsonResponse(responseJson : JSONObject) : Boolean{
		val accessToken = Token(responseJson)
		if(!accessToken.isOk())
			return false

		val accessTokenSerialized = accessToken.toString()
		PreferencesOperator.savePref(callerActivity, R.string.PREF_accessToken, accessTokenSerialized)
		return true
	}
	private fun getTokenJson() : JSONObject?{
		val request = getTokenRequest()
		val response = OkHttpClient().newCall(request).execute()

		if(response.code == 401){
			Log.e(TagProduction, "Request for token in getToken was unAuthorized!")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorUnauthorizedRequest)
			return null
		}

		val responseCodeError = response.code != 200
		if(responseCodeError){
			Log.e(TagProduction, "Error, getToken response returned code [${response.code}]")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
			return null
		}

		val bodyStr = response.body?.string()
		if(bodyStr.isNullOrEmpty()){
			Log.e(TagProduction, "Error, getToken response body is null or empty")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
			return null
		}
		return try {
			JSONObject(bodyStr)
		}catch (e : Exception){
			Log.e(TagProduction,e.toString())
			null
		}
	}
}

