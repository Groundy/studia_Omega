package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception

class API_getToken {
	companion object{
		fun run() : Boolean{
			var success = false
			val thread = Thread{
				try {
					val responseJson = getTokenJson()
					if(responseJson != null)
						success = parseJsonResponse(responseJson)
				} catch (e: Exception) {
					Log.e(Utilites.TagProduction,e.toString())
				}
			}
			thread.start()
			thread.join(ApiFuncs.requestTimeOut)
			return success
		}
		private fun getTokenJson() : JSONObject?{
			val request = getTokenRequest()
			val response = OkHttpClient().newCall(request).execute()

			val responseCodeOk = response.code == 200
			if(!responseCodeOk)
				return null//todo

			val bodyStr = response.body?.string()
			if(bodyStr.isNullOrEmpty())
				return null//todo

			try {
				return JSONObject(bodyStr)
			}catch (e : Exception){
				Log.e(Utilites.TagProduction,e.toString())
				return null
			}
		}
		private fun parseJsonResponse(responseJson : JSONObject) : Boolean{
			val tokenType = responseJson.get("token_type")
			val accessTokenCont = responseJson.get("access_token")
			val refreshToken = responseJson.get("refresh_token")
			val expiresIn = responseJson.get("expires_in")
			val scope = responseJson.get("scope")
			val scopeDetails = responseJson.getJSONObject("scope_details")
			val responseHeader = responseJson.get("responseHeader")


			val scopeToSet = when(scope){
				"ais" -> ApiConsts.scopeValues.AIS
				"ais-accounts" ->ApiConsts.scopeValues.AIS_ACC
				"pis" -> ApiConsts.scopeValues.PIS
				else -> null
			}
			val expirationDate = scopeDetails.get("scopeTimeLimit").toString()

			val accountsToSet = ArrayList<UserData.PaymentAccount>()
			val accArray = scopeDetails.getJSONArray("privilegeList")
			for (i in 0 until accArray.length()){
				val accNumber = accArray.getJSONObject(i).getString("accountNumber")
				accountsToSet.add(UserData.PaymentAccount(accNumber))
			}

			val accessToken = UserData.AccessTokenStruct()
				.setTokenContent(accessTokenCont.toString())
				.setTokenType(tokenType.toString())
				.setTokenScope(scopeToSet)
				.setTokenExpirationTime(expirationDate)
				.setRefreshToken(refreshToken as String)
				.addAccounts(accountsToSet)
			UserData.accessTokenStruct = accessToken

			return true
		}

		private fun getTokenRequest() : Request {
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token"
			val uuidStr = ApiFuncs.getUUID()
			val currentTimeStr = ApiFuncs.getCurrentTimeStr()
			val requestBodyJson = JSONObject()
				.put("requestHeader", JSONObject()
					.put("requestId", uuidStr)
					.put("userAgent", ApiFuncs.getUserAgent())
					.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
					.put("sendDate", currentTimeStr)
					.put("tppId", ApiConsts.TTP_ID)
					.put("isCompanyContext", false))
				.put("Code",UserData.authCode)
				.put("grant_type","authorization_code")
				.put("redirect_uri",ApiConsts.REDIRECT_URI)
				.put("client_id",ApiConsts.userId_ALIOR)
				.put("client_secret",ApiConsts.appSecret_ALIOR)


			return ApiFuncs.bodyToRequest(url, requestBodyJson, uuidStr)
		}
	}
}
