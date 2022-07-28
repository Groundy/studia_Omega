package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.ApiConsts.ApiReqFields
import com.example.omega.ApiConsts.ScopeValues

class ApiRefreshToken(activity: Activity, token: Token) {
	private var token = token
	private var callerActivity = activity

	fun run() : Boolean{
		Log.i(Utilities.TagProduction, "refresh token started")
		var success = false
		val thread = Thread{
			try {
				success = sendRequest()
			} catch (e: Exception) {
				Log.e(Utilities.TagProduction,"[ApiRefreshToken/${this.javaClass.name}] $e")
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		Log.i(Utilities.TagProduction, "refresh token ended with sucess? $success")
		return success
	}
	private fun getRequest() : Request{
		val uuid = ApiFunctions.getUUID()
		val refreshToken = token.getRefreshTokenValue()
		if(refreshToken == null){
			//todo
		}
		val requestHeaders = JSONObject()
			.put(ApiReqFields.RequestId.text, uuid)
			.put(ApiReqFields.SendDate.text, OmegaTime.getCurrentTime())
			.put(ApiReqFields.TppId.text, ApiConsts.TTP_ID)

		val jsonBodyRequest = JSONObject()
			.put(ApiReqFields.RequestHeader.text, requestHeaders)
			.put(ApiReqFields.GrantType.text, ApiConsts.GrantTypes.RefreshToken.text)
			.put(ApiReqFields.RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ApiReqFields.ClientId.text, ApiConsts.userId_ALIOR)
			.put(ApiReqFields.RefreshToken.text, refreshToken)
			.put(ApiReqFields.Scope.text, ScopeValues.AisAcc.text)

		return 	ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl.text, jsonBodyRequest, uuid)
	}
	private fun sendRequest() : Boolean{
		return try{
			val request = getRequest()
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.responseOkCode){
				Log.e(Utilities.TagProduction, "[sendRequest/${this.javaClass.name}] Error ${ApiFunctions.getErrorTextOfRequestToLog(response.code)}")
				return false
			}
			val responseBody = response.body?.string()
			val responseJsonObject = JSONObject(responseBody!!)
			token.replaceTokenWithFreshOne(responseJsonObject)

			true
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catched $e")
			false
		}
	}

}