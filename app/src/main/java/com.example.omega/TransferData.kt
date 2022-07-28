package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class TransferData {
	var senderAccNumber : String? = null
	var receiverAccNumber : String? = null
	var receiverName : String? = null
	var title : String? = null
	var amount : Double? = null
	var currency : String? = null

	constructor(senderAccNumber : String?, receiverAccNumber : String?, receiverName : String?, title : String?, amount : Double?, currency : String?){
		this.senderAccNumber = senderAccNumber
		this.receiverAccNumber = receiverAccNumber
		this.receiverName = receiverName
		this.title = title
		this.amount = amount
		this.currency = currency
	}
	constructor(jsonObj : JSONObject){
		try {
			senderAccNumber = jsonObj.getString(Fields.SenderAccNumber.text)
			receiverAccNumber = jsonObj.getString(Fields.ReceiverAccNumber.text)
			receiverName = jsonObj.getString(Fields.ReceiverName.text)
			title = jsonObj.getString(Fields.Title.text)
			amount = jsonObj.getDouble(Fields.Amount.text)
			currency = jsonObj.getString(Fields.Currency.text)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] error in constructing TransferDataObj from json")
		}
	}
	fun toDisplayString() : String{
		val line1 = "Nadawca:\n$senderAccNumber"
		val line2 = "Nazwa odbiorcy:\n$receiverName"
		val line3 = "Numer odbiorcy:\n$receiverAccNumber"
		val line4 = "Tytuł:\n$title"
		val line5 = "kwota:\n${amount.toString()} ${currency.toString()}"
		return "$line1\n $line2\n $line3\n $line4\n $line5"
	}
	override fun toString() : String{
		return toJsonObject().toString()
	}
	constructor(serializedObject : String){
		try{
			val separator = ";;;"
			val parts = serializedObject.split(separator)
			senderAccNumber = parts[0]
			receiverAccNumber = parts[1]
			receiverName = parts[2]
			title = parts[3]
			amount = parts[4].toDouble()
			currency = parts[5]
		}
		catch (e : Exception){
			Log.e(TagProduction, "Error in creating transferDataObject from serialized data! [$e]")
		}
	}
	private fun toJsonObject() : JSONObject{
		return JSONObject()
			.put(Fields.SenderAccNumber.text,senderAccNumber)
			.put(Fields.ReceiverAccNumber.text,receiverAccNumber)
			.put(Fields.ReceiverName.text,receiverName)
			.put(Fields.Title.text,title)
			.put(Fields.Amount.text,amount)
			.put(Fields.Currency.text, currency)
	}
	private fun validateTransferData() : Boolean{
		val objectWrong =
			senderAccNumber.isNullOrEmpty() ||
			receiverAccNumber.isNullOrEmpty() ||
			receiverName.isNullOrEmpty() ||
			title.isNullOrEmpty() ||
			amount == null ||
			amount == 0.0 ||
			currency.isNullOrEmpty()
		if(objectWrong)
			return false

		val senderAccCorrectLength = senderAccNumber?.length == ApiFunctions.getLengthOfCountryBankNumberDigitsOnly() - ApiConsts.countryCodeLength
		if(!senderAccCorrectLength)
			return false

		val receiverAccCorrectLength = senderAccNumber?.length == ApiFunctions.getLengthOfCountryBankNumberDigitsOnly() - ApiConsts.countryCodeLength
		if(!receiverAccCorrectLength)
			return false

		val senderReceiverAccDiffer = senderAccNumber != receiverAccNumber
		if(!senderReceiverAccDiffer)
			return false

		val receiverNameOk = receiverName?.length in 5..50
		if(!receiverNameOk)
			return false

		val titleOk = receiverName?.length in 5..50
		if(!titleOk)
			return false

		val amountOk = amount!! > 0.0
		if(!amountOk)
			return false

		return true
	}
	companion object{
		enum class Fields(val text : String){
			SenderAccNumber("senderAccNumber"),
			ReceiverAccNumber("receiverAccNumber"),
			ReceiverName("receiverName"),
			Title("title"),
			Amount("amount"),
			Currency("currency"),
		}
	}
}