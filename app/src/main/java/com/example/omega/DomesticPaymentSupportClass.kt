package com.example.omega

import android.app.Activity
import org.json.JSONObject
import org.json.JSONArray
import java.util.*

class DomesticPaymentSupportClass(private val transferData: TransferData) {
	private companion object{
		enum class RequestFields(val text : String){
			Recipient("recipient"),
			Sender("sender"),
			TransferData ("transferData"),
			TppTransactionId("tppTransactionId"),
			DeliveryMode("deliveryMode"),
			System("system"),
			Hold("hold"),
			ExecutionMode("executionMode"),
			SplitPayment("splitPayment"),
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
		enum class DeliveryModes(val text: String){
			Express("ExpressD0"),//Unsported by Api
			Standard("StandardD1");
		}
		enum class Systems(val text: String){
			Elixir("Elixir"),
			ExpressElixir("ExpressElixir"),
			Sorbnet("Sorbnet"),
			BlueCash("BlueCash"),
			Internal("Internal");
		}
		enum class ExecutionModes(val text: String){
			Immediate("Immediate"),
			FutureDated("FutureDated");
		}
		val usingExecutionMode = ExecutionModes.Immediate.text
		val usingSystem = Systems.Elixir.text
		val usingDeliveryMode = DeliveryModes.Standard.text
	}

	private fun getRecipentbj(): JSONObject {
		val receiverNameStr = transferData.receiverName?: String()
		return JSONObject()
			.put(RequestFields.AccountNumber.text, transferData.receiverAccNumber)
			.put(RequestFields.NameAddressArray.text,strToAcceptableJsonArrayForm(receiverNameStr))
	}
	private fun getSenderObj(): JSONObject {
		val senderNameStr = transferData.senderAccName?: String()
		return JSONObject()
			.put(RequestFields.AccountNumber.text, transferData.senderAccNumber)
			.put(RequestFields.NameAddressArray.text,strToAcceptableJsonArrayForm(senderNameStr))
	}
	private fun getTransferDataObj(): JSONObject {
		return JSONObject()
			.put(TransactionsObjFields.Description.text, transferData.description)
			.put(TransactionsObjFields.Amount.text, transferData.amount.toString())
			.put(TransactionsObjFields.ExecutionDate.text, transferData.executionDate)
			.put(TransactionsObjFields.Currency.text, transferData.currency)
	}
	private fun getTransactionInfoSpObj(): JSONObject {
		return JSONObject()
			.put(TransactionsExtraInfoObjFields.SpInvoiceNumber.text, "spInvoiceNumber")
			.put(TransactionsExtraInfoObjFields.SpTaxIdentificationNumber.text, "1111111112")
			.put(TransactionsExtraInfoObjFields.SpTaxAmount.text, "1.23")
			.put(TransactionsExtraInfoObjFields.SpDescription.text,"Epo da jewbe in jimtafo miwjon ra izo on lu kuwimwen zi ijufeb.")
	}

	fun gePrivilegeScopeDetailsObjForAuth(callerActivity: Activity): JSONObject {
		val scopeUsageLimitStr = ApiConsts.ScopeDetailsFields.ScopeUsageLimit.text
		val scopeSingleUsageStr = ApiConsts.ScopeUsageLimit.Single.text
		val ttpTransactionId = getRandomValueForTppTransId()
		PreferencesOperator.savePref(callerActivity, R.string.PREF_PaymentID, ttpTransactionId)
		return JSONObject()
			.put(scopeUsageLimitStr,scopeSingleUsageStr)
			.put(RequestFields.Recipient.text, getRecipentbj())
			.put(RequestFields.Sender.text, getSenderObj())
			.put(RequestFields.TransferData.text,getTransferDataObj())
			.put(RequestFields.TppTransactionId.text, ttpTransactionId)
			.put(RequestFields.DeliveryMode.text, usingDeliveryMode)
			.put(RequestFields.System.text, usingSystem)
			//.put(RequestFields.Hold.text, true)
			.put(RequestFields.ExecutionMode.text, usingExecutionMode)
			//.put("splitPayment", false)
			//.put("transactionInfoSp", getTransactionInfoSpObj())
	}

	fun getBodyForTokenRequest(callerActivity: Activity, requestHeadersJsonObject: JSONObject): JSONObject {
		val ttpTransactionId = PreferencesOperator.readPrefStr(callerActivity, R.string.PREF_PaymentID)
		return JSONObject()
			.put(ApiConsts.ApiReqFields.RequestHeader.text, requestHeadersJsonObject)
			.put(RequestFields.Recipient.text, getRecipentbj())
			.put(RequestFields.Sender.text, getSenderObj())
			.put(RequestFields.TransferData.text,getTransferDataObj())
			.put(RequestFields.TppTransactionId.text,ttpTransactionId)
			.put(RequestFields.DeliveryMode.text, usingDeliveryMode)
			.put(RequestFields.System.text, usingSystem)
			//.put(RequestFields.Hold.text, true)
			.put(RequestFields.ExecutionMode.text, usingExecutionMode)
			//.put(RequestFields.SplitPayment.text, false)
			//.put(RequestFields.TransactionInfoSpObj.text,getTransactionInfoSpObj())
	}

	private fun strToAcceptableJsonArrayForm(str: String) : JSONObject{
		//todo dodac jakies logi
		val valueFieldName = "value"
		val jsonArray = JSONArray()
		val nameParts = str.split(" ")
		nameParts.forEach{
			if(it.isNotEmpty())
				jsonArray.put(it)
		}
		return JSONObject().put(valueFieldName, jsonArray)
	}

	private fun getRandomValueForTppTransId() : String{
		val number = Random().nextInt(Int.MAX_VALUE)
		return number.toString()
	}
}