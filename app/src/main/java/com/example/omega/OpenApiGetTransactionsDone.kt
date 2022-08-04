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
		val thread = Thread{
			try {
				val request = getRequest(accNumber, infos)
				val response = sendRequest(request) ?: return@Thread
				sucess = handleResponse(response)
			}catch (e: Exception) {
				Log.e(TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)

		return if(sucess && historyRecordsToRet.isNotEmpty()){
			Log.i(TagProduction, "GetTransactionsDone ends with success")
			historyRecordsToRet.toList()
		}
		else if(sucess && historyRecordsToRet.isEmpty()){
			Log.i(TagProduction, "GetTransactionsDone ends with success but list of transcations is empty")
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

		if(infos.itemIdFrom != null)
			requestBodyJson.put(ItemIdFrom.text, infos.itemIdFrom.toString())

		if(infos.bookingDateFrom != null)
			requestBodyJson.put(BookingDateFrom.text, infos.bookingDateFrom)

		if(infos.itemIdFrom != null)
			requestBodyJson.put(BookingDateTo.text, infos.bookingDateTo)

		if(infos.itemIdFrom != null)
			requestBodyJson.put(MinAmount.text, infos.minAmount.toString())

		if(infos.itemIdFrom != null)
			requestBodyJson.put(MaxAmount.text, infos.maxAmount.toString())

		if(infos.itemIdFrom != null)
			requestBodyJson.put(PageId.text, infos.pageId.toString())

		if(infos.itemIdFrom != null)
			requestBodyJson.put(PerPage.text, infos.perPage.toString())

		if(infos.itemIdFrom != null)
			requestBodyJson.put(Type.text, infos.type!!.text)

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
	private fun handleResponse(response : JSONObject) : Boolean{
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
		enum class Type(val text: String){
			DEBIT("DEBIT"), CREDIT("CREDIT")
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
		}
	}

	//mandatory
	val fromDate : String = OmegaTime.getDate(daysBack)
	val endDate : String = OmegaTime.getDate()

	//additional
	val itemIdFrom : Int? = null//Int or String
	val bookingDateFrom : String? = null
	val bookingDateTo : String? = null
	val minAmount : Double? = null
	val maxAmount : Double? = null
	val pageId : Int? = null
	val perPage : Int? = null
	val type : Type? = null
}


class AccountHistoryRecord(jsonObj: JSONObject) {
	private var itemId : String? = null
	private var transactionCategory : String? = null
	private var amount : Double? = null
	private var currency : String? = null
	private var description : String? = null
	private var tradeDate : String? = null
	private var sender : String? = null
	private var recipient : String? = null
	private var bookingDate : String? = null

	init {
		try {
			itemId = jsonObj.getString(ItemId.text)
			transactionCategory = jsonObj.getString(TransactionCategory.text)
			amount = jsonObj.getDouble(Amount.text)
			currency = jsonObj.getString(Currency.text)
			description = jsonObj.getString(Description.text)
			tradeDate = jsonObj.getString(TradeDate.text)
			sender = jsonObj.getString(Sender.text)
			recipient = jsonObj.getString(Recipient.text)
			bookingDate = jsonObj.getString(BookingDate.text)
		}
		catch (e : Exception){
			Log.e(TagProduction, "[constructor/${this.javaClass.name}] Cant parse from Json")
		}
	}

	override fun toString(): String {
		return "${bookingDate?.subSequence(0,11)}--$amount--$description"//todo implement
	}
}