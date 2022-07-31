package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import com.example.omega.ApiConsts.ApiReqFields.*
import okhttp3.OkHttpClient
import org.json.JSONArray

class ApiDomesticPayment(activity: Activity, token: Token, transferData: TransferData) {
	private val callerActivity = activity
	private val token = token
	private val transferData = transferData
	private companion object{
		enum class RequestFields(val text : String){
			Recipient("recipient"),
			Sender("sender"),
			TransferData ("transferData"),
			TppTransactionId("tppTransactionId"),//: "358636580765696",
			DeliveryMode("deliveryMode"),//: "ExpressD0",
			System("system"),//: "Elixir",
			Hold("hold"),//: true,
			ExecutionMode("executionMode"),//: "FutureDated",
			SplitPayment("splitPayment"),//: false,
			TransactionInfoSpObj("transactionInfoSp"),

			AccountNumber("accountNumber"),
			NameAddressArray("nameAddress"),
			Value("Value")
		}
		enum class TransactionsObjFields(val text: String){
			Description("description"),
			Amount("amount"),
			ExecutionDate("executionDate"),
			Currency("currency")
		}
		enum class TransactionsExtraInfoObjFields(val text: String){
			SpInvoiceNumber("spInvoiceNumber"),//: "5845456248635392",
			SpTaxIdentificationNumber("spTaxIdentificationNumber"),//: "6976149825519616",
			SpTaxAmount("spTaxAmount"),//: "6864360880209920",
			SpDescription("spDescription"),//: "Epo da jewbe in jimtafo miwjon ra izo on lu kuwimwen zi ijufebo ki hodaraz masehow jehti."
		}
	}

	fun run() : Boolean{
		Log.i(Utilities.TagProduction, "Domestic Payement started")
		var success = false
		val thread = Thread{
			val request = getRequest()
			val okResponse = sendRequest(request) ?: return@Thread
			success = handleResponse(okResponse)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(success)
			Log.i(Utilities.TagProduction, "Domestic Payement ended with sucess")
		else
			Log.e(Utilities.TagProduction, "Domestic Payement ended with error")
		return success
	}
	private fun getRequest() : Request{
		val uuidStr = ApiFunctions.getUUID()
		val currentTime = OmegaTime.getCurrentTime()
		val authFieldValue = token.getAuthFieldValue()

		val requestHeaders = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTime)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, authFieldValue)
			//.put("callbackURL","http://uwagihuka.gg/poh")
			//.put("apiKey","ezvozsiwoltoz")


		val recpientJsonObj = JSONObject()
			recpientJsonObj.put(RequestFields.AccountNumber.text, transferData.receiverName)
			recpientJsonObj.put(RequestFields.NameAddressArray.text, transferData.getReceiverNameAsJsonObjForDomesticPayment())


		val senderJsonObj = JSONObject()
			senderJsonObj.put(RequestFields.AccountNumber.text, transferData.senderAccNumber)
			senderJsonObj.put(RequestFields.NameAddressArray.text, transferData.getSenderNameAsJsonObjForDomesticPayment())

		val transferDataJsonObj = JSONObject()
			transferDataJsonObj.put(TransactionsObjFields.Description.text, transferData.description)
			transferDataJsonObj.put(TransactionsObjFields.Amount.text, transferData.amount)
			transferDataJsonObj.put(TransactionsObjFields.ExecutionDate.text, transferData.executionDate)
			transferDataJsonObj.put(TransactionsObjFields.Currency.text, transferData.currency)


		val transferDataExtraInfoJsonObj = JSONObject()
			.put(TransactionsExtraInfoObjFields.SpInvoiceNumber.text, "spInvoiceNumber")
			.put(TransactionsExtraInfoObjFields.SpTaxIdentificationNumber.text, "6976149825519616")
			.put(TransactionsExtraInfoObjFields.SpTaxAmount.text, "6864360880209920")
			.put(TransactionsExtraInfoObjFields.SpDescription.text, "Epo da jewbe in jimtafo miwjon ra izo on lu kuwimwen zi ijufebo ki hodaraz masehow jehti.")


		val requestBodyJsonObj = JSONObject()
			requestBodyJsonObj.put(RequestHeader.text, requestHeaders)
			requestBodyJsonObj.put(RequestFields.Recipient.text, recpientJsonObj)
			requestBodyJsonObj.put(RequestFields.Sender.text, senderJsonObj)
			requestBodyJsonObj.put(RequestFields.TransferData.text, transferDataJsonObj)
			recpientJsonObj.put(RequestFields.TppTransactionId.text, "358636580765696")
			recpientJsonObj.put(RequestFields.DeliveryMode.text,"ExpressD0")
			recpientJsonObj.put(RequestFields.System.text,"Elixir")
			recpientJsonObj.put(RequestFields.Hold.text,true)
			recpientJsonObj.put(RequestFields.ExecutionMode.text,"FutureDated")
			recpientJsonObj.put(RequestFields.SplitPayment.text,false)
			recpientJsonObj.put(RequestFields.TransactionInfoSpObj.text,transferDataExtraInfoJsonObj)

		val additionalHeaderList = arrayListOf(Pair(Authorization.text, authFieldValue))
		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.SinglePayment, requestBodyJsonObj, uuidStr, additionalHeaderList)
	}
	private fun sendRequest(request : Request) : JSONObject?{
		return try{
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ApiConsts.responseOkCode){
				ApiFunctions.LogResponseError(response, this.javaClass.name)
				null
			}
			val responseBody = response.body?.string()
			val responseJsonObject = JSONObject(responseBody!!)
			responseJsonObject
		}catch (e : Exception){
			Log.e(Utilities.TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			null
		}
	}
	private fun handleResponse(response: JSONObject) : Boolean{
		return false
	}
}