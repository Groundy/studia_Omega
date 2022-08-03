package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class TransferData() {
	private companion object{
		enum class TransferDataFields(val text : String){
			SenderAccNumber("senderAccNumber"),
			SenderAccName("senderAccName"),
			ReceiverAccNumber("receiverAccNumber"),
			ReceiverName("receiverName"),
			Description("description"),
			Amount("amount"),
			Currency("currency"),
			ExecutionDate("executionDate"),

			Recipient("recipient"),
			Sender("sender"),
			TransferData ("transferData"),
			Value("value"),
			NameAdress("nameAddress"),
			AccountNumber("accountNumber");
		}
		const val methodeName = "pis:domestic"
	}

	var senderAccNumber : String? = null
	var senderAccName : String? = null
	var receiverAccNumber : String? = null
	var receiverName : String? = null
	var description : String? = null
	var amount : Double? = null
	var currency : String? = null
	var executionDate : String? = null

	constructor(jsonObj : JSONObject) : this(){
		try {
			senderAccNumber = jsonObj.getString(TransferDataFields.SenderAccNumber.text)
			senderAccName = jsonObj.getString(TransferDataFields.SenderAccName.text)
			receiverAccNumber = jsonObj.getString(TransferDataFields.ReceiverAccNumber.text)
			receiverName = jsonObj.getString(TransferDataFields.ReceiverName.text)
			description = jsonObj.getString(TransferDataFields.Description.text)
			amount = jsonObj.getDouble(TransferDataFields.Amount.text)
			currency = jsonObj.getString(TransferDataFields.Currency.text)
			executionDate = jsonObj.getString(TransferDataFields.ExecutionDate.text)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] error in constructing TransferDataObj from json")
		}
	}
	constructor(serializedObject : String) : this(){
		try{
			val obj = JSONObject(serializedObject)
			val tmpTransferData = TransferData(obj)
			senderAccNumber = tmpTransferData.senderAccNumber
			senderAccName = tmpTransferData.senderAccName
			receiverAccNumber = tmpTransferData.receiverAccNumber
			receiverName = tmpTransferData.receiverName
			description = tmpTransferData.description
			amount = tmpTransferData.amount
			currency = tmpTransferData.currency
			executionDate = tmpTransferData.executionDate
		}
		catch (e : Exception){
			Log.e(TagProduction, "Error in creating transferDataObject from serialized data! [$e]")
		}
	}
	constructor(paymentToken : Token): this(){
		try {
			val privilegesList = paymentToken.getPriligeList()
			val methodeObj = privilegesList!!.getJSONObject(0).getJSONObject(methodeName)
			val subDataObj = methodeObj.getJSONObject(TransferDataFields.TransferData.text)
			val senderObj = methodeObj.getJSONObject(TransferDataFields.Sender.text)
			val receiverObj = methodeObj.getJSONObject(TransferDataFields.Recipient.text)

			val senderNameArray = senderObj.getJSONObject(TransferDataFields.NameAdress.text).getJSONArray(TransferDataFields.Value.text)
			val receiverNameArray = receiverObj.getJSONObject(TransferDataFields.NameAdress.text).getJSONArray(TransferDataFields.Value.text)

			var senderNameTmp = String()
			var receiverNameTmp = String()

			val separator = ", "
			for (i in 0 until senderNameArray.length()){
				senderNameTmp = senderNameTmp.plus(senderNameArray[i])
				if(i < senderNameArray.length() - 1)
					senderNameTmp = senderNameTmp.plus(separator)
			}
			for (i in 0 until receiverNameArray.length()){
				receiverNameTmp = receiverNameTmp.plus(receiverNameArray[i])
				if(i < senderNameArray.length() - 1)
					receiverNameTmp = receiverNameTmp.plus(separator)
			}


			senderAccNumber = senderObj.getString(TransferDataFields.AccountNumber.text)
			senderAccName = senderNameTmp
			receiverAccNumber = receiverObj.getString(TransferDataFields.AccountNumber.text)
			receiverName = receiverNameTmp
			description = subDataObj.getString(TransferDataFields.ExecutionDate.text)
			amount = subDataObj.getString(TransferDataFields.Amount.text).toDouble()
			currency = subDataObj.getString(TransferDataFields.Currency.text)
			executionDate = subDataObj.getString(TransferDataFields.ExecutionDate.text)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(token)/${this.javaClass.name}] failed to obtain tmp data transfer class from payment token")
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
			.put(TransferDataFields.SenderAccNumber.text, senderAccNumber?: String())
			.put(TransferDataFields.SenderAccName.text, senderAccName?: String())
			.put(TransferDataFields.ReceiverAccNumber.text, receiverAccNumber)
			.put(TransferDataFields.ReceiverName.text, receiverName)
			.put(TransferDataFields.Description.text, description)
			.put(TransferDataFields.Amount.text, amount)
			.put(TransferDataFields.Currency.text, currency)
			.put(TransferDataFields.ExecutionDate.text, executionDate)

	}
	private fun validateTransferData() : Boolean{
		//todo review
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
}