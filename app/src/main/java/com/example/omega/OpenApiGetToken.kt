package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*

class OpenApiGetToken(private val callerActivity: Activity, private val scope : ApiConsts.ScopeValues) {
	fun run(dialog: WaitingDialog? = null) : Boolean{
		if(dialog!=null)
			dialog.changeText(callerActivity, R.string.POPUP_getToken)
		Log.i(TagProduction, "GetToken started")
		var success = false
		val thread = Thread{
			try {
				val request = getRequest()
				val responseJson = sendRequest(request)?: return@Thread
				handleResponse(responseJson)
				success = true
			} catch (e: Exception) {
				Log.e(TagProduction,"[run/${this.javaClass.name}] $e")
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		Log.i(TagProduction, "GetToken ended sucessfuly? $success")
		if(dialog!=null)
			dialog.changeText(callerActivity, R.string.POPUP_empty)
		return success
	}
	private fun getRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()

		val headerJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, OmegaTime.getCurrentTime())
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
	private fun handleResponse(responseJson : JSONObject){
		val accessToken = Token(responseJson)
		val accessTokenSerialized = accessToken.toString()
		val resouceIdWhereToSaveToken = if(scope == ApiConsts.ScopeValues.Ais)
			R.string.PREF_Token
		else
			R.string.PREF_PaymentToken
		PreferencesOperator.savePref(callerActivity, resouceIdWhereToSaveToken, accessTokenSerialized)
		PreferencesOperator.clearPreferences(callerActivity, R.string.PREF_authCode)
	}
	private fun sendRequest(request: Request) : JSONObject?{
		return try {
			val response = OkHttpClient().newCall(request).execute()
			val responseCode = response.code
			if(responseCode != ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.logResponseError(response, this.javaClass.name)
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

