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
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

enum class GetTransDoneResponseFields(val text : String){
	Amount ("amount"),
	Currency ("currency"),
	Description ("description"),
	TradeDate ("tradeDate"),
	Sender ("sender"),
	Recipient ("recipient"),
	NameAdress("nameAddress"),
	Value("value");
}

class ApiGetTransactionsDone(private val callerActivity: Activity, private val  token: Token){
	private var historyRecordsToRet : ArrayList<AccountHistoryRecord> = ArrayList()

	private companion object {
		enum class GetTransDoneRequestFields(val text : String){
			TransactionDateFrom("transactionDateFrom"),
			TransactionDateTo("transactionDateTo"),
			MinAmount("minAmount"),
			MaxAmount("maxAmount"),
			TransactionsObj("transactions"),
		}

	}

	suspend fun run(accNumber: String, infos : HistoryFillters): List<AccountHistoryRecord>? {
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
	private fun getRequest(accNumber: String, infos : HistoryFillters) : Request {
		val uuidStr = ApiFunctions.getUUID()

		val bodyHeaders = JSONObject()
		with(bodyHeaders){
			put(RequestId.text, uuidStr)
			put(UserAgent.text, ApiFunctions.getUserAgent())
			put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			put(SendDate.text, OmegaTime.getCurrentTime())
			put(TppId.text, ApiConsts.TTP_ID)
			put(TokenField.text, token.getAuthFieldValue())
			put(IsDirectPsu.text,false)
		}

		val body = JSONObject()
		with(body){
			put(RequestHeader.text, bodyHeaders)
			put(AccountNumberField.text, accNumber)
			put(TransactionDateFrom.text,infos.getStartDateForRequest())
			put(TransactionDateTo.text,infos.getEndDateForRequest())

			val minAmount = infos.getMinAmountForRequest()
			if(minAmount != null)
				put(MinAmount.text, minAmount)

			val maxAmount = infos.getMaxAmountForRequest()
			if(maxAmount != null)
				put(MaxAmount.text, maxAmount)
		}

		val additionalHeaderList = arrayListOf(Pair(Authorization.text,token.getAuthFieldValue()))
		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.GetTransactionsDone, body, uuidStr, additionalHeaderList)
	}
	private suspend fun sendRequest(request: Request) : JSONObject?{
		return try {
			val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
			val response = client.newCall(request).execute()
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

class HistoryFillters(daysBack : Int = 7){
	//mandatory
	private var fromDate : String = OmegaTime.getDate(daysBack,false)
	private var endDate : String = OmegaTime.getDate(0,false)

	//additional
	private var minAmount : Double? = null
	private var maxAmount : Double? = null

	constructor(amountMin: Double, amountMax: Double, dateFrom: String, dateTo: String) : this(){
		this.maxAmount = amountMax
		this.minAmount = amountMin
		this.fromDate = dateFrom
		this.endDate = dateTo
	}

	fun getEndDateForRequest() : String{
		return reversdateToYearsFirst(endDate)
	}
	fun getStartDateForRequest() : String{
		return reversdateToYearsFirst(fromDate)
	}
	fun getEndDateForDisplay() : String{
		return endDate
	}
	fun getStartDateForDisplay() : String{
		return fromDate
	}

	fun getMinAmountForRequest() : String?{
		if(minAmount == null)
			return null

		if(minAmount == 0.0)
			return null

		return Utilities.doubleToTwoDigitsAfterCommaString(minAmount)
	}
	fun getMaxAmountForRequest() : String?{
		if(maxAmount == null)
			return null

		if(maxAmount == 0.0)
			return null

		return Utilities.doubleToTwoDigitsAfterCommaString(maxAmount)
	}
	fun getMinAmountForDisplay() : String{
		if(minAmount == null)
			return String()

		if(minAmount == 0.0)
			return String()

		return minAmount.toString()
	}
	fun getMaxAmountForDisplay() : String?{
		if(maxAmount == null)
			return String()

		if(maxAmount == 0.0)
			return String()

		return maxAmount.toString()
	}
	fun amountIsInFillterRange(amountToCheck : Double?) : Boolean{
		if(amountToCheck == null)
			return false

		if(minAmount != null){
			if(amountToCheck < minAmount!!)
				return false
		}

		if(maxAmount != null){
			if(amountToCheck > maxAmount!!)
				return false
		}

		return true
	}
	private fun reversdateToYearsFirst(date : String) : String{
		return try {
			val parts = date.split("-")
			val dd = parts[0]
			val mm = parts[1]
			val yyyy = parts[2]
			"$yyyy-$mm-$dd"
		} catch (e : Exception){
			Log.e(TagProduction, "[reversdateToYearsFirst/${this.javaClass.name}] input=$date")
			String()
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
			val senderObj = jsonObj.getJSONObject(GetTransDoneResponseFields.Sender.text)
			val recipientObj = jsonObj.getJSONObject(GetTransDoneResponseFields.Recipient.text)

			amount = jsonObj.getString(GetTransDoneResponseFields.Amount.text).toDouble()
			currency = jsonObj.getString(GetTransDoneResponseFields.Currency.text)
			description = jsonObj.getString(GetTransDoneResponseFields.Description.text)
			tradeDate = jsonObj.getString(GetTransDoneResponseFields.TradeDate.text)
			senderAccNumber = senderObj.getString(AccountNumberField.text)
			recipientAccNumber = recipientObj.getString(AccountNumberField.text)

			val senderNameArr = senderObj.getJSONObject(GetTransDoneResponseFields.NameAdress.text).getJSONArray(GetTransDoneResponseFields.Value.text)
			val recipientNameArr = recipientObj.getJSONObject(GetTransDoneResponseFields.NameAdress.text).getJSONArray(GetTransDoneResponseFields.Value.text)

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