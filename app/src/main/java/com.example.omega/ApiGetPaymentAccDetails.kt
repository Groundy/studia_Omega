package com.example.omega

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class ApiGetPaymentAccDetails {
	companion object{
		fun run(accNumber: String? = null): Boolean {
			val getDetailsOfOnlyOneAcc = accNumber != null
			if(getDetailsOfOnlyOneAcc){
				var isSuccess = false
				val thread = Thread{
					try {
						isSuccess = getAccInfo(accNumber!!)
					}catch (e: Exception) {
						Log.e(Utilities.TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
					}
				}
				thread.start()
				thread.join(ApiFunctions.requestTimeOut)
				return isSuccess
			}
			else{
				try {
					val listOfThreadCheckingAccInfo = arrayListOf<Thread>()
					val amountOfAccToCheck = UserData.accessTokenStruct?.listOfAccounts!!.size
					for (i in 0 until amountOfAccToCheck){
						val accountNumber = UserData.accessTokenStruct?.listOfAccounts!![i].accNumber
						val thread = Thread {
							getAccInfo(accountNumber)
						}
						listOfThreadCheckingAccInfo.add(thread)
					}
					for (i in 0 until  listOfThreadCheckingAccInfo.size)
						listOfThreadCheckingAccInfo[i].start()
					for(i in 0 until  listOfThreadCheckingAccInfo.size)
						listOfThreadCheckingAccInfo[i].join(ApiFunctions.requestTimeOut)
					return true

				}catch (e: Exception) {
					Log.e(Utilities.TagProduction,"Failed to obtain information for at account with number[${accNumber}] [$e]")
					return false
				}
			}

		}
		private fun getPaymentAccDetailsRequest(accNumber: String) : Request {
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccount"
			val uuidStr = ApiFunctions.getUUID()
			val currentTimeStr = ApiFunctions.getCurrentTimeStr()

			val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
			val requestBodyJson = JSONObject()
				.put("requestHeader", JSONObject()
					.put("requestId", uuidStr)
					.put("userAgent", ApiFunctions.getUserAgent())
					.put("ipAddress", ApiFunctions.getPublicIPByInternetService())
					.put("sendDate", currentTimeStr)
					.put("tppId", ApiConsts.TTP_ID)
					.put("token", authFieldValue)
					.put("isDirectPsu", false)
					.put("directPsu", false)
				)
				.put("accountNumber", accNumber)

			val additionalHeaderList = arrayListOf(Pair("AUTHORIZATION",authFieldValue))
			return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
		}
		private fun getAccInfo(accNumber: String) : Boolean{
			val request = getPaymentAccDetailsRequest(accNumber)
			val response = OkHttpClient().newCall(request).execute()
			val responseCodeOk = response.code == 200
			if(!responseCodeOk)
				return false//todo

			return try {
				val responseBodyJson = JSONObject(response.body?.string()!!)
				parseResponseJson(responseBodyJson)
			}
			catch (e : Exception){
				Log.e(Utilities.TagProduction, e.toString())
				false
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