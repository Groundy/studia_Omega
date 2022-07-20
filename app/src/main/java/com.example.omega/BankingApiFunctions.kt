package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class ApiGetPaymentAccDetails {
    companion object{
        fun run(accNumber: String? = null): Boolean {
            val getDetailsOfOnlyOneAcc = accNumber != null
            if(getDetailsOfOnlyOneAcc){
                var isSuccess = false
                val thread = Thread{
                    try {
                        isSuccess = getAccInfo(accNumber!!)
                    }catch (e: Exception) {
                        Log.e(Utilities.TagProduction,"Failed to obtain information for account with number[${accNumber}] [$e]")
                    }
                }
                thread.start()
                thread.join(ApiConsts.requestTimeOut)
                return isSuccess
            }
            else{
                try {
                    val listOfThreadCheckingAccInfo = arrayListOf<Thread>()
                    val amountOfAccToCheck = UserData.accessTokenStruct?.listOfAccounts!!.size
                    for (i in 0 until amountOfAccToCheck){
                        val accountNumber = UserData.accessTokenStruct?.listOfAccounts!![i].accNumber
                        val thread = Thread {
                            getAccInfo(accountNumber)
                        }
                        listOfThreadCheckingAccInfo.add(thread)
                    }
                    for (i in 0 until  listOfThreadCheckingAccInfo.size)
                        listOfThreadCheckingAccInfo[i].start()
                    for(i in 0 until  listOfThreadCheckingAccInfo.size)
                        listOfThreadCheckingAccInfo[i].join(ApiConsts.requestTimeOut)
                    return true

                }catch (e: Exception) {
                    Log.e(Utilities.TagProduction,"Failed to obtain information for at account with number[${accNumber}] [$e]")
                    return false
                }
            }

        }
        private fun getPaymentAccDetailsRequest(accNumber: String) : Request {
            val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccount"
            val uuidStr = ApiFunctions.getUUID()
            val currentTimeStr = ApiFunctions.getCurrentTimeStr()

            val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
            val requestBodyJson = JSONObject()
                .put("requestHeader", JSONObject()
                    .put("requestId", uuidStr)
                    .put("userAgent", ApiFunctions.getUserAgent())
                    .put("ipAddress", ApiFunctions.getPublicIPByInternetService())
                    .put("sendDate", currentTimeStr)
                    .put("tppId", ApiConsts.TTP_ID)
                    .put("token", authFieldValue)
                    .put("isDirectPsu", false)
                    .put("directPsu", false)
                )
                .put("accountNumber", accNumber)

            val additionalHeaderList = arrayListOf(Pair("AUTHORIZATION",authFieldValue))
            return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
        }
        private fun getAccInfo(accNumber: String) : Boolean{
            val request = getPaymentAccDetailsRequest(accNumber)
            val response = OkHttpClient().newCall(request).execute()
            val responseCodeOk = response.code == 200
            if(!responseCodeOk)
                return false//todo

            return try {
                val responseBodyJson = JSONObject(response.body?.string()!!)
                parseResponseJson(responseBodyJson)
            }
            catch (e : Exception){
                Log.e(Utilities.TagProduction, e.toString())
                false
            }
        }
        private fun parseResponseJson(obj : JSONObject) : Boolean{
            val tmpPaymentAcc = UserData.PaymentAccount(obj)
            val isValid = tmpPaymentAcc.isValid()
            if(isValid){
                return UserData.accessTokenStruct?.swapPaymentAccountToFilledOne(tmpPaymentAcc)!!
            }
            return false
        }
    }
}

class ApiGetAccounts {
    private lateinit var activity : Activity
    constructor(activity: Activity){
        this.activity = activity
    }

    fun run() {
        var accountListTmp : ArrayList<UserData.PaymentAccount>?
        val thread = Thread{
            try {
                accountListTmp = getAccInfo()
                if(accountListTmp != null)
                    UserData.accessTokenStruct?.listOfAccounts = accountListTmp
                else{
                    //todo
                }
            } catch (e: Exception) {
                Log.e(Utilities.TagProduction,e.toString())//todo
            }
        }
        thread.start()
        thread.join(ApiConsts.requestTimeOut)
    }

    private fun getAccountsRequest() : Request {
        val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccounts"
        val uuidStr = ApiFunctions.getUUID()
        val currentTimeStr = ApiFunctions.getCurrentTimeStr()

        val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
        val requestBodyJson = JSONObject()
            .put("requestHeader", JSONObject()
                .put("requestId", uuidStr)
                .put("userAgent", ApiFunctions.getUserAgent())
                .put("ipAddress", ApiFunctions.getPublicIPByInternetService())
                .put("sendDate", currentTimeStr)
                .put("tppId", "requiredValueThatIsNotValidated")
                .put("token", authFieldValue)
                .put("isDirectPsu", false)
                .put("directPsu", false)
            )

        val additionalHeaderList = arrayListOf<Pair<String,String>>(Pair("AUTHORIZATION",authFieldValue))
        val request = ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
        return request
    }

    private fun getAccInfo() : ArrayList<UserData.PaymentAccount>?{
        val request = getAccountsRequest()
        val response = OkHttpClient().newCall(request).execute()

        val responseCodeOk = response.code == 200
        if(!responseCodeOk){
            return null//todo
        }

        val bodyStr = response.body?.string()
        if(bodyStr.isNullOrEmpty()){
            return null//todo
        }

        val accountsList = return try {
            val responseJson = JSONObject(bodyStr)
            val accountsArray = responseJson.getJSONArray("accounts")
            var accountList = ArrayList<UserData.PaymentAccount>()
            for (i in 0 until accountsArray.length()){
                val accObj =  accountsArray.getJSONObject(i)
                val accountNumber = accObj.getString("accountNumber")
                val tmpAcc = UserData.PaymentAccount(accountNumber)
                accountList.add(tmpAcc)
            }
            accountList
        }catch (e : Exception){
            Log.e(Utilities.TagProduction,e.toString())
            null
        }
        return accountsList
    }
}

class ApiGetToken {
    companion object{
        fun run() : Boolean{
            var success = false
            val thread = Thread{
                try {
                    val responseJson = getTokenJson()
                    if(responseJson != null)
                        success = parseJsonResponse(responseJson)
                } catch (e: Exception) {
                    Log.e(Utilities.TagProduction,e.toString())
                }
            }
            thread.start()
            thread.join(ApiConsts.requestTimeOut)
            return success
        }
        private fun getTokenJson() : JSONObject?{
            val request = getTokenRequest()
            val response = OkHttpClient().newCall(request).execute()

            val responseCodeOk = response.code == 200
            if(!responseCodeOk)
                return null//todo

            val bodyStr = response.body?.string()
            if(bodyStr.isNullOrEmpty())
                return null//todo

            return try {
                JSONObject(bodyStr)
            }catch (e : Exception){
                Log.e(Utilities.TagProduction,e.toString())
                null
            }
        }
        private fun parseJsonResponse(responseJson : JSONObject) : Boolean{
            val tokenType = responseJson.get("token_type")
            val accessTokenCont = responseJson.get("access_token")
            val refreshToken = responseJson.get("refresh_token")
            //val expiresIn = responseJson.get("expires_in") // unused
            val scope = responseJson.get("scope")
            val scopeDetails = responseJson.getJSONObject("scope_details")
            //val responseHeader = responseJson.get("responseHeader")// unused


            val scopeToSet = when(scope){
                "ais" -> ApiConsts.ScopeValues.AIS
                "ais-accounts" -> ApiConsts.ScopeValues.AIS_ACC
                "pis" -> ApiConsts.ScopeValues.PIS
                else -> null
            }
            val expirationDate = scopeDetails.get("scopeTimeLimit").toString()

            val accountsToSet = ArrayList<UserData.PaymentAccount>()
            val accArray = scopeDetails.getJSONArray("privilegeList")
            for (i in 0 until accArray.length()){
                val accNumber = accArray.getJSONObject(i).getString("accountNumber")
                accountsToSet.add(UserData.PaymentAccount(accNumber))
            }

            val accessToken = UserData.AccessTokenStruct()
                .setTokenContent(accessTokenCont.toString())
                .setTokenType(tokenType.toString())
                .setTokenScope(scopeToSet)
                .setTokenExpirationTime(expirationDate)
                .setRefreshToken(refreshToken as String)
                .addAccounts(accountsToSet)
            UserData.accessTokenStruct = accessToken

            return true
        }

        private fun getTokenRequest() : Request {
            val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token"
            val uuidStr = ApiFunctions.getUUID()
            val currentTimeStr = ApiFunctions.getCurrentTimeStr()
            val requestBodyJson = JSONObject()
                .put("requestHeader", JSONObject()
                    .put("requestId", uuidStr)
                    .put("userAgent", ApiFunctions.getUserAgent())
                    .put("ipAddress", ApiFunctions.getPublicIPByInternetService())
                    .put("sendDate", currentTimeStr)
                    .put("tppId", ApiConsts.TTP_ID)
                    .put("isCompanyContext", false))
                .put("Code", UserData.authCode)
                .put("grant_type","authorization_code")
                .put("redirect_uri", ApiConsts.REDIRECT_URI)
                .put("client_id", ApiConsts.userId_ALIOR)
                .put("client_secret", ApiConsts.appSecret_ALIOR)


            return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr)
        }
    }
}

class ApiGetTransactionsDone {

    companion object{
        fun run(activity: Activity, accNumber: String? = null): Boolean {
            val getDetailsOfOnlyOneAcc = accNumber != null
            if(getDetailsOfOnlyOneAcc){
                var isSuccess = false
                val thread = Thread{
                    try {
                        isSuccess = getAccHistory(accNumber!!)
                    }catch (e: Exception) {
                        Log.e(Utilities.TagProduction,"Failed to obtain information for account with number[${accNumber}] [${e.toString()}]")
                    }
                }
                thread.start()
                thread.join(ApiConsts.requestTimeOut)
                return isSuccess
            }
            else{
                try {
                    var listOfThreadCheckingAccInfo = arrayListOf<Thread>()
                    val amountOfAccToCheck = UserData.accessTokenStruct?.listOfAccounts!!.size
                    for (i in 0 until amountOfAccToCheck){
                        val accNumber = UserData.accessTokenStruct?.listOfAccounts!!.get(i).accNumber!!
                        val thread = Thread {
                            val success = getAccHistory(accNumber)
                        }
                        listOfThreadCheckingAccInfo.add(thread)
                    }
                    for (i in 0 until  listOfThreadCheckingAccInfo.size)
                        listOfThreadCheckingAccInfo[i].start()
                    for(i in 0 until  listOfThreadCheckingAccInfo.size)
                        listOfThreadCheckingAccInfo[i].join(ApiConsts.requestTimeOut)
                    return true

                }catch (e: Exception) {
                    Log.e(Utilities.TagProduction,"Failed to obtain information for at account with nummber[${accNumber}] [${e.toString()}]")
                    return false
                }
            }

        }
        private fun getPaymentAccHistoryRequest(accNumber: String) : Request {
            val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getTransactionsDone"
            val uuidStr = ApiFunctions.getUUID()
            val currentTimeStr = ApiFunctions.getCurrentTimeStr()


            val requestBodyJson = JSONObject()
                .put("requestHeader", JSONObject()
                    .put("requestId", uuidStr)
                    .put("userAgent", ApiFunctions.getUserAgent())
                    .put("ipAddress", ApiFunctions.getPublicIPByInternetService())
                    .put("sendDate", currentTimeStr)
                    .put("tppId", ApiConsts.TTP_ID)
                    .put("token", UserData.accessTokenStruct?.tokenContent)
                    .put("isDirectPsu",false)
                    //.put("callbackURL",ApiConsts.REDIRECT_URI)//??
                    //.put("apiKey", ApiConsts.appSecret_ALIOR)//??
                )
                .put("accountNumber", accNumber)
                //.put("itemIdFrom","5989073072160768")//??
                //.put("transactionDateFrom","Thu Apr 30")//??
                //.put("transactionDateTo","Thu Feb 06")//??
                //.put("bookingDateFrom","Thu Feb 03")//??
                //.put("bookingDateTo","Mon Feb 03")//??
                //.put("minAmount","0.0")//??
                //.put("maxAmount","99999.99")//??
                //.put("pageId","1")//??
                //.put("perPage",10)//??
                .put("type","DEBIT")//??

            val authFieldValue = "${UserData.accessTokenStruct?.tokenType} ${UserData.accessTokenStruct?.tokenContent}"
            val additionalHeaderList = arrayListOf(Pair("authorization",authFieldValue))
            return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr, additionalHeaderList)
        }
        private fun getAccHistory(accNumber: String) : Boolean{
            val request = getPaymentAccHistoryRequest(accNumber)
            val response = OkHttpClient().newCall(request).execute()
            val responseCodeOk = response.code == 200
            if(!responseCodeOk){
                return false//todo
            }
            try {
                var responseBodyJson = JSONObject(response.body?.string())
                return parseResponseJson(responseBodyJson)
            }
            catch (e : Exception){
                Log.e(Utilities.TagProduction, e.toString())
                return false
            }
        }
        private fun parseResponseJson(obj : JSONObject) : Boolean{
            //todo
            return false
        }
    }
}

class ApiAuthorize {
    private var permissionsList : PermissionList? = null
    private lateinit var callerActivity : Activity

    fun run(activity: Activity, stateValue : String,  permissionsInput : List<ApiConsts.Privileges>? = null) : Boolean{
        var success = false
        permissionsList = PermissionList(permissionsInput!!)
        callerActivity = activity

        val thread = Thread{
            success = startAuthorize(stateValue)
        }
        thread.start()
        thread.join(ApiConsts.requestTimeOut)
        return success
    }

    private fun startAuthorize(stateValue : String) : Boolean{
        return try{
            val request = getAuthRequest(stateValue)
            val response = OkHttpClient().newCall(request).execute()

            val responseCodeOk = response.code == 200
            return if(responseCodeOk){
                val responseBody = response.body?.string()
                val responseJsonObject = JSONObject(responseBody!!)
                val authUrl = responseJsonObject.get("aspspRedirectUri").toString()
                if(!authUrl.isNullOrEmpty()){//save to prefs
                    PreferencesOperator.savePref(callerActivity, R.string.PREF_authURL, authUrl)
                    PreferencesOperator.savePref(callerActivity, R.string.PREF_lastRandomValue, stateValue)
                    PreferencesOperator.savePref(callerActivity, R.string.PREF_lastUsedPermissionsForAuth, permissionsList.toString())
                    val validityTime = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)
                    PreferencesOperator.savePref(callerActivity, R.string.PREF_authUrlValidityTimeEnd, validityTime)
                    true
                }
                else
                    false
            } else{
                Log.e(Utilities.TagProduction, "Got auth response, Code=${response.code}, body=${response.body?.byteString()}")
                false
            }
        }catch (e : Exception){
            Log.e(Utilities.TagProduction,e.toString())
            false
        }
    }
    private fun getAuthRequest(stateStr : String) : Request {
        val uuidStr = ApiFunctions.getUUID()
        val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
        val currentTimeStr = ApiFunctions.getCurrentTimeStr()
        val endValidityTimeStr = ApiFunctions.getCurrentTimeStr(ApiConsts.AuthUrlValidityTimeSeconds)

        val requestBodyJson = JSONObject()
            .put("requestHeader",JSONObject()
                .put("requestId", uuidStr)
                .put("userAgent", ApiFunctions.getUserAgent())
                .put("ipAddress", ApiFunctions.getPublicIPByInternetService())
                .put("sendDate", currentTimeStr)
                .put("tppId", ApiConsts.TTP_ID)
                .put("isCompanyContext", false)
            )
            .put("response_type","code")
            .put("client_id", ApiConsts.userId_ALIOR)
            .put("scope","ais")
            .put("scope_details",getScopeDetailsObject(endValidityTimeStr))
            .put("redirect_uri", ApiConsts.REDIRECT_URI)
            .put("state",stateStr)
        return ApiFunctions.bodyToRequest(url, requestBodyJson, uuidStr)
    }
    private fun getScopeDetailsObject(expTimeStr : String) : JSONObject{
        val permissionListArray = JSONArray()
        if(!permissionsList!!.permissions.isNullOrEmpty()){
            val toAddObject = JSONObject()

            if(permissionsList!!.permissions.contains(ApiConsts.Privileges.accountsHistory)){
                toAddObject.put("ais:getTransactionsDone",JSONObject()
                    .put("scopeUsageLimit","multiple")
                    .put("maxAllowedHistoryLong",11)
                )
            }
            if(permissionsList!!.permissions.contains(ApiConsts.Privileges.accountsDetails)){
                toAddObject.put("ais:getAccount",JSONObject()
                    .put("scopeUsageLimit","multiple")
                )
            }

            permissionListArray.put(toAddObject)
        }
        else{
            //test
            permissionListArray.put(JSONObject()
                /*
                .put("ais:getAccount",JSONObject()
                    .put("scopeUsageLimit","multiple")
                )
                */
                .put("ais:getTransactionsDone",JSONObject()
                    .put("scopeUsageLimit","multiple")
                    .put("maxAllowedHistoryLong",11)
                )
            )
        }

        return JSONObject() // scopeDetailsObj
            .put("privilegeList", permissionListArray)
            .put("scopeGroupType", "ais")
            .put("consentId", "123456789")
            .put("scopeTimeLimit", expTimeStr)
            .put("throttlingPolicy", "psd2Regulatory")
    }

    companion object{
        fun obtainingNewAuthUrlIsNecessary(activity: Activity, permissionsStr : String?) : Boolean{
            val lastPermissionListStr= PreferencesOperator.readPrefStr(activity, R.string.PREF_lastUsedPermissionsForAuth)
            if(lastPermissionListStr.isNullOrEmpty())
                return true
            if(lastPermissionListStr != permissionsStr)
                return true

            val lastAuthUrlValidityTime = PreferencesOperator.readPrefStr(activity, R.string.PREF_authUrlValidityTimeEnd)
            if(lastAuthUrlValidityTime.isNullOrEmpty())
                return true
            val authTimeIsStillValid = OmegaTime.timestampIsValid(lastAuthUrlValidityTime)
            if(!authTimeIsStillValid)
                return true

            val lastTimeUsedRandomStateValue = PreferencesOperator.readPrefStr(activity, R.string.PREF_lastRandomValue)
            if(lastTimeUsedRandomStateValue.isNullOrEmpty())
                return true

            return false
        }
    }
}