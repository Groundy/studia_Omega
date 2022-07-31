package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import org.json.JSONArray

class TransferData() {
	var senderAccNumber : String? = null
	var senderAccName : String? = null
	var receiverAccNumber : String? = null
	var receiverName : String? = null
	var description : String? = null
	var amount : Double? = null
	var currency : String? = null
	var executionDate : String? = null//todo new

	constructor(jsonObj : JSONObject) : this(){
		try {
			senderAccNumber = jsonObj.getString(Fields.SenderAccNumber.text)
			receiverAccNumber = jsonObj.getString(Fields.ReceiverAccNumber.text)
			receiverName = jsonObj.getString(Fields.ReceiverName.text)
			description = jsonObj.getString(Fields.Description.text)
			amount = jsonObj.getDouble(Fields.Amount.text)
			currency = jsonObj.getString(Fields.Currency.text)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] error in constructing TransferDataObj from json")
		}
	}
	constructor(serializedObject : String) : this(){
		try{
			val separator = ";;;"
			val parts = serializedObject.split(separator)
			senderAccNumber = parts[0]
			receiverAccNumber = parts[1]
			receiverName = parts[2]
			description = parts[3]
			amount = parts[4].toDouble()
			currency = parts[5]
		}
		catch (e : Exception){
			Log.e(TagProduction, "Error in creating transferDataObject from serialized data! [$e]")
		}
	}
	fun toDisplayString() : String{
		val line1 = "Nadawca:\n$senderAccNumber"
		val line2 = "Nazwa odbiorcy:\n$receiverName"
		val line3 = "Numer odbiorcy:\n$receiverAccNumber"
		val line4 = "Tytuł:\n$description"
		val line5 = "kwota:\n${amount.toString()} ${currency.toString()}"
		return "$line1\n $line2\n $line3\n $line4\n $line5"
	}
	override fun toString() : String{
		return toJsonObject().toString()
	}
	private fun toJsonObject() : JSONObject{
		return JSONObject()
			.put(Fields.SenderAccNumber.text,senderAccNumber)
			.put(Fields.ReceiverAccNumber.text,receiverAccNumber)
			.put(Fields.ReceiverName.text,receiverName)
			.put(Fields.Description.text,description)
			.put(Fields.Amount.text,amount)
			.put(Fields.Currency.text, currency)
	}
	private fun validateTransferData() : Boolean{
		val objectWrong =
			senderAccNumber.isNullOrEmpty() ||
			receiverAccNumber.isNullOrEmpty() ||
			receiverName.isNullOrEmpty() ||
			description.isNullOrEmpty() ||
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

		val descriptionOk = receiverName?.length in 5..50
		if(!descriptionOk)
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
			Description("description"),
			Amount("amount"),
			Currency("currency"),
		}
		fun developerGetTestObjWithFilledData() : TransferData{
			val testTransferData = TransferData()
			testTransferData.receiverAccNumber = "0123456789012345678901234567"
			testTransferData.receiverName = "OdbiorcaName"
			testTransferData.senderAccName = "0001112223334445556667778889"
			testTransferData.senderAccName = "Nadawca"
			testTransferData.amount = 12.34
			testTransferData.description = "zwrot pożyczki"
			testTransferData.currency = "PLN"
			return testTransferData
		}
	}


	fun getSenderNameAsJsonObjForDomesticPayment() : JSONObject{
		val str = senderAccName?: String()
		return strToJsonObj(str)
	}
	fun getReceiverNameAsJsonObjForDomesticPayment() : JSONObject{
		val str = receiverName?: String()
		return strToJsonObj(str)
	}
	private fun strToJsonObj(str : String) : JSONObject{
		//todo dodac jakies logi
		val valueFieldName = "Value"
		val jsonArray = JSONArray()
		val nameParts = str.split(" ")
		nameParts.forEach{
			if(it.isNotEmpty())
				jsonArray.put(it)
		}
		return JSONObject().put(valueFieldName, jsonArray)
	}
}