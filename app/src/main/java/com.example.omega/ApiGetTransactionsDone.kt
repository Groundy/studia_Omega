package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ScopeFields.*
import com.example.omega.ApiConsts.ApiReqFields.*

class ApiGetTransactionsDone {
	var token: Token
	var callerActivity: Activity
	constructor(token: Token, activity: Activity){
		this.token = token
		this.callerActivity = activity
	}
	fun run(activity: Activity, accNumber: String? = null): Boolean {
			val getDetailsOfOnlyOneAcc = accNumber != null
			if(getDetailsOfOnlyOneAcc){
				var isSuccess = false
				val thread = Thread{
					try {
						isSuccess = getAccHistory(accNumber!!)
					}catch (e: Exception) {
						Log.e(TagProduction,"Failed to obtain information for account with number[${accNumber}] [${e.toString()}]")
					}
				}
				thread.start()
				thread.join(ApiConsts.requestTimeOut)
				return isSuccess
			}
			else{//todo
			/*
								try {
					var listOfThreadCheckingAccInfo = arrayListOf<Thread>()
					val listOfAccounts = token.getListOfAccounts()
					if(listOfAccounts == null)
					;//todo
					val amountOfAccToCheck = listOfAccounts.size
					for (i in 0 until amountOfAccToCheck){
						val accNumber = listOfAccounts[i].getAccountNumber()
						val thread = Thread {
							val success = getAccHistory(accNumber)
						}
						listOfThreadCheckingAccInfo.add(thread)
					}
					for (i in 0 until  listOfThreadCheckingAccInfo.size)
						listOfThreadCheckingAccInfo[i].start()
					for(i in 0 until  listOfThreadCheckingAccInfo.size)
						listOfThreadCheckingAccInfo[i].join(ApiConsts.requestTimeOut)
					return true

				}catch (e: Exception) {
					Log.e(TagProduction,"Failed to obtain information for at account with nummber[${accNumber}] [${e.toString()}]")
					return false
				}
			*/
				return false
			}
		}
	private fun getPaymentAccHistoryRequest(accNumber: String) : Request {
			val uuidStr = ApiFunctions.getUUID()
			val currentTimeStr = OmegaTime.getCurrentTime()

			val requestBodyJson = JSONObject()
				.put(RequestHeader.text, JSONObject()
					.put(RequestId.text, uuidStr)
					.put(UserAgent.text, ApiFunctions.getUserAgent())
					.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
					.put(SendDate.text, currentTimeStr)
					.put(TppId.text, ApiConsts.TTP_ID)
					.put(TokenField.text, token.getAccessToken())
					.put(IsDirectPsu.text,false)
					//.put("callbackURL",ApiConsts.REDIRECT_URI)//??
					//.put("apiKey", ApiConsts.appSecret_ALIOR)//??
				)
				.put(AccountNumberField.text, accNumber)
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

			val additionalHeaderList = arrayListOf(Pair(Authorization.text,token.getAuthFieldValue()))
			return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTransactionsDone.text, requestBodyJson, uuidStr, additionalHeaderList)
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
				Log.e(TagProduction, e.toString())
				return false
			}
		}
	private fun parseResponseJson(obj : JSONObject) : Boolean{
			//todo
			return false
		}
}
