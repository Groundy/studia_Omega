package com.example.omega


import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.ApiConsts.ApiReqFields
import com.example.omega.ApiConsts.ScopeValues

class OpenApiRefreshToken(private val refreshToken : String) {

	fun run() : JSONObject?{
		Log.i(Utilities.TagProduction, "refresh token started")
		var toRet : JSONObject? = null
		val thread = Thread{
			toRet = try {
				val request = getRequest()
				val response = sendRequest(request)
				response
			} catch (e: Exception) {
				Log.e(Utilities.TagProduction,"[ApiRefreshToken/${this.javaClass.name}] $e  ")
				null
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		Log.i(Utilities.TagProduction, "refresh token ended with sucess? ${toRet != null}")
		return toRet
	}
	private fun getRequest() : Request{
		val uuid = ApiFunctions.getUUID()
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

		return 	ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTokenUrl, jsonBodyRequest, uuid)
	}
	private fun sendRequest(request: Request) : JSONObject?{
		return try{
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.LogResponseError(response, this.javaClass.name)
				return null
			}
			val responseBodyStr = response.body?.string()
			val responseJsonObject = JSONObject(responseBodyStr!!)
			responseJsonObject
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catched $e")
			null
		}
	}
}