package com.example.omega

import android.util.Log
import org.json.JSONObject

class PaymentAccount {
    enum class BankResponseJsonFields(val text: String){
        AccountObj("account"),
        ResponseHeaderObj("responseHeader");
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
        PsuRelationsArr("psuRelations"),
        BankObj("bank");
        //Dostępne są bardziej szczególowe info, np.Nazwa banku
    }
    private var contentJson : JSONObject? = null


    constructor(){
        contentJson = null
    }
    constructor(jsonObject: JSONObject){
        contentJson = try {
            jsonObject.getJSONObject(BankResponseJsonFields.AccountObj.text)
        }catch(e : Exception){
            Log.e(Utilities.TagProduction,"[constructor(json)/${this.javaClass.name}], Error Wrong json struct!")
            null
        }
    }
    fun isValid() : Boolean{
        return true //todo
    }

    override fun toString(): String {
        return contentJson.toString()//todo chyba ta funkcja jest zbyteczna
    }
    fun toDisplayableString() : String{
        val availableBalance = getBalanceOfAccount()
        val currency = getCurrencyOfAccount()
        val accNumber = getAccNumber()
        return "[$availableBalance $currency]  $accNumber"
    }

    fun getCurrencyOfAccount() : String{
        if(this.contentJson == null){
            Log.e(Utilities.TagProduction, "[getCurrencyOfAccount/${this.javaClass.name}] json in payment acc is null")
            return "Null"
        }

        return try {
            contentJson!!.getString(AccountObjectFields.Currency.text).toString()
        }catch (e :Exception){
            Log.e(Utilities.TagProduction, "[getCurrencyOfAccount/${this.javaClass.name}] wrong Json Struct")
            "Null"
        }
    }
    fun getBalanceOfAccount() : Double{
        if(this.contentJson == null){
            Log.e(Utilities.TagProduction, "[getBalanceOfAccount/${this.javaClass.name}] json in payment acc is null")
            return 0.0
        }

        return try {
            contentJson!!.getString(AccountObjectFields.AvailableBalance.text).toDouble()
        }catch (e :Exception){
            Log.e(Utilities.TagProduction, "[getBalanceOfAccount/${this.javaClass.name}] wrong Json Struct")
            0.0
        }
    }
    fun getAccNumber() : String{
        if(this.contentJson == null){
            Log.e(Utilities.TagProduction, "[getAccNumber/${this.javaClass.name}] json in payment acc is null")
            return "Null"
        }

        return try {
            contentJson!!.getString(AccountObjectFields.AccountNumber.text).toString()
        }catch (e :Exception){
            Log.e(Utilities.TagProduction, "[getAccNumber/${this.javaClass.name}] wrong Json Struct")
            "Null"
        }
    }
}
