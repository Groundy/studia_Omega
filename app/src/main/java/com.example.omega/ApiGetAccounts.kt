package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
//Czy to do cholery jest w ogóle potrzebne?!

class ApiGetAccounts {
	private lateinit var callerActivity : Activity
	private lateinit var token : Token

	constructor(activity: Activity, token: Token){
		callerActivity = activity
		this.token = token
	}
	fun run() {
		/*
				var accountListTmp : ArrayList<PaymentAccount>?
		val thread = Thread{
			try {
				accountListTmp = getAccInfo()
				if(accountListTmp != null){
					val listOfAcc = accountListTmp?.toList()!!//todo rename it better
					token.fillAllAccWithDetails(listOfAcc)
				}
				else{
					//todo
				}
			} catch (e: Exception) {
				Log.e(Utilities.TagProduction,e.toString())//todo
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		*/

	}

	private fun getAccountsRequest() : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val authFieldValue = token.getAuthFieldValue()
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
		val request = ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetAccounts.text, requestBodyJson, uuidStr, additionalHeaderList)
		return request
	}
	private fun getAccInfo() : ArrayList<PaymentAccount>?{
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
			var accountList = ArrayList<PaymentAccount>()
			for (i in 0 until accountsArray.length()){
				val accObj =  accountsArray.getJSONObject(i)
				val accountNumber = accObj.getString("accountNumber")
				val tmpAcc = PaymentAccount()//val tmpAcc = PaymentAccount(accountNumber)
				accountList.add(tmpAcc)
			}
			accountList
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,e.toString())
			null
		}
		return accountsList
	}
}
