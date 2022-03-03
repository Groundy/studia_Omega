package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import org.json.JSONObject
import java.lang.Exception
import kotlin.concurrent.thread

class API_getAccounts {
	private lateinit var activity : Activity
	enum class responseFields{
		accounts, accountNumber, accountType, accountTypeName
	}
	constructor(activity: Activity){
		this.activity = activity
	}

	fun run() {
		var accountListTmp : ArrayList<UserData.Account>? = null
		val thread = Thread{
			try {
				accountListTmp = getAccInfo()
				if(accountListTmp != null){
					UserData.accList = accountListTmp
				}
				else{
					//todo
				}
			} catch (e: Exception) {
				Log.e("WookieTag",e.toString())//todo
			}
		}
		thread.start()
		thread.join(ApiFuncs.requestTimeOut)
	}

	private fun getAccountsRequest() : Request {
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccounts"
		val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
		val uuidStr = ApiFuncs.getUUID()
		val currentTimeStr = ApiFuncs.getCurrentTimeStr()

		val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
		val requestBodyJson = JSONObject()
			.put("requestHeader", JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFuncs.getUserAgent())
				.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", "requiredValueThatIsNotValidated")
				.put("token", authFieldValue)
				.put("isDirectPsu", false)
				.put("directPsu", false)
		)

		val requestBody = requestBodyJson.toString().toByteArray().toRequestBody(mediaType)
		val request = Request.Builder()
			.url(url)
			.post(requestBody)
			.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
			.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
			.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
			.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
			.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
			.addHeader("AUTHORIZATION", authFieldValue)
			//.addHeader("x-jws-signature", ApiFuncs.getJWS(bodyStr))
			.addHeader("x-request-id", uuidStr)
			.addHeader("content-type", ApiConsts.CONTENT_TYPE)
			.addHeader("accept", ApiConsts.CONTENT_TYPE)
			.build()
		return request
	}

	private fun getAccInfo() : ArrayList<UserData.Account>?{
		val request = getAccountsRequest()
		val response = OkHttpClient().newCall(request).execute()

		val responseCodeOk = response.code == 200
		if(!responseCodeOk){
			return null//todo
		}

		val bodyStr = response.body?.string()
		if(bodyStr.isNullOrEmpty()){
			return null//todo
		}

		try {
			val responseJson = JSONObject(bodyStr)
			val accountsArray = responseJson.getJSONArray(responseFields.accounts.toString())
			var accountList = ArrayList<UserData.Account>()
			for (i in 0 until accountsArray.length()){
				val accObj =  accountsArray.getJSONObject(i)
				val accountNumber = accObj.getString(responseFields.accountNumber.toString())
				val accType = accObj.getString(responseFields.accountTypeName.toString())
				val tmpAcc = UserData.Account().setNumber(accountNumber).setType(accType)
				accountList.add(tmpAcc)
			}
			return accountList
		}catch (e : Exception){
			Log.e("WookieTag",e.toString())
			return null
		}
	}
}