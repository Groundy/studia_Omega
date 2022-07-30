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

	fun run() : Boolean{
		Log.i(TagProduction, "GetToken started")
		var success = false
		val thread = Thread{
			try {
				val responseJson = getTokenResponseJson()?: return@Thread
				success = parseJsonResponse(responseJson)
			} catch (e: Exception) {
				Log.e(TagProduction,e.toString())
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		Log.i(TagProduction, "GetToken ended")
		return success
	}
	private fun getTokenRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val headerJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTimeStr)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(IsCompanyContext.text, false)

		val requestBodyJson = JSONObject()
			.put(RequestHeader.text, headerJson)
			.put(Code.text, PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_authCode))
			.put(GrantType.text,ApiConsts.GrantTypes.AuthorizationCode.text)
			.put(RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ClientId.text, ApiConsts.userId_ALIOR)
			.put(ClientSecret.text, ApiConsts.appSecret_ALIOR)

		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl, requestBodyJson, uuidStr)
	}
	private fun parseJsonResponse(responseJson : JSONObject) : Boolean{
		val accessToken = Token(responseJson)
		if(!accessToken.isOk(callerActivity))
			return false

		val accessTokenSerialized = accessToken.toString()
		PreferencesOperator.savePref(callerActivity, R.string.PREF_Token, accessTokenSerialized)
		return true
	}
	private fun getTokenResponseJson() : JSONObject?{
		return try {
			val request = getTokenRequest()
			val response = OkHttpClient().newCall(request).execute()
			val responseCode = response.code
			if(responseCode != ApiConsts.responseOkCode){
				Log.e(TagProduction, "[getTokenResponseJson/ ${this.javaClass.name}] ${ApiFunctions.getErrorTextOfRequestToLog(responseCode)}")
				val errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
				Utilities.showToast(callerActivity, errorToDisplay)
				return null
			}
			val bodyStr = response.body?.string()
			JSONObject(bodyStr!!)
		}catch (e : Exception){
			Log.e(TagProduction,e.toString())
			null
		}
	}
}

