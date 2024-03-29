package com.example.omega

import android.util.Log
import com.example.omega.ApiConsts.Companion.countryCodeLength
import org.json.JSONObject
import org.json.JSONArray

class PaymentSuppClass(private val transferData: TransferData) {
	private companion object{
		enum class RequestFields(val text : String){
			Recipient("recipient"),
			Sender("sender"),
			TransferData ("transferData"),
			TppTransactionId("tppTransactionId"),
			DeliveryMode("deliveryMode"),
			System("system"),
			//Hold("hold"),
			ExecutionMode("executionMode"),
			//SplitPayment("splitPayment"),
			//TransactionInfoSpObj("transactionInfoSp"),

			AccountNumber("accountNumber"),
			NameAddressArray("nameAddress"),
			Value("value")
		}
		enum class TransactionsObjFields(val text: String){
			Description("description"),
			Amount("amount"),
			ExecutionDate("executionDate"),
			Currency("currency")
		}
		/*
		enum class TransactionsExtraInfoObjFields(val text: String){
			SpInvoiceNumber("spInvoiceNumber"),//: "5845456248635392",
			SpTaxIdentificationNumber("spTaxIdentificationNumber"),//: "6976149825519616",
			SpTaxAmount("spTaxAmount"),//: "6864360880209920",
			SpDescription("spDescription"),//: "Epo da jewbe in jimtafo miwjon ra izo on lu kuwimwen zi ijufebo ki hodaraz masehow jehti."
		}
		*/
		enum class DeliveryModes(val text: String){
			//Express("ExpressD0"),//Unsported by Api
			Standard("StandardD1");
		}
		enum class Systems(val text: String){
			Elixir("Elixir"),
			//ExpressElixir("ExpressElixir"),
			//Sorbnet("Sorbnet"),
			//BlueCash("BlueCash"),
			//Internal("Internal");
		}
		enum class ExecutionModes(val text: String){
			Immediate("Immediate"),
			//FutureDated("FutureDated");
		}
		val usingExecutionMode = ExecutionModes.Immediate.text
		val usingSystem = Systems.Elixir.text
		val usingDeliveryMode = DeliveryModes.Standard.text
	}

	private fun getRecipentbj(): JSONObject {
		val recipientObj = JSONObject()
		with(recipientObj){
			val nameAdressObj = nameToRequestJsonFormat(transferData.receiverName)
			val accountNumber = ensureAccNumberGotPLPrefix(transferData.receiverAccNumber)
			put(RequestFields.AccountNumber.text, accountNumber)
			put(RequestFields.NameAddressArray.text,nameAdressObj)
		}
		return recipientObj
	}
	private fun getSenderObj(): JSONObject {
		val senderObj = JSONObject()
		with(senderObj){
			val nameAdressObj = nameToRequestJsonFormat(transferData.senderAccName)
			val accountNumber = ensureAccNumberGotPLPrefix(transferData.senderAccNumber)
			put(RequestFields.AccountNumber.text, accountNumber)
			put(RequestFields.NameAddressArray.text,nameAdressObj)
		}
		return senderObj
	}
	private fun getTransferDataObj(): JSONObject {
		val transferDataObj = JSONObject()
		with(transferDataObj){
			put(TransactionsObjFields.Description.text, transferData.description)
			val amountStr = Utilities.doubleToTwoDigitsAfterCommaString(transferData.amount)
			put(TransactionsObjFields.Amount.text, amountStr)
			put(TransactionsObjFields.ExecutionDate.text, transferData.executionDate)
			put(TransactionsObjFields.Currency.text, transferData.currency)
		}
		return transferDataObj
	}
	private fun ensureAccNumberGotPLPrefix(accountNumber: String?) : String{
		if(accountNumber==null){
			Log.e(Utilities.TagProduction,"[ensureAccNumberGotPLPrefix/${this.javaClass.name}] null acc number passed to request")
			return String()
		}
		val length = accountNumber.length
		val digitsLengthProperVal = ApiFunctions.getLengthOfCountryBankNumberWitchCountryCode()
		return when (length) {
			digitsLengthProperVal - countryCodeLength -> "PL$accountNumber"
			digitsLengthProperVal -> accountNumber
			else -> {
				Log.e(Utilities.TagProduction,"[ensureAccNumberGotPLPrefix/${this.javaClass.name}] error acc number passed to request")
				String()
			}
		}
	}
	private fun nameToRequestJsonFormat(name : String?) : JSONObject{
		if(name == null){
			Log.e(Utilities.TagProduction,"[nameToRequestJsonFormat/${this.javaClass.name}] null acc name passed to request")
			return JSONObject()
		}
		val nameObj = JSONObject()
		with(nameObj){
			val array = JSONArray().put(name)
			put(RequestFields.Value.text,array)
		}
		return nameObj
	}

	fun toDomesticPaymentScopeDetialObjForAuth(): JSONObject {
		val scopeUsageLimitStr = ApiConsts.ScopeDetailsFields.ScopeUsageLimit.text
		val scopeSingleUsageStr = ApiConsts.ScopeUsageLimit.Single.text
		val scopeDetails = JSONObject()
		with(scopeDetails){
			put(scopeUsageLimitStr,scopeSingleUsageStr)
			put(RequestFields.Recipient.text, getRecipentbj())
			put(RequestFields.Sender.text, getSenderObj())
			put(RequestFields.TransferData.text,getTransferDataObj())
			put(RequestFields.TppTransactionId.text, ApiFunctions.getRandomValueForTppTransId())
			put(RequestFields.DeliveryMode.text, usingDeliveryMode)
			put(RequestFields.System.text, usingSystem)
			put(RequestFields.ExecutionMode.text, usingExecutionMode)
		}
		return scopeDetails
	}
	fun toDomesticPaymentRequest(requestHeadersJsonObject: JSONObject): JSONObject {
		val requestJson = JSONObject()
		with(requestJson){
			put(ApiConsts.ApiReqFields.RequestHeader.text, requestHeadersJsonObject)
			put(RequestFields.Recipient.text, getRecipentbj())
			put(RequestFields.Sender.text, getSenderObj())
			put(RequestFields.TransferData.text,getTransferDataObj())
			put(RequestFields.TppTransactionId.text, ApiFunctions.getRandomValueForTppTransId())
			put(RequestFields.DeliveryMode.text, usingDeliveryMode)
			put(RequestFields.System.text, usingSystem)
			put(RequestFields.ExecutionMode.text, usingExecutionMode)
		}
		return requestJson
	}
	fun toBundleJsonArrayElement() : JSONObject{
		val toRet = JSONObject()
		with(toRet){
			put(RequestFields.Recipient.text, getRecipentbj())
			put(RequestFields.Sender.text, getSenderObj())
			put(RequestFields.TransferData.text,getTransferDataObj())
			put(RequestFields.DeliveryMode.text, usingDeliveryMode)
			put(RequestFields.System.text, usingSystem)
			put(RequestFields.ExecutionMode.text, usingExecutionMode)

			if(transferData.tppId == null)
				put(RequestFields.TppTransactionId.text, ApiFunctions.getRandomValueForTppTransId())
			else
				put(RequestFields.TppTransactionId.text, transferData.tppId)
		}
		return toRet
	}
}