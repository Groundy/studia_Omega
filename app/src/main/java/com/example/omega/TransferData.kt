package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class TransferData() {
	companion object{
		private enum class TransferDataFields(val text : String){
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
		private const val domesticMethodeName = "pis:domestic"
		private const val bundleMethodeName = "pis:bundle"
		fun fromDomesticPaymentToken(singlePaymentToken : Token) : TransferData?{
			return try {
				val privilegesList = singlePaymentToken.getPriligeList()
				val methodeObj = privilegesList!!.getJSONObject(0).getJSONObject(domesticMethodeName)
				val toRet = fromTokenObj(methodeObj)
				toRet
			}catch (e : Exception){
				Log.e(TagProduction, "[constructor(token)/TransferData] failed to obtain tmp data transfer class from payment token")
				null
			}
		}
		fun fromBundleToken(bundle : Token) : List<TransferData>{
			return try {
				val privilegesList = bundle.getPriligeList() ?: return emptyList()
				val toRet = arrayListOf<TransferData>()
				val transfersArray = privilegesList
					.getJSONObject(0)
					.getJSONObject(bundleMethodeName)
					.getJSONArray("domesticTransfers")

				for (i in 0 until transfersArray.length()){
					val transferData = fromTokenObj(transfersArray.getJSONObject(i))
						?: throw Exception("fromTokenObj return null")
					toRet.add(transferData)
				}

				toRet.toList()
			}catch (e : Exception){
				Log.e(TagProduction, "[constructor(token)/TransferData] failed to obtain tmp data transfer class from payment token, e=$e")
				emptyList()
			}
		}
		private fun fromTokenObj(methodeObj : JSONObject) : TransferData?{
			return try {
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

				val objToRet = TransferData()
				with(objToRet){
					senderAccNumber = senderObj.getString(TransferDataFields.AccountNumber.text)
					senderAccName = senderNameTmp
					receiverAccNumber = receiverObj.getString(TransferDataFields.AccountNumber.text)
					receiverName = receiverNameTmp
					description = subDataObj.getString(TransferDataFields.Description.text)
					amount = subDataObj.getString(TransferDataFields.Amount.text).toDouble()
					currency = subDataObj.getString(TransferDataFields.Currency.text)
					executionDate = subDataObj.getString(TransferDataFields.ExecutionDate.text)
				}
				objToRet
			}catch (e : Exception){
				Log.e(TagProduction, "[constructor(token)/TransferData] failed to obtain tmp data transfer class from payment token")
				null
			}
		}
		fun fromJsonSerilization(jsonObj : JSONObject) : TransferData{
			try {
				val senderAccNumberTmp = if(jsonObj.has(TransferDataFields.SenderAccNumber.text))
					jsonObj.getString(TransferDataFields.SenderAccNumber.text)
				else
					String()

				val senderAccNameTmp = if(jsonObj.has(TransferDataFields.SenderAccName.text))
					jsonObj.getString(TransferDataFields.SenderAccName.text)
				else
					String()
				val receiverAccNumberTmp = jsonObj.getString(TransferDataFields.ReceiverAccNumber.text)
				val receiverNameTmp = jsonObj.getString(TransferDataFields.ReceiverName.text)
				val descriptionTmp = jsonObj.getString(TransferDataFields.Description.text)
				val amountTmp = jsonObj.getDouble(TransferDataFields.Amount.text)
				val currencyTmp = jsonObj.getString(TransferDataFields.Currency.text)
				val executionDateTmp = jsonObj.getString(TransferDataFields.ExecutionDate.text)

				val toRet = TransferData().also {
					it.amount = amountTmp
					it.senderAccNumber = senderAccNumberTmp
					it.senderAccName = senderAccNameTmp
					it.receiverName = receiverNameTmp
					it.receiverAccNumber  =receiverAccNumberTmp
					it.description = descriptionTmp
					it.currency = currencyTmp
					it.executionDate = executionDateTmp
				}
				return toRet
			}catch (e : Exception){
				Log.e(TagProduction, "[constructor(json)/TransferData] error in constructing TransferDataObj from json")
				return TransferData()
			}
		}
		fun fromJsonSerialized(serializedObject : String) : TransferData?{
			try{
				val jsonObj = JSONObject(serializedObject)

				val senderAccNumberTmp = if(jsonObj.has(TransferDataFields.SenderAccNumber.text))
					jsonObj.getString(TransferDataFields.SenderAccNumber.text)
				else
					String()

				val senderAccNameTmp = if(jsonObj.has(TransferDataFields.SenderAccName.text))
					jsonObj.getString(TransferDataFields.SenderAccName.text)
				else
					String()

				val receiverAccNumberTmp = jsonObj.getString(TransferDataFields.ReceiverAccNumber.text)
				val receiverNameTmp = jsonObj.getString(TransferDataFields.ReceiverName.text)
				val descriptionTmp = jsonObj.getString(TransferDataFields.Description.text)
				val amountTmp = jsonObj.getDouble(TransferDataFields.Amount.text)
				val currencyTmp = jsonObj.getString(TransferDataFields.Currency.text)
				val executionDateTmp = jsonObj.getString(TransferDataFields.ExecutionDate.text)

				val toRet = TransferData().also {
					it.amount = amountTmp
					it.senderAccNumber = senderAccNumberTmp
					it.senderAccName = senderAccNameTmp
					it.receiverName = receiverNameTmp
					it.receiverAccNumber  =receiverAccNumberTmp
					it.description = descriptionTmp
					it.currency = currencyTmp
					it.executionDate = executionDateTmp
				}
				return toRet
			}
			catch (e : Exception){
				Log.e(TagProduction, "Error in creating transferDataObject from serialized data! [$e]")
				return null
			}
		}
	}
	var senderAccNumber : String? = null
	var senderAccName : String? = null
	var receiverAccNumber : String? = null
	var receiverName : String? = null
	var description : String? = null
	var amount : Double? = null
	var currency : String? = null
	var executionDate : String? = OmegaTime.getDate()

	override fun toString() : String{
		return toJsonObject().toString()
	}
	fun toJsonObject() : JSONObject{
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
}

/*
	fun toDisplayString() : String{
		val line1 = "Nadawca:\n$senderAccNumber"
		val line2 = "Nazwa odbiorcy:\n$receiverName"
		val line3 = "Numer odbiorcy:\n$receiverAccNumber"
		val line4 = "Tytuł:\n$description"
		val line5 = "kwota:\n${amount.toString()} ${currency.toString()}"
		return "$line1\n $line2\n $line3\n $line4\n $line5"
	}
*/
/*
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

		val senderAccCorrectLength = senderAccNumber?.length == ApiFunctions.getLengthOfCountryBankNumberWitchCountryCode() - ApiConsts.countryCodeLength
		if(!senderAccCorrectLength)
			return false

		val receiverAccCorrectLength = senderAccNumber?.length == ApiFunctions.getLengthOfCountryBankNumberWitchCountryCode() - ApiConsts.countryCodeLength
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
 */