package com.example.omega

import org.json.JSONObject

class TransferData {
	var senderAccNumber : String? = ""
	var receiverAccNumber : String? = ""
	var receiverName : String? = ""
	var title : String? = ""
	var amount : Double? = 0.0

	constructor(senderAccNumber : String?, receiverAccNumber : String?, receiverName : String?, title : String?, amount : Double?){
		this.senderAccNumber = senderAccNumber
		this.receiverAccNumber = receiverAccNumber
		this.receiverName = receiverName
		this.title = title
		this.amount = amount
	}
	constructor(jsonObj : JSONObject){
		this.senderAccNumber = jsonObj.get("senderAccNumber") as String?
		this.receiverAccNumber = jsonObj.get("receiverAccNumber") as String?
		this.receiverName = jsonObj.get("receiverName") as String?
		this.title = jsonObj.get("title") as String?
		this.amount = jsonObj.get("amount") as Double?
	}
	override fun toString() : String{
		val line1 = "Nadawca:\n$senderAccNumber"
		val line2 = "Nazwa odbiorcy:\n$receiverName"
		val line3 = "Numer odbiorcy:\n$receiverAccNumber"
		val line4 = "Tytuł:\n$title"
		val line5 = "kwota:\n${amount.toString()}"
		val toRet = "$line1\n $line2\n $line3\n $line4\n $line5"
		return toRet
	}
	override fun toJsonObject() : JSONObject{
		val JsonObj = JSONObject()
		JsonObj.put("senderAccNumber",senderAccNumber)
		JsonObj.put("receiverAccNumber",receiverAccNumber)
		JsonObj.put("receiverName",receiverName)
		JsonObj.put("title",title)
		JsonObj.put("amount",amount)
	}
	fun validateData() : Boolean{
		val senderAccCorrectLength = senderAccNumber?.length in 0..26
		val receiverAccCorrectLength = receiverAccNumber?.length in 0..26
		val senderReciverAccDiffer = senderAccNumber != receiverAccNumber
		val receiverNameOk = receiverName?.length in 5..50
		val titleOk = receiverName?.length in 5..50
		val amountOk = amount!! > 0.0

		val dataOk = senderAccCorrectLength && receiverAccCorrectLength &&senderReciverAccDiffer && receiverNameOk && titleOk && amountOk
		return dataOk
	}
}