package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class API_getTransactionsDone {

	companion object{
		fun run(activity: Activity, accNumber: String? = null): Boolean {
			val getDetailsOfOnlyOneAcc = accNumber != null
			if(getDetailsOfOnlyOneAcc){
				var isSuccess = false
				val thread = Thread{
					try {
						isSuccess = getAccHistory(accNumber!!)
					}catch (e: Exception) {
						Log.e(Utilites.TagProduction,"Failed to obtain information for account with nummber[${accNumber}] [${e.toString()}]")
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
							val success = getAccHistory(accNumber)
						}
						listOfThreadCheckingAccInfro.add(thread)
					}
					for (i in 0 until  listOfThreadCheckingAccInfro.size)
						listOfThreadCheckingAccInfro[i].start()
					for(i in 0 until  listOfThreadCheckingAccInfro.size)
						listOfThreadCheckingAccInfro[i].join(ApiFuncs.requestTimeOut)
					return true

				}catch (e: Exception) {
					Log.e(Utilites.TagProduction,"Failed to obtain information for at account with nummber[${accNumber}] [${e.toString()}]")
					return false
				}
			}

		}
		private fun getPaymentAccHistoryRequest(accNumber: String) : Request {
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getTransactionsDone"
			val uuidStr = ApiFuncs.getUUID()
			val currentTimeStr = ApiFuncs.getCurrentTimeStr()


			val requestBodyJson = JSONObject()
				.put("requestHeader", JSONObject()
					.put("requestId", uuidStr)
					.put("userAgent", ApiFuncs.getUserAgent())
					.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
					.put("sendDate", currentTimeStr)
					.put("tppId", ApiConsts.TTP_ID)
					.put("token", UserData.accessTokenStruct?.tokenContent)
					.put("isDirectPsu",false)
					//.put("callbackURL",ApiConsts.REDIRECT_URI)//??
					//.put("apiKey", ApiConsts.appSecret_ALIOR)//??
				)
				.put("accountNumber", accNumber)
				//.put("itemIdFrom","5989073072160768")//??
				//.put("transactionDateFrom","Thu Apr 30")//??
				//.put("transactionDateTo","Thu Feb 06")//??
				//.put("bookingDateFrom","Thu Feb 03")//??
				//.put("bookingDateTo","Mon Feb 03")//??
				//.put("minAmount","0.0")//??
				//.put("maxAmount","99999.99")//??
				//.put("pageId","1")//??
				//.put("perPage",10)//??
				.put("type","DEBIT")//??

			val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
			val additionalHeaderList = arrayListOf(Pair("authorization",authFieldValue))
			return ApiFuncs.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
		}
		private fun getAccHistory(accNumber: String) : Boolean{
			val request = getPaymentAccHistoryRequest(accNumber)
			val response = OkHttpClient().newCall(request).execute()
			val responseCodeOk = response.code == 200
			if(!responseCodeOk){
				return false//todo
			}
			try {
				var responseBodyJson = JSONObject(response.body?.string())
				return parseResponseJson(responseBodyJson)
			}
			catch (e : Exception){
				Log.e(Utilites.TagProduction, e.toString())
				return false
			}
		}
		private fun parseResponseJson(obj : JSONObject) : Boolean{
			//todo
			return false
		}
	}
}