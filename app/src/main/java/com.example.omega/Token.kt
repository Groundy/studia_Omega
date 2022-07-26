package com.example.omega

import android.util.Log
import org.json.JSONObject
class Token {
	private var tokenObj : JSONObject? = null
	private var accounts : List<PaymentAccount>? = null
	companion object{
		enum class ResponseFieldsNames(val text : String){
			TokenType("token_type"),
			AccessToken("access_token"),
			RefreshToken("refresh_token"),
			ExpiresIn("expires_in"),//secondsToExpire
			Scope("scope"),
			ScopeDetails("scope_details"),
			ResponseHeader("responseHeader");
		}
		enum class ScopeDetailsObjFieldsNames(val text: String){
			ConsentId("consentId"),
			ScopeTimeLimit("scopeTimeLimit"),
			ThrottlingPolicy("throttlingPolicy"),
			PrivilegeList("privilegeList");
		}
		enum class PrivilegeListObjFieldsNames(val text: String){
			AccountNumber("accountNumber");//tutaj oprocz accountNbr jest jeszcze lista zgód dla tego konta wraz z ich ograniczeniami, jezeli trzeba to dodac pozniej
		}
	}
	constructor(jsonObject: JSONObject){
		tokenObj = jsonObject
	}
	constructor(jsonObjectStr: String){
		tokenObj = JSONObject(jsonObjectStr)
	}

	override fun toString(): String {
		return tokenObj.toString()
	}
	fun isOk() : Boolean{
		return true//todo
	}

	fun getDetailsOfAccountsFromBank() : Boolean{
		//fill accounts object in Token

		if(tokenObj == null){
			Log.e(Utilities.TagProduction, "[getDetailsOfAccountsFromBank()/${this.javaClass.name}] Token json is null")
			false
		}
		return try {
			val accountNumbers = getListOfAccountsNumbers()
			if(accountNumbers.isNullOrEmpty())
				false

			val successfulyObtainedAccountDetails = ApiGetPaymentAccDetails(this).run(accountNumbers!!)
			successfulyObtainedAccountDetails
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getDetailsOfAccountsFromBank()/${this.javaClass.name}] Token json wrong struct")
			false
		}
	}
	fun getListOfAccountsNumbers() : List<String>?{
		if(tokenObj == null){
			Log.e(Utilities.TagProduction, "[getListOfAccountsNumbers()/${this.javaClass.name}] Token json is null")
			return null
		}
		return try {
			val scopeDetailsObj = tokenObj!!.getJSONObject(ResponseFieldsNames.ScopeDetails.text)
			val accountsArray = scopeDetailsObj.getJSONArray(ScopeDetailsObjFieldsNames.PrivilegeList.text)
			val array : ArrayList<String> = ArrayList()
			for (i in 0 until accountsArray.length()){
				val accNumber = accountsArray.getJSONObject(i).getString(PrivilegeListObjFieldsNames.AccountNumber.text)
				array.add(accNumber)
			}
			array.toList()
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getListOfAccountsNumbers()/${this.javaClass.name}] wrong struct of Token json struct")
			null
		}
	}
	fun updateListOfAccountWithDetails(accList : List<PaymentAccount>){
		this.accounts = accList
	}
	fun getAuthFieldValue() : String{
		if(tokenObj == null) {
			Log.e(Utilities.TagProduction, "[getAuthFieldValue/${this.javaClass.name}] null Token")
			return String()
		}

		return try {
			val tokenType = tokenObj!!.getString(ResponseFieldsNames.TokenType.text).toString()
			val accessToken = tokenObj!!.getString(ResponseFieldsNames.AccessToken.text).toString()
			"$tokenType $accessToken"
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getAuthFieldValue/${this.javaClass.name}] null values of accessToken or tokenType")
			""
		}
	}
	fun getListOfAccountsToDisplay() : List<String>?{
		val displayableStringsToRet = arrayListOf<String>()
		return try {
			this.accounts!!.forEach {
				displayableStringsToRet.add(it.toDisplayableString())
			}
			displayableStringsToRet.toList()
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getListOfAccountsToDisplay/${this.javaClass.name}] functions started with null accountList, although it shouldnt")
			null
		}
	}
	fun getAccessToken() : String{
		if(tokenObj == null){
			Log.e(Utilities.TagProduction, "[getAccessToken()/${this.javaClass.name}] Error, cant get accessToken from Token Json, json is null")
			String()
		}
		return try {
			tokenObj!!.get(ResponseFieldsNames.AccessToken.text).toString()
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getAccessToken()/${this.javaClass.name}] Error, cant get accessToken from Token Json, but json is not null")
			String()
		}
	}
}