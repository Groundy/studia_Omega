package com.example.omega

import android.util.Log
import org.json.JSONObject

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
		this.senderAccNumber = jsonObj.get("senderAccNumber") as String?
		this.receiverAccNumber = jsonObj.get("receiverAccNumber") as String?
		this.receiverName = jsonObj.get("receiverName") as String?
		this.title = jsonObj.get("title") as String?
		this.amount = jsonObj.get("amount") as Double?
		this.currency = jsonObj.get("currency") as String?
	}
	override fun toString() : String{
		val line1 = "Nadawca:\n$senderAccNumber"
		val line2 = "Nazwa odbiorcy:\n$receiverName"
		val line3 = "Numer odbiorcy:\n$receiverAccNumber"
		val line4 = "Tytuł:\n$title"
		val line5 = "kwota:\n${amount.toString()} ${currency.toString()}"
		return "$line1\n $line2\n $line3\n $line4\n $line5"
	}
	fun serialize() : String?{
		if(!validateTransferData()){
			Log.e(Utilities.TagProduction, "Error in serilization transferDataObj")
			return null
		}

		val separator =";;;"
		return String()
			.plus(senderAccNumber).plus(separator)
			.plus(receiverAccNumber).plus(separator)
			.plus(receiverName).plus(separator)
			.plus(title).plus(separator)
			.plus(amount.toString()).plus(separator)
			.plus(currency).plus(separator)
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
			Log.e(Utilities.TagProduction, "Error in creating transferDataObject from serialized data! [${e.toString()}]")
		}
	}
	fun toJsonObject() : JSONObject{
		return JSONObject()
			.put("senderAccNumber",senderAccNumber)
			.put("receiverAccNumber",receiverAccNumber)
			.put("receiverName",receiverName)
			.put("title",title)
			.put("amount",amount)
			.put("currency", currency)
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

		val senderAccCorrectLength = senderAccNumber?.length == 28
		if(!senderAccCorrectLength)
			return false

		val receiverAccCorrectLength = true //todo tutaj trzeba ogarnąć troche więcej gdyż przelewy międzynardowoew mogą mieć różną długość,  https://pl.wikipedia.org/wiki/Międzynarodowy_numer_rachunku_bankowego
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

		val currencyOK = true //TODO dodać sprawdzanie czy waluta jest z zakresu dostępnych.
		if(!currencyOK)
			return false

		return true
	}
}