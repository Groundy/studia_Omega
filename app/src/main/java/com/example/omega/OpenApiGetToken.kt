package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class OpenApiGetToken(private val callerActivity: Activity, private val scope : ApiConsts.ScopeValues) {
	suspend fun run() : Boolean{
		return try {
			Log.i(TagProduction, "GetToken started")
			val request = getRequest()
			val responseJson = sendRequest(request) ?: throw Exception("logged before")
			handleResponse(responseJson)
			true
		} catch (e: Exception) {
			Log.e(TagProduction,"[run/${this.javaClass.name}] GetToken ended with failure, e=$e")
			false
		}
	}
	private fun getRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()

		val bodyHeaders = JSONObject()
		with(bodyHeaders){
			put(RequestId.text, uuidStr)
			put(UserAgent.text, ApiFunctions.getUserAgent())
			put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			put(SendDate.text, OmegaTime.getCurrentTime())
			put(TppId.text, ApiConsts.TTP_ID)
			put(IsCompanyContext.text, false)
		}

		val body = JSONObject()
		with(body){
			put(RequestHeader.text, bodyHeaders)
			put(Code.text, PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_authCode))
			put(GrantType.text,ApiConsts.GrantTypes.AuthorizationCode.text)
			put(RedirectUri.text, ApiConsts.REDIRECT_URI)
			put(ClientId.text, ApiConsts.userId_ALIOR)
			put(ClientSecret.text, ApiConsts.appSecret_ALIOR)
		}

		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl, body, uuidStr)
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
	private suspend fun sendRequest(request: Request) : JSONObject?{
		return try {
			val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
			val response = client.newCall(request).execute()
			val responseCode = response.code
			if(responseCode != ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.logResponseError(response, this.javaClass.name)
				withContext(Main){
					val errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
					ActivityStarter.startOperationResultActivity(callerActivity, errorToDisplay)
				}
				return null
			}
			val bodyStr = response.body?.string()
			JSONObject(bodyStr!!)
		}catch (e : Exception){
			Log.e(TagProduction,e.toString())
			withContext(Main){
				val errorToDisplay = callerActivity.getString(R.string.UserMsg_Banking_errorObtaingToken)
				ActivityStarter.startOperationResultActivity(callerActivity, errorToDisplay)
			}
			null
		}
	}
}

