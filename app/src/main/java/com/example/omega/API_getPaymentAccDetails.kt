package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class API_getPaymentAccDetails {
	companion object{
		fun run(activity: Activity,accNumber: String? = null): Boolean {
			val getDetailsOfOnlyOneAcc = accNumber != null
			if(getDetailsOfOnlyOneAcc){
				var isSuccess = false
				val thread = Thread{
					try {
						isSuccess = getAccInfo(accNumber!!)
					}catch (e: Exception) {
						Log.e("WookieTag","Failed to obtain information for account with nummber[${accNumber}] [${e.toString()}]")
					}
				}
				thread.start()
				thread.join(ApiFuncs.requestTimeOut)
				return isSuccess
			}
			else{
				try {
					var listOfThreadCheckingAccInfro = arrayListOf<Thread>()
					val amountOfAccToCheck = UserData.accessTokenStruct?.listOfAccounts!!.size
					for (i in 0 until amountOfAccToCheck){
						val accNumber = UserData.accessTokenStruct?.listOfAccounts!!.get(i).accNumber!!
						val thread = Thread {
							val success = getAccInfo(accNumber)
						}
						listOfThreadCheckingAccInfro.add(thread)
					}
					for (i in 0 until  listOfThreadCheckingAccInfro.size)
						listOfThreadCheckingAccInfro[i].start()
					for(i in 0 until  listOfThreadCheckingAccInfro.size)
						listOfThreadCheckingAccInfro[i].join(ApiFuncs.requestTimeOut)
					return true

				}catch (e: Exception) {
					Log.e("WookieTag","Failed to obtain information for at account with nummber[${accNumber}] [${e.toString()}]")
					return false
				}
			}

		}
		private fun getPaymentAccDetailsRequest(accNumber: String) : Request {
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccount"
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
				.put("accountNumber", accNumber)

			val additionalHeaderList = arrayListOf<Pair<String,String>>(Pair("AUTHORIZATION",authFieldValue))
			return ApiFuncs.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
		}
		private fun getAccInfo(accNumber: String) : Boolean{
			val request = getPaymentAccDetailsRequest(accNumber)
			val response = OkHttpClient().newCall(request).execute()
			val responseCodeOk = response.code == 200
			if(!responseCodeOk){
				return false//todo
			}
			try {
				var responseBodyJson = JSONObject(response.body?.string())
				val success = parseResponseJson(responseBodyJson)
				return success
			}
			catch (e : Exception){
				Log.e("WookieTag", e.toString())
				return false
			}
		}
		private fun parseResponseJson(obj : JSONObject) : Boolean{
			val tmpPaymentAcc = UserData.PaymentAccount(obj)
			val isValid = tmpPaymentAcc.isValid()
			if(isValid){
				return UserData.accessTokenStruct?.swapPaymentAccountToFilledOne(tmpPaymentAcc)!!
			}
			return false
		}
	}
}