package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.ApiConsts.ApiReqFields.*
import java.util.*
import kotlin.Exception
import com.example.omega.ApiGetTransactionsDone.Companion.GetTransactionsDoneRequestFields.*
import com.example.omega.ApiGetTransactionsDone.Companion.GetTransactionsResponseFields.*
import kotlin.collections.ArrayList


class ApiGetTransactionsDone(activity: Activity, token: Token){
	private val token = token
	private val callerActivity = activity
	private var historyRecordsToRet : ArrayList<AccountHistoryRecord> = ArrayList()

	companion object {
		enum class GetTransactionsDoneRequestFields(val text : String){
			TransactionDateFrom("transactionDateFrom"),
			TransactionDateTo("transactionDateTo"),
			ItemIdFrom("itemIdFrom"),
			BookingDateFrom ("bookingDateFrom"),
			BookingDateTo ("bookingDateTo"),
			MinAmount("minAmount"),
			MaxAmount("maxAmount"),
			PageId("pageId"),
			PerPage("perPage"),
			Type("type")
		}
		enum class GetTransactionsResponseFields(val text : String){
			TransactionsObj("transactions"),

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
	fun run(accNumber: String, infos : TransactionsDoneAdditionalInfos = TransactionsDoneAdditionalInfos()): List<AccountHistoryRecord>? {
		Log.i(TagProduction, "GetTransactionsDone starts")
		val thread = Thread{
			try {
				val request = getRequest(accNumber, infos)
				val response = sendRequest(request) ?: return@Thread
				var sucess = parseResponseJson(response)
				if(sucess)
					Log.i(TagProduction, "GetTransactionsDone ends with success")
				else
					Log.e(TagProduction, "GetTransactionsDone ends with fail")
			}catch (e: Exception) {
				Log.e(TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
			}
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		return historyRecordsToRet.toList()
	}

	private fun getRequest(accNumber: String, infos : TransactionsDoneAdditionalInfos) : Request {
		val uuidStr = ApiFunctions.getUUID()
		val currentTimeStr = OmegaTime.getCurrentTime()

		val headersJson = JSONObject()
			.put(RequestId.text, uuidStr)
			.put(UserAgent.text, ApiFunctions.getUserAgent())
			.put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(SendDate.text, currentTimeStr)
			.put(TppId.text, ApiConsts.TTP_ID)
			.put(TokenField.text, token.getAuthFieldValue())
			.put(IsDirectPsu.text,false)
			//.put("callbackURL",ApiConsts.REDIRECT_URI)//??
			//.put("apiKey", ApiConsts.appSecret_ALIOR)//??

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
			if(responseCode != ApiConsts.responseOkCode){
				val additionalErrorMsg : String = try {
					JSONObject(response.body?.string()!!).getString("message")
				}catch (e : Exception){
					String()
				}
				val logMsg = "[getAccHistory/${this.javaClass.name}] getHistory return code error ${ApiFunctions.getErrorTextOfRequestToLog(responseCode)}, additional MSG: $additionalErrorMsg"
				Log.e(TagProduction, logMsg)
				null
			}

			JSONObject(response.body?.string()!!)
		}
		catch (e : Exception){
			val logMsg = "[getAccHistory/${this.javaClass.name}] wrong json struct \ne=$e"
			Log.e(TagProduction, logMsg)
			null
		}
	}
	private fun parseResponseJson(response : JSONObject) : Boolean{
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
	}

	//mandatory
	val fromDate : String = getDate(daysBack)
	val endDate : String = getDate()

	//additional
	val itemIdFrom : Int? = null//Int or String
	val bookingDateFrom : String? = null
	val bookingDateTo : String? = null
	val minAmount : Double? = null
	val maxAmount : Double? = null
	val pageId : Int? = null
	val perPage : Int? = null
	val type : Type? = null

	private fun getDate(daysBack : Int = 0) : String{
		val c: Calendar = Calendar.getInstance()
		c.timeInMillis -= daysBack * 24 * 60 * 60 * 1000

		var y = (c.get(Calendar.YEAR)).toString()
		var m = (c.get(Calendar.MONTH) + 1).toString()//0-11 -> 1-12
		var d = (c.get(Calendar.DAY_OF_MONTH)).toString()

		if(m.length == 1)
			m = "0${m}"
		if(d.length == 1)
			d = "0${d}"

		return "$y-$m-$d"
	}
}
class AccountHistoryRecord{
	var itemId : String? = null
	var transactionCategory : String? = null
	var amount : Double? = null
	var currency : String? = null
	var description : String? = null
	var tradeDate : String? = null
	var sender : String? = null
	var recipient : String? = null
	var bookingDate : String? = null

	constructor(jsonObj: JSONObject){
		try {
			itemId = jsonObj.getString(ItemId.text)
			transactionCategory = jsonObj.getString(TransactionCategory.text)
			amount = jsonObj.getDouble(Amount.text)
			currency = jsonObj.getString(ApiGetTransactionsDone.Companion.GetTransactionsResponseFields.Currency.text)
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
}