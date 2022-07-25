package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
class ApiGetToken {
	private lateinit var callerActivity: Activity
	private var errorToDisplay = String()

	fun run(activity: Activity) : Boolean{
		Log.i(Utilities.TagProduction, "GetToken function started")
		callerActivity = activity
		var success = false
		val thread = Thread{
			try {
				val responseJson = getTokenJson()
				if(responseJson != null)
					success = parseJsonResponse(responseJson)
				return@Thread
			} catch (e: Exception) {
				Log.e(Utilities.TagProduction,e.toString())
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(errorToDisplay.isNotEmpty())
			Utilities.showToast(callerActivity, errorToDisplay)
		return success
	}
	private fun getTokenRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()
		val requestBodyJson = JSONObject()
			.put(ApiConsts.ApiRequestsBodyFields.RequestHeader.text, JSONObject()
				.put(ApiConsts.ApiRequestsBodyFields.RequestId.text, uuidStr)
				.put(ApiConsts.ApiRequestsBodyFields.UserAgent.text, ApiFunctions.getUserAgent())
				.put(ApiConsts.ApiRequestsBodyFields.IpAddress.text, ApiFunctions.getPublicIPByInternetService())
				.put(ApiConsts.ApiRequestsBodyFields.SendDate.text, currentTimeStr)
				.put(ApiConsts.ApiRequestsBodyFields.TppId.text, ApiConsts.TTP_ID)
				.put(ApiConsts.ApiRequestsBodyFields.IsCompanyContext.text, false))
			.put(ApiConsts.ApiRequestsBodyFields.Code.text, PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_authCode))
			.put(ApiConsts.ApiRequestsBodyFields.GrantType.text,"authorization_code")
			.put(ApiConsts.ApiRequestsBodyFields.RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ApiConsts.ApiRequestsBodyFields.ClientId.text, ApiConsts.userId_ALIOR)
			.put(ApiConsts.ApiRequestsBodyFields.ClientSecret.text, ApiConsts.appSecret_ALIOR)

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
			Log.e(Utilities.TagProduction, "Request for token in getToken was unAuthorized!")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorUnauthorizedRequest)
			return null
		}

		val responseCodeError = response.code != 200
		if(responseCodeError){
			Log.e(Utilities.TagProduction, "Error, getToken response returned code [${response.code}]")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
			return null
		}

		val bodyStr = response.body?.string()
		if(bodyStr.isNullOrEmpty()){
			Log.e(Utilities.TagProduction, "Error, getToken response body is null or empty")
			errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
			return null
		}
		return try {
			JSONObject(bodyStr)
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,e.toString())
			null
		}
	}
}

