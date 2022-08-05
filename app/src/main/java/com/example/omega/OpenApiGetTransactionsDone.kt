package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*
import kotlin.Exception
import com.example.omega.ApiGetTransactionsDone.Companion.GetTransDoneRequestFields.*
import com.example.omega.TransactionsDoneAdditionalInfos.Companion.GetTransDoneResponseFields.*
import java.time.Instant
import kotlin.collections.ArrayList

class ApiGetTransactionsDone(private val callerActivity: Activity, private val  token: Token){
	private var historyRecordsToRet : ArrayList<AccountHistoryRecord> = ArrayList()

	private companion object {
		enum class GetTransDoneRequestFields(val text : String){
			TransactionDateFrom("transactionDateFrom"),
			TransactionDateTo("transactionDateTo"),
			ItemIdFrom("itemIdFrom"),
			BookingDateFrom ("bookingDateFrom"),
			BookingDateTo ("bookingDateTo"),
			MinAmount("minAmount"),
			MaxAmount("maxAmount"),
			PageId("pageId"),
			PerPage("perPage"),
			Type("type"),
			TransactionsObj("transactions"),
		}

	}
	fun run(accNumber: String, infos : TransactionsDoneAdditionalInfos = TransactionsDoneAdditionalInfos()): List<AccountHistoryRecord>? {
		Log.i(TagProduction, "GetTransactionsDone starts")
		var sucess = false
		try {
			val request = getRequest(accNumber, infos)
			val response = sendRequest(request)
			if(response != null)
				sucess = handleCorretResponse(response)
		}catch (e: Exception) {
			Log.e(TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
		}


		return if(sucess && historyRecordsToRet.isNotEmpty()){
			Log.i(TagProduction, "GetTransactionsDone ends with success, returned ${historyRecordsToRet.size} elements")
			historyRecordsToRet.toList()
		}
		else if(sucess && historyRecordsToRet.isEmpty()){
			Log.w(TagProduction, "GetTransactionsDone ends with success but list of transcations is empty")
			historyRecordsToRet.toList()
		}
		else{
			Log.e(TagProduction, "GetTransactionsDone ends with fail")
			null
		}
	}
	private fun getRequest(accNumber: String, infos : TransactionsDoneAdditionalInfos) : Request {
		val uuidStr = ApiFunctions.getUUID()

		val headersJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, OmegaTime.getCurrentTime())
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, token.getAuthFieldValue())
			.put(IsDirectPsu.text,false)

		val requestBodyJson = JSONObject()
			.put(RequestHeader.text, headersJson)
			.put(AccountNumberField.text, accNumber)
			.put(TransactionDateFrom.text,infos.fromDate)
			.put(TransactionDateTo.text,infos.endDate)


		val additionalHeaderList = arrayListOf(Pair(Authorization.text,token.getAuthFieldValue()))
		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTransactionsDone, requestBodyJson, uuidStr, additionalHeaderList)
	}
	private fun sendRequest(request: Request) : JSONObject?{
		return try {
			val response = OkHttpClient().newCall(request).execute()
			val responseCode = response.code
			if(responseCode != ApiConsts.ResponseCodes.OK.code){
				if(responseCode == ApiConsts.ResponseCodes.LimitExceeded.code){
					val msgForUser = callerActivity.getString(R.string.AccHistoryAct_UserMsg_TooManyResuest)
					Utilities.showToast(callerActivity, msgForUser)
				}
				ApiFunctions.logResponseError(response, this.javaClass.name)
				return null
			}
			JSONObject(response.body?.string()!!)
		}
		catch (e : Exception){
			val logMsg = "[sendRequest/${this.javaClass.name}] could not get proper response from server \ne=$e"
			Log.e(TagProduction, logMsg)
			null
		}
	}
	private fun handleCorretResponse(response : JSONObject) : Boolean{
		return try {
			val transactionsObj = response.getJSONArray(TransactionsObj.text)
			for (i in 0 until transactionsObj.length()){
				val toAdd = AccountHistoryRecord(transactionsObj.getJSONObject(i))
				historyRecordsToRet.add(toAdd)
			}
			true
		}catch (e : Exception){
			Log.e(TagProduction, "[parseResponseJson/${this.javaClass.name}] unkown error in parsing json response")
			false
		}
	}
}



