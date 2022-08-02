package com.example.omega
import android.app.Activity
import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction
import org.json.JSONArray

class Token() {
	private var accounts : List<PaymentAccount>? = null

	private var tokenType : String? = null
	private var accessToken : String? = null
	private var refreshToken : String? = null
	private var obtainTime : String? = null
	private var scope : ApiConsts.ScopeValues? = null
	private var scopeExpirationTime : String? = null
	private var privilegeList : JSONArray? = null

	private companion object{
		const val expirationTimeInSeconds = 600
		const val minimalTimeTokenNotMustbeRefreshedSeconds = 180

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
			//ConsentId("consentId"),
			ScopeTimeLimit("scopeTimeLimit"),
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

	}
	constructor(jsonObject: JSONObject) : this() {
		try {
			tokenType = jsonObject.getString(ResponseFieldsNames.TokenType.text)
			accessToken = jsonObject.getString(ResponseFieldsNames.AccessToken.text)
			refreshToken = jsonObject.getString(ResponseFieldsNames.RefreshToken.text)
			obtainTime = jsonObject.getJSONObject(ResponseFieldsNames.ResponseHeader.text).getString(HeadersNames.SendDate.text)

			val scopeTmpStr = jsonObject.getString(ResponseFieldsNames.Scope.text)
			scope = ApiConsts.ScopeValues.fromStr(scopeTmpStr)

			val scopeDetilsObj = jsonObject.getJSONObject(ResponseFieldsNames.ScopeDetails.text)
			scopeExpirationTime = scopeDetilsObj.getString(ScopeDetailsObjFieldsNames.ScopeTimeLimit.text)

			val privilegeListTmp = scopeDetilsObj.getJSONArray(ScopeDetailsObjFieldsNames.PrivilegeList.text)
			privilegeList = privilegeListTmp
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] error in parsing")
		}
	}
	constructor(string: String) : this() {
		try {
			val jsonConstructor = JSONObject(string)
			val tokenCopy = Token(jsonConstructor)
			fillFieldsFromTokenCpy(tokenCopy)
		}catch (e : Exception){
			Log.e(TagProduction, "[constructor(json)/${this.javaClass.name}] error in parsing from str e=$e")
		}
	}
	override fun toString(): String{
		return try{
			val scopeDetilsObj = JSONObject()
				.put(ScopeDetailsObjFieldsNames.ScopeTimeLimit.text, scopeExpirationTime)
				.put(ScopeDetailsObjFieldsNames.PrivilegeList.text, privilegeList)

			val responseHeadersObj = JSONObject()
				.put(HeadersNames.SendDate.text, obtainTime)

			val toRet = JSONObject()
				.put(ResponseFieldsNames.TokenType.text, tokenType)
				.put(ResponseFieldsNames.AccessToken.text, accessToken)
				.put(ResponseFieldsNames.RefreshToken.text, refreshToken)
				.put(ResponseFieldsNames.ResponseHeader.text, responseHeadersObj)
				.put(ResponseFieldsNames.Scope.text, scope!!.text)
				.put(ResponseFieldsNames.ScopeDetails.text, scopeDetilsObj)
			toRet.toString()
		}catch (e : Exception){
			Log.e(TagProduction,"[serialize/${this.javaClass.name}] e=$e")
			JSONObject().toString()
		}
	}

	private fun refreshIFneeded(activity : Activity) : Boolean{
		if(refreshToken == null){
			val logMsg = "[refreshIFneeded/${this.javaClass.name}] null refresh token"
			Log.e(TagProduction, logMsg)
			return false
		}

		val secondsToExp = getSecondsLeftToTokenExpiration()
		if(secondsToExp == null){
			val logMsg = "[refreshIFneeded/${this.javaClass.name}] wrong Token struct"
			Log.e(TagProduction, logMsg)
			return false
		}

		val needToRefresh = secondsToExp < minimalTimeTokenNotMustbeRefreshedSeconds
		if(!needToRefresh){
			Log.i(TagProduction, "seconds left to token exp:  $secondsToExp")
			return true
		}else
			Log.i(TagProduction, "Token expired: ${-secondsToExp/3600}h ${(-secondsToExp%3600)/60}m ago")


		val refreshToken = this.refreshToken!!
		val newTokenObj = ApiRefreshToken(refreshToken).run()
		if(newTokenObj == null){
			val logMsg = "[refreshIFneeded/${this.javaClass.name}] cant refresh"
			Log.e(TagProduction, logMsg)
			return false
		}

		fillFieldsFromTokenCpy(Token(newTokenObj))
		PreferencesOperator.savePref(activity, R.string.PREF_Token , this.toString())
		return true
	}

	fun getListOfAccountsNumbers() : List<String>?{
		return try {
			val accountsArray = privilegeList!!
			val array : ArrayList<String> = ArrayList()
			for (i in 0 until accountsArray.length()){
				val accNumber = accountsArray.getJSONObject(i).getString(PrivilegeListObjFieldsNames.AccountNumber.text)
				array.add(accNumber)
			}
			array.toList()
		}catch (e : Exception){
			Log.e(TagProduction, "[getListOfAccountsNumbers()/${this.javaClass.name}] wrong struct of Token struct")
			null
		}
	}
	fun isOk(activity: Activity): Boolean {
		if (accessToken == null)
			return false
		return refreshIFneeded(activity)
	}
	fun getDetailsOfAccountsFromBank(activity: Activity) : Boolean{
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
		val ok = accessToken != null && tokenType!= null
		if(!ok){
			Log.e(TagProduction, "[getAuthFieldValue/${this.javaClass.name}] empty access or type field")
			return String()
		}
		return "$tokenType $accessToken"
	}
	fun getListOfAccountsToDisplay() : List<String>?{
		if(accessToken == null)
			return null

		val displayableStringsToRet = arrayListOf<String>()
		return try {
			accounts!!.forEach {
				displayableStringsToRet.add(it.toDisplayableString())
			}
			displayableStringsToRet.toList()
		}catch (e : Exception){
			Log.e(TagProduction, "[getListOfAccountsToDisplay/${this.javaClass.name}] functions started with null accountList, although it shouldnt")
			null
		}
	}
	private fun getSecondsLeftToTokenExpiration() : Long? {
		return try {
			val startTokenTime = this.obtainTime
			val expireIn = expirationTimeInSeconds
			return OmegaTime.getSecondsToStampExpiration(startTokenTime!!, expireIn)
		} catch (e: Exception) {
			Log.e(TagProduction,"[getSecondsLeftToTokenExpiration/${this.javaClass.name}] Probably token is null")
			null
		}
	}
	fun getPaymentAccount(accountNumber: String) : PaymentAccount?{
		if(accessToken == null)
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
	private fun fillFieldsFromTokenCpy(tokenCopy: Token){
		tokenType = tokenCopy.tokenType
		accessToken = tokenCopy.accessToken
		refreshToken = tokenCopy.refreshToken
		obtainTime = tokenCopy.obtainTime
		scope = tokenCopy.scope
		scopeExpirationTime = tokenCopy.scopeExpirationTime
		privilegeList= tokenCopy.privilegeList
	}
}