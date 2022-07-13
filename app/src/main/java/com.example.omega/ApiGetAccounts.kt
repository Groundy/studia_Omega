package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class ApiGetAccounts {
	private lateinit var activity : Activity
	constructor(activity: Activity){
		this.activity = activity
	}

	fun run() {
		var accountListTmp : ArrayList<UserData.PaymentAccount>? = null
		val thread = Thread{
			try {
				accountListTmp = getAccInfo()
				if(accountListTmp != null){
					UserData.accessTokenStruct?.listOfAccounts = accountListTmp
				}
				else{
					//todo
				}
			} catch (e: Exception) {
				Log.e(Utilites.TagProduction,e.toString())//todo
			}
		}
		thread.start()
		thread.join(ApiFunctions.requestTimeOut)
	}

	private fun getAccountsRequest() : Request {
		val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccounts"
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = ApiFunctions.getCurrentTimeStr()

		val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
		val requestBodyJson = JSONObject()
			.put("requestHeader", JSONObject()
				.put("requestId", uuidStr)
				.put("userAgent", ApiFunctions.getUserAgent())
				.put("ipAddress", ApiFunctions.getPublicIPByInternetService())
				.put("sendDate", currentTimeStr)
				.put("tppId", "requiredValueThatIsNotValidated")
				.put("token", authFieldValue)
				.put("isDirectPsu", false)
				.put("directPsu", false)
		)

		val additionalHeaderList = arrayListOf<Pair<String,String>>(Pair("AUTHORIZATION",authFieldValue))
		val request = ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
		return request
	}

	private fun getAccInfo() : ArrayList<UserData.PaymentAccount>?{
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

		val accountsList = return try {
			val responseJson = JSONObject(bodyStr)
			val accountsArray = responseJson.getJSONArray("accounts")
			var accountList = ArrayList<UserData.PaymentAccount>()
			for (i in 0 until accountsArray.length()){
				val accObj =  accountsArray.getJSONObject(i)
				val accountNumber = accObj.getString("accountNumber")
				val tmpAcc = UserData.PaymentAccount(accountNumber)
				accountList.add(tmpAcc)
			}
			accountList
		}catch (e : Exception){
			Log.e(Utilites.TagProduction,e.toString())
			null
		}
		return accountsList
	}
}