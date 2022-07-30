package com.example.omega

import android.app.Activity
import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class Token() {
	private var tokenObj : JSONObject? = null
	private var accounts : List<PaymentAccount>? = null

	companion object{
		//tutaj oprocz accountNbr jest jeszcze lista zgód dla tego konta wraz z ich ograniczeniami, jezeli trzeba to dodac pozniej
		enum class ResponseFieldsNames(val text : String){
			TokenType("token_type"),
			AccessToken("access_token"),
			RefreshToken("refresh_token"),
			ExpiresIn("expires_in"),//secondsToExpire
			//Scope("scope"),
			ScopeDetails("scope_details"),
			ResponseHeader("responseHeader");
		}
		enum class ScopeDetailsObjFieldsNames(val text: String){
			//ConsentId("consentId"),
			//ScopeTimeLimit("scopeTimeLimit"),
			//ThrottlingPolicy("throttlingPolicy"),
			PrivilegeList("privilegeList");
		}
		enum class PrivilegeListObjFieldsNames(val text: String){
			AccountNumber("accountNumber");//tutaj oprocz accountNbr jest jeszcze lista zgód dla tego konta wraz z ich ograniczeniami, jezeli trzeba to dodac pozniej
		}
		enum class HeadersNames(val text: String){
			//RequestId("requestId"),
			SendDate("sendDate"),
			//IsCallback("isCallback")
			;
		}
		const val minTimeTokenNotMustbeRefreshedSeconds = 180
	}

	constructor(jsonObject: JSONObject) : this() {
		tokenObj = jsonObject
	}

	override fun toString(): String {
		return tokenObj.toString()
	}

	private fun refreshIFneeded(activity : Activity) : Boolean{
		if(tokenObj == null){
			val logMsg = "[refreshIFneeded/${this.javaClass.name}] null Token struct"
			Log.e(TagProduction, logMsg)
			return false
		}

		val secondsToExp = getSecondsLeftToTokenExpiration()
		if(secondsToExp == null){
			val logMsg = "[refreshIFneeded/${this.javaClass.name}] wrong Token struct"
			Log.e(TagProduction, logMsg)
			return false
		}

		val needToRefresh = secondsToExp < minTimeTokenNotMustbeRefreshedSeconds
		if(!needToRefresh)
			return true

		val refreshToken = getRefreshTokenValue() ?: return false
		val newTokenObj = ApiRefreshToken(refreshToken).run() ?: return false

		tokenObj = newTokenObj
		PreferencesOperator.savePref(activity, R.string.PREF_Token , tokenObj.toString())
		return true		//todo implementation
	}
	private fun getRefreshTokenValue(): String? {
		if(tokenObj == null){
			Log.e(TagProduction, "[getRefreshToken/${this.javaClass.name}] Error, json is null")
			String()
		}
		return try {
			tokenObj!!.get(ResponseFieldsNames.RefreshToken.text).toString()
		}catch (e : Exception){
			Log.e(TagProduction, "[getRefreshToken/${this.javaClass.name}] Error,Json is not null but still cant get token")
			null
		}
	}

	fun getListOfAccountsNumbers() : List<String>?{
		if(tokenObj == null){
			Log.e(TagProduction, "[getListOfAccountsNumbers()/${this.javaClass.name}] Token json is null")
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
			Log.e(TagProduction, "[getListOfAccountsNumbers()/${this.javaClass.name}] wrong struct of Token json struct")
			null
		}
	}
	fun isOk(activity: Activity): Boolean {
		if (tokenObj == null)
			return false
		return refreshIFneeded(activity)
	}
	fun getDetailsOfAccountsFromBank(activity: Activity) : Boolean{
		//fill accounts object in Token
		if(tokenObj == null){
			Log.e(TagProduction, "[getDetailsOfAccountsFromBank()/${this.javaClass.name}] Token json is null")
			return false
		}

		return try {
			val accountNumbers = getListOfAccountsNumbers()
			if(accountNumbers.isNullOrEmpty())
				return false

			val successfulyObtainedAccountDetails = ApiGetPaymentAccDetails(this, activity).run(accountNumbers)
			successfulyObtainedAccountDetails
		}catch (e : Exception){
			Log.e(TagProduction, "[getDetailsOfAccountsFromBank()/${this.javaClass.name}] Token json wrong struct")
			false
		}
	}
	fun updateListOfAccountWithDetails(accList : List<PaymentAccount>){
		this.accounts = accList
	}
	fun getAuthFieldValue() : String{
		if(tokenObj == null) {
			Log.e(TagProduction, "[getAuthFieldValue/${this.javaClass.name}] null Token")
			return String()
		}

		return try {
			val tokenType = tokenObj!!.getString(ResponseFieldsNames.TokenType.text).toString()
			val accessToken = tokenObj!!.getString(ResponseFieldsNames.AccessToken.text).toString()
			"$tokenType $accessToken"
		}catch (e : Exception){
			Log.e(TagProduction, "[getAuthFieldValue/${this.javaClass.name}] null values of accessToken or tokenType")
			String()
		}
	}
	fun getListOfAccountsToDisplay() : List<String>?{
		if(tokenObj == null)
			return null

		val displayableStringsToRet = arrayListOf<String>()
		return try {
			this.accounts!!.forEach {
				displayableStringsToRet.add(it.toDisplayableString())
			}
			displayableStringsToRet.toList()
		}catch (e : Exception){
			Log.e(TagProduction, "[getListOfAccountsToDisplay/${this.javaClass.name}] functions started with null accountList, although it shouldnt")
			null
		}
	}
	fun getAccessToken() : String{
		if(tokenObj == null){
			Log.e(TagProduction, "[getAccessToken()/${this.javaClass.name}] Error, cant get accessToken from Token Json, json is null")
			String()
		}

		return try {
			tokenObj!!.get(ResponseFieldsNames.AccessToken.text).toString()
		}catch (e : Exception){
			Log.e(TagProduction, "[getAccessToken()/${this.javaClass.name}] Error, cant get accessToken from Token Json, but json is not null")
			String()
		}
	}
	fun getSecondsLeftToTokenExpiration() : Long? {
		if(tokenObj == null)
			return null

		return try {
			val startTokenTime = tokenObj!!.getJSONObject(ResponseFieldsNames.ResponseHeader.text)
				.getString(HeadersNames.SendDate.text)
			val expireIn = tokenObj!!.getString(ResponseFieldsNames.ExpiresIn.text).toInt()
			return OmegaTime.getSecondsToStampExpiration(startTokenTime, expireIn)
		} catch (e: Exception) {
			null
		}
	}
	fun getPaymentAccount(accountNumber: String) : PaymentAccount?{
		if(tokenObj == null)
			return null

		if(accounts.isNullOrEmpty()){
			Log.e(TagProduction, "[getPaymentAccount/${this.javaClass.name}] empty or null account list, probably it isnt initilized yet")
			return null
		}
		accounts!!.forEach{
			val userLookForThatAcc = it.getAccNumber() == accountNumber
			if(userLookForThatAcc){
				return it
			}
		}
		Log.e(TagProduction, "[getPaymentAccount/${this.javaClass.name}] account number not found, size of accounts array: ${accounts!!.size}")
		return null
	}
	fun getAccountNbrByDisplayStr(string: String) : String?{
		val pattern = "]  "
		return try {
			val parts = string.split(pattern)
			parts[1]
		}
		catch (e : Exception){
			Log.e(TagProduction, "[getAccountNbrByDisplayStr/${this.javaClass.name}] error in obtainging acc number from display str")
			null
		}
	}
}