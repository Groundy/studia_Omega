package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import com.example.omega.PaymentAccount.Companion.AccountObjectFields.*
import com.example.omega.PaymentAccount.Companion.OtherJsonFields.*

class PaymentAccount() {
	private companion object{
		enum class BankResponseJsonFields(val text: String){
			AccountObj("account"),
			//ResponseHeaderObj("responseHeader");
		}
		enum class AccountObjectFields(val text : String){
			AccountNumber("accountNumber"),
			AccountTypeName("accountTypeName"),
			Currency("currency"),
			AvailableBalance("availableBalance"),
			BookingBalance("bookingBalance"),
			AccountHolderType("accountHolderType"),

			NameAdressObj("nameAddress"),
			AccountTypeObj("accountType"),
			PsuRelationsArray("psuRelations"),
			BankObj("bank");
		}
		enum class BankObjFields(val text : String){
			Address("address"),
			Name("name"),
			BicOrSwift("bicOrSwift");
		}
		enum class BicOrSwiftType(val text: String){
			Bic("bic"), Swift("swift"), Undefined("Undefined");
			companion object{
				fun fromStr(text: String) : BicOrSwiftType{
					return when(text){
						Bic.text ->Bic
						Swift.text -> Swift
						else -> Undefined
					}
				}
			}

		}
		enum class OtherJsonFields(val text : String){
			Description("description"),
			Value("value"),
			TypeOfRelation("typeOfRelation");
		}
	}

	internal var accountNumber : String? = null
	internal var ownerName : String? = null
	internal var accountDescription : String? = null
	private var accountTypeName : String? = null
	internal var currency : String? = null
	internal var availableBalance : Double? = null
	private var bookingBalance : Double? = null
	private var accountHolder  : String? = null
	private var psuRelations : String? = null

	private var bankName : String? = null
	private var bankAdress : String? = null
	private var bankType : BicOrSwiftType? = null

	constructor(jsonObjectResponse: JSONObject) : this() {
		try {
			val accountDetailsObj = jsonObjectResponse.getJSONObject(BankResponseJsonFields.AccountObj.text)
			val ownerNameJsonArray = accountDetailsObj.getJSONObject(NameAdressObj.text).getJSONArray(Value.text)
			var ownerNameTmp = String()
			for (i in 0 until ownerNameJsonArray.length()){
				ownerNameTmp = ownerNameTmp.plus(ownerNameJsonArray[i]).plus(" ")
			}
			val bankObj = accountDetailsObj.getJSONObject(BankObj.text)

			val psuRelationArrTmp = accountDetailsObj.getJSONArray(PsuRelationsArray.text)
			val size = psuRelationArrTmp.length()
			val psuRealtionsTmp = if(size >= 1){
				val psuRelationsObj = psuRelationArrTmp.getJSONObject(0)
				psuRelationsObj.getString(TypeOfRelation.text)
			}
			else
				String()

			val bankAdressObjTmp = bankObj.getJSONObject(BankObjFields.Address.text)
			val bankAdressArrayTmp = bankAdressObjTmp.getJSONArray(Value.text)
			var bankAdressTmp = String()
			val size2 = bankAdressArrayTmp.length()
			for (i in 0 until size2){
				bankAdressTmp = bankAdressTmp.plus(bankAdressArrayTmp.get(i))
				if(i != size2 - 1 )
					bankAdressTmp = bankAdressTmp.plus(", ")
			}

			ownerName = ownerNameTmp
			accountNumber = accountDetailsObj.getString(AccountNumber.text)
			accountDescription = accountDetailsObj.getJSONObject(AccountTypeObj.text).getString(Description.text)
			accountTypeName = accountDetailsObj.getString(AccountTypeName.text)
			currency = accountDetailsObj.getString(Currency.text)
			availableBalance = accountDetailsObj.getDouble(AvailableBalance.text)
			bookingBalance = accountDetailsObj.getDouble(BookingBalance.text)
			accountHolder = accountDetailsObj.getString(AccountHolderType.text)
			psuRelations = psuRealtionsTmp

			bankName  = bankObj.getString(BankObjFields.Name.text)
			bankAdress = bankAdressTmp
			val bicOrSwiftStr = bankObj.getString(BankObjFields.BicOrSwift.text)
			bankType = BicOrSwiftType.fromStr(bicOrSwiftStr)
		}catch(e : Exception){
			Log.e(TagProduction,"[constructor(json)/${this.javaClass.name}], Error Wrong json struct!")
		}
	}

	fun isValid() : Boolean{
		//todo implement
		return true
	}
	fun toDisplayableString() : String{
		val availableBalance = getBalanceOfAccount()
		val currency = getCurrencyOfAccount()
		val accNumber = getAccNumber()
		return "[$availableBalance $currency]  $accNumber"
	}
	fun getCurrencyOfAccount() : String{
		if(currency == null){
			Log.e(TagProduction, "[getCurrencyOfAccount/${this.javaClass.name}] json in payment acc is null")
			return "Null"
		}
		return currency as String
	}
	fun getBalanceOfAccount() : Double?{
		if(availableBalance == null){
			Log.e(TagProduction, "[getBalanceOfAccount/${this.javaClass.name}] is null")
			return null
		}
		return availableBalance
	}
	fun getAccNumber() : String{
		if(accountNumber == null){
			Log.e(TagProduction, "[getAccNumber/${this.javaClass.name}] s null")
			return "Null"
		}
		return accountNumber as String
	}
	fun getOwnerName(): String{
		if(ownerName == null){
			Log.e(TagProduction, "[getOwnerName/${this.javaClass.name}] is null")
			return "null"
		}
		return ownerName as String
	}
}
