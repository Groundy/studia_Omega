package com.example.omega

import android.util.Log
import com.google.android.gms.common.server.response.FastJsonResponse
import org.json.JSONObject
import java.lang.NullPointerException
import kotlin.math.E
class Token  {
	private val separator = "___"

	var tokenType : String? = null
	var tokenContent : String? = null
	private var scope : ApiConsts.ScopeValues? = null
	private var expirationTime : String? = null
	private var refreshToken : String? = null
	var listOfAccounts : ArrayList<PaymentAccount>? = null

	constructor(responseJson: JSONObject){
		try{
			tokenType = responseJson.get("token_type")?.toString()
			tokenContent = responseJson.get("access_token")?.toString()
			refreshToken = responseJson.get("refresh_token")?.toString()
			val scopeStr = responseJson.get("scope").toString()
			scope = when(scopeStr){
				"ais" -> ApiConsts.ScopeValues.AIS
				"ais-accounts" -> ApiConsts.ScopeValues.AIS_ACC
				"pis" -> ApiConsts.ScopeValues.PIS
				else -> null
			}
			val scopeDetailsJson = responseJson.getJSONObject("scope_details")
			expirationTime = scopeDetailsJson?.get("scopeTimeLimit").toString()

			val accountsToSet = ArrayList<PaymentAccount>()
			val accArray = scopeDetailsJson.getJSONArray("privilegeList")
			for (i in 0 until accArray.length()){
				val accNumber = accArray.getJSONObject(i).getString("accountNumber")
				accountsToSet.add(PaymentAccount(accNumber))
			}
			listOfAccounts = accountsToSet

			//val responseHeader = responseJson.get("responseHeader")// unused
			//val expiresIn = responseJson.get("expires_in") // unused
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "Error in parsing getToken JsonResponseToUSerDataObj")
			tokenType = null
			tokenContent = null
			scope  = null
			expirationTime = null
			refreshToken = null
			listOfAccounts = null
		}
	}
	constructor(creationStr : String){
		try {
			val parts = creationStr.split(separator).toMutableList().also{ it.remove("") }.also { it.toList() }
			tokenType = parts[0]
			tokenContent = parts[1]
			scope = when(parts[2]){
				ApiConsts.ScopeValues.AIS.toString() -> ApiConsts.ScopeValues.AIS
				ApiConsts.ScopeValues.AIS_ACC.toString() -> ApiConsts.ScopeValues.AIS_ACC
				ApiConsts.ScopeValues.PIS.toString() -> ApiConsts.ScopeValues.PIS
				else -> null
			}
			expirationTime = parts[3]
			refreshToken = parts[4]
			listOfAccounts = PaymentAccount.getListOfPaymentsAccounts(parts[5])
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "Error in parsing getToken JsonResponseToUSerDataObj")
			tokenType = null
			tokenContent = null
			scope  = null
			expirationTime = null
			refreshToken = null
			listOfAccounts = null
		}
	}
	constructor(){
		tokenType = null
		tokenContent = null
		scope  = null
		expirationTime = null
		refreshToken = null
		listOfAccounts = null
	}
	fun isNull() : Boolean{
		if(tokenType.isNullOrEmpty())
			return true
		if(tokenContent.isNullOrEmpty())
			return true
		if(scope == null)
			return true
		if(expirationTime.isNullOrEmpty())
			return true
		if(refreshToken.isNullOrEmpty())
			return true
		return false
	}
	fun serialize(): String {
		var listOfAccountsAsStr = String()
	fun getAuthFieldValue() : String{
		if(tokenObj == null) {
		}
			.plus(tokenType).plus(separator)
			.plus(tokenContent).plus(separator)
			.plus(refreshToken).plus(separator)
			.plus(listOfAccountsAsStr).plus(separator)


			return this
		return try {
			val tokenType = tokenObj!!.getString(ResponseFieldsNames.TokenType.text).toString()
			"$tokenType $accessToken"
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[getAuthFieldValue/${this.javaClass.name}] null values of accessToken or tokenType")
			""
		}
				val accountMaths = accList[i].accNumber == tmpPaymentAccount.accNumber
					accList[i] = tmpPaymentAccount
					return true
	}
	fun getListOfAccountsToDisplay() : List<String>?{
		return try {
			this.accounts!!.forEach {
				displayableStringsToRet.add(it.toDisplayableString())
			}
			return false
			displayableStringsToRet.toList()
			null
		}
	fun getBalanceOfAccount(accountNumber : String) : Double?{
	fun getAccessToken() : String{
		if(tokenObj == null){
			Log.e(Utilities.TagProduction, "[getAccessToken()/${this.javaClass.name}] Error, cant get accessToken from Token Json, json is null")
			String()
		}
				if(it.accNumber == accountNumber)
		return try {
			tokenObj!!.get(ResponseFieldsNames.AccessToken.text).toString()
		}
}

class PaymentAccount{
	private var bookingBalance : Double? = null
	private var accountHolderType : String? = null
	private var bankAddress : String? = null
	private var ownerName : String? = null
	fun isValid() : Boolean{
			accNumber.isNullOrEmpty() ||
			availableBalance == null ||
			bankAddress.isNullOrEmpty() ||
		return !isWrong
	}
			val accNumber = accountDetailsObj.getString("accountNumber")
			val accountHolderType = accountDetailsObj.getString("accountHolderType")
		BookingBalance("bookingBalance"),
		AccountHolderType("accountHolderType"),

			for (i in 0 until bankAddressArray.length())
			bankAddress = bankAddress.substring(0,bankAddress.length-1)

			val ownerNameArray = accountDetailsObj.getJSONObject("nameAddress").getJSONArray("value")
			var ownerName = ""
			for (i in 0 until ownerNameArray.length())
				ownerName += "${ownerNameArray[i]},"
			ownerName = ownerName.substring(0,ownerName.length-1)

			this.accNumber = accNumber
			this.accType = accountTypeName
			this.currency = currency
			this.availableBalance = availableBalance.toDoubleOrNull()
			this.bookingBalance = bookingBalance.toDoubleOrNull()
			this.accountHolderType = accountHolderType
			this.bankName = bankName
			this.bankAddress = bankAddress
			this.ownerName = ownerName
			this.ownerName = ownerName
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "Failed to convert response body from getAccount methode to Account obj [$e]")
		}
	}
	constructor(accNumber : String?, accType : String?, currency :String?, availableBalance : Double?,
	            bookingBalance : Double?, accountHolderType : String?, bankName  : String?,
	            bankAddress : String?, ownerName : String?) {
		this.accNumber = accNumber
		this.accType = accType
		this.currency = currency
		this.availableBalance = availableBalance
		this.bookingBalance = bookingBalance
		this.accountHolderType = accountHolderType
		this.bankName  = bankName
		this.bankAddress = bankAddress
		this.ownerName = ownerName

	}
	constructor(creationStr: String){
		try {
			val parts = creationStr.split(separator).toMutableList().also{ it.remove("") }.also { it.toList() }
			when(parts.size){
				1 ->
					accNumber = if(parts[0] == "null") null else parts[0]
				9 ->{
					accNumber = if(parts[0] == "null") null else parts[0]
					accType = if(parts[1] == "null") null else parts[1]
					currency = if(parts[2] == "null") null else parts[2]
					availableBalance = if(parts[3] == "null") null else parts[3].toDoubleOrNull()
					bookingBalance = if(parts[4] == "null") null else parts[4].toDoubleOrNull()
					accountHolderType = if(parts[5] == "null") null else parts[5]
					bankName  = if(parts[6] == "null") null else parts[6]
					bankAddress = if(parts[7] == "null") null else parts[7]
					ownerName = if(parts[8] == "null") null else parts[8]
				}
				else->throw Exception("Parsing Payment passed wrong construction str, \n constStr -->[$creationStr]")
			}
		}
		catch (e : Exception){
			Log.e(Utilities.TagProduction, "Error in Parsing Payment account from str [${e}]")
			accNumber = null
			accType  = null
			currency = null
			availableBalance  = null
			bookingBalance = null
			accountHolderType = null
			bankName = null
			bankAddress = null
			ownerName = null
		}
	}
	constructor(){
		accNumber = null
		accType  = null
		currency = null
		availableBalance  = null
		bookingBalance = null
		accountHolderType = null
		bankName = null
		bankAddress = null
		ownerName = null
	}
	override fun toString(): String {
		return "[${availableBalance.toString()} $currency]  $accNumber"
	}
	fun serialize() : String{
		return String()
			.plus(accNumber).plus(separator)
			.plus(accType).plus(separator)
			.plus(currency).plus(separator)
			.plus(availableBalance).plus(separator)
			.plus(bookingBalance).plus(separator)
			.plus(accountHolderType).plus(separator)
			.plus(bankName).plus(separator)
			.plus(bankAddress).plus(separator)
			.plus(ownerName).plus(endOfPayementStrSeparator)
	}

	companion object{
		private val separator = "+++"
		private val endOfPayementStrSeparator = "***"
		fun getListOfPaymentsAccounts(creationStr : String) : ArrayList<PaymentAccount>? {
			return try {
				var toRet = ArrayList<PaymentAccount>()
				val accountsStr = creationStr.split(endOfPayementStrSeparator).toMutableList().also{ it.remove("")}.also { it.toList() }
				accountsStr.forEach{
					toRet.add(PaymentAccount(it))
				}
				toRet
			}
			catch (e : Exception){
				Log.e(Utilities.TagProduction, "Error in getting arrayList of PaymentAccounts from str.\ncreation str-->[$creationStr] \n error-->[${e}]")
				null
			}
		}
	}
}
