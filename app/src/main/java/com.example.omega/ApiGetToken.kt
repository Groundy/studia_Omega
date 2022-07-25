package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
class ApiGetToken {
	companion object{
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
		private fun parseJsonResponse(responseJson : JSONObject) : Boolean{
			val accessToken = Token(responseJson)
			if(!accessToken.isOk())
				return false

			val accessTokenSerialized = accessToken.toString()
			PreferencesOperator.savePref(callerActivity, R.string.PREF_accessToken, accessTokenSerialized)
			return true
		}
		private fun getTokenRequest() : Request {
			val uuidStr = ApiFunctions.getUUID()
			val currentTimeStr = OmegaTime.getCurrentTime()
			val requestBodyJson = JSONObject()
				.put("requestHeader", JSONObject()
					.put("requestId", uuidStr)
					.put("userAgent", ApiFunctions.getUserAgent())
					.put("ipAddress", ApiFunctions.getPublicIPByInternetService())
					.put("sendDate", currentTimeStr)
					.put("tppId", ApiConsts.TTP_ID)
					.put("isCompanyContext", false))
				.put("Code", PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_authCode))
				.put("grant_type","authorization_code")
				.put("redirect_uri", ApiConsts.REDIRECT_URI)
				.put("client_id", ApiConsts.userId_ALIOR)
				.put("client_secret", ApiConsts.appSecret_ALIOR)

			return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl.text, requestBodyJson, uuidStr)
		}
	}
}