class TransactionsDoneAdditionalInfos(daysBack : Int = 5){
	companion object{
		private enum class Type(val text: String){
			DEBIT("DEBIT"), CREDIT("CREDIT")
		}
		private enum class JsonFields(val text: String){
			DateFromInclusvie("DateFromInclusvie"),
			DateToInclusvie("DateToInclusvie"),
			AmountMin("AmountMin"),
			AmountMax("AmountMax"),
		}
		enum class GetTransDoneResponseFields(val text : String){
			ItemId ("itemId"),
			TransactionCategory ("transactionCategory"),
			Amount ("amount"),
			Currency ("currency"),
			Description ("description"),
			TradeDate ("tradeDate"),
			Sender ("sender"),
			Recipient ("recipient"),
			BookingDate ("bookingDate"),
			NameAdress("nameAddress"),
			Value("value");

		}
	}

	//mandatory
	var fromDate : String = OmegaTime.getDate(daysBack)
	var endDate : String = OmegaTime.getDate()

	//additional
	//val itemIdFrom : Int? = null//Int or String
	//val bookingDateFrom : String? = null
	//val bookingDateTo : String? = null
	private var minAmount : Double? = null
	private var maxAmount : Double? = null
	//val pageId : Int? = null
	//val perPage : Int? = null
	//val type : Type? = null

	fun toJSONObject() : JSONObject{
		return JSONObject()
			.put(JsonFields.DateFromInclusvie.text,"")
			.put(JsonFields.DateToInclusvie.text,"")
			.put(JsonFields.AmountMin.text,"")
			.put(JsonFields.AmountMax.text,"")
	}
	constructor(jsonObj : JSONObject) : this(){
		try {
			minAmount = jsonObj.getDouble(JsonFields.AmountMin.text)
			maxAmount = jsonObj.getDouble(JsonFields.AmountMax.text)
			fromDate = jsonObj.getString(JsonFields.DateFromInclusvie.text)
			endDate = jsonObj.getString(JsonFields.DateToInclusvie.text)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] Error in parsing TransactiondDoneInfo from json obj")
		}
	}
}


class AccountHistoryRecord(jsonObj: JSONObject) : Comparable<AccountHistoryRecord> {
	var senderAccNumber : String? = null
	var senderName : String? = null
	var recipientAccNumber : String? = null
	var recipientName : String? = null
	var amount : Double? = null
	var currency : String? = null
	var description : String? = null
	var tradeDate : String? = null
	init {
		try {
			val senderObj = jsonObj.getJSONObject(Sender.text)
			val recipientObj = jsonObj.getJSONObject(Recipient.text)

			amount = jsonObj.getString(Amount.text).toDouble()
			currency = jsonObj.getString(Currency.text)
			description = jsonObj.getString(Description.text)
			tradeDate = jsonObj.getString(TradeDate.text)
			senderAccNumber = senderObj.getString(AccountNumberField.text)
			recipientAccNumber = recipientObj.getString(AccountNumberField.text)

			val senderNameArr = senderObj.getJSONObject(NameAdress.text).getJSONArray(Value.text)
			val recipientNameArr = recipientObj.getJSONObject(NameAdress.text).getJSONArray(Value.text)

			recipientName = if(recipientNameArr.length() > 0)
				recipientNameArr[0].toString()
			else
				"Unknown"

			senderName = if(senderNameArr.length() > 0)
				senderNameArr[0].toString()
			else
				"Unknown"
		}
		catch (e : Exception){
			Log.e(TagProduction, "[constructor/${this.javaClass.name}] Cant parse from Json")
		}
	}

	override fun compareTo(other: AccountHistoryRecord): Int {
		val currentSecSinceEpoch = OmegaTime.converTimeStampToEpoch(tradeDate)
		val otherSecSinceEpoch = OmegaTime.converTimeStampToEpoch(other.tradeDate)

		//sortowanie od najmłodszych wpisów do najstarszych
		return (otherSecSinceEpoch - currentSecSinceEpoch).toInt()
	}
}