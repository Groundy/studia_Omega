package com.example.omega

import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class PaymentAccount() {
    companion object{
        enum class BankResponseJsonFields(val text: String){
            AccountObj("account"),
            ResponseHeaderObj("responseHeader");
        }
        enum class AccountObjectFields(val text : String){
            AccountNumber("accountNumber"),
            //AccountTypeName("accountTypeName"),
            Currency("currency"),
            AvailableBalance("availableBalance"),
            //BookingBalance("bookingBalance"),
            //AccountHolderType("accountHolderType"),

            NameAdressObj("nameAddress"),
            //AccountTypeObj("accountType"),
            //PsuRelationsArr("psuRelations"),
            //BankObj("bank");
            //Dostępne są bardziej szczególowe info, np.Nazwa banku
        }
        enum class NameClassObjFields(val text : String){
            Value("value")
        }
    }

    private var contentJson : JSONObject? = null

    constructor(jsonObject: JSONObject) : this() {
        contentJson = try {
            jsonObject.getJSONObject(BankResponseJsonFields.AccountObj.text)
        }catch(e : Exception){
            Log.e(TagProduction,"[constructor(json)/${this.javaClass.name}], Error Wrong json struct!")
            null
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
        if(this.contentJson == null){
            Log.e(TagProduction, "[getCurrencyOfAccount/${this.javaClass.name}] json in payment acc is null")
            return "Null"
        }

        return try {
            contentJson!!.getString(AccountObjectFields.Currency.text).toString()
        }catch (e :Exception){
            Log.e(TagProduction, "[getCurrencyOfAccount/${this.javaClass.name}] wrong Json Struct")
            "Null"
        }
    }
    fun getBalanceOfAccount() : Double?{
        if(this.contentJson == null){
            Log.e(TagProduction, "[getBalanceOfAccount/${this.javaClass.name}] json in payment acc is null")
            return null
        }

        return try {
            contentJson!!.getString(AccountObjectFields.AvailableBalance.text).toDouble()
        }catch (e :Exception){
            Log.e(TagProduction, "[getBalanceOfAccount/${this.javaClass.name}] wrong Json Struct")
            null
        }
    }
    fun getAccNumber() : String{
        if(this.contentJson == null){
            Log.e(TagProduction, "[getAccNumber/${this.javaClass.name}] json in payment acc is null")
            return "Null"
        }

        return try {
            contentJson!!.getString(AccountObjectFields.AccountNumber.text).toString()
        }catch (e :Exception){
            Log.e(TagProduction, "[getAccNumber/${this.javaClass.name}] wrong Json Struct")
            "Null"
        }
    }
	fun getOwnerName(): String{
        if(this.contentJson == null){
            Log.e(TagProduction, "[getOwnerName/${this.javaClass.name}] json in payment acc is null")
            return "Null"
        }
        return try {
            val nameObj = contentJson!!.getJSONObject(AccountObjectFields.NameAdressObj.text)
            val nameArray = nameObj.getJSONArray(NameClassObjFields.Value.text)
            var name = String()
            for (i in 0 until nameArray.length())
                name = name.plus(nameArray[i])
            name
        }catch (e :Exception){
            Log.e(TagProduction, "[getOwnerName/${this.javaClass.name}] wrong Json Struct")
            "Null"
        }
	}
}
