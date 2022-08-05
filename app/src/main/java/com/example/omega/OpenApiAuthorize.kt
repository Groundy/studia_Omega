package com.example.omega

import android.app.Activity
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import com.example.omega.ApiConsts.*
import com.example.omega.ApiFunctions.Companion.logResponseError
import com.example.omega.Utilities.Companion.TagProduction
import kotlin.Exception

class OpenApiAuthorize(activity: Activity) {
	private lateinit var permissionsList : PermissionList
	private var callerActivity : Activity = activity
	private lateinit var transferData: TransferData
	private var stateValue = ApiFunctions.getRandomStateValue()
	private companion object{
		enum class ScopeFields(val text: String){
			PrivilegeList("privilegeList"),
			ScopeGroupType("scopeGroupType"),
			ConsentId("consentId"),
			ScopeTimeLimit("scopeTimeLimit"),
			ThrottlingPolicy("throttlingPolicy")
		}
		enum class ApiMethodes(val text : String){
			AisGetTransactionsDone("ais:getTransactionsDone"),
			AisGetAccount("ais:getAccount"),
			PisDomestic("pis:domestic"),
		}
		const val redirectUriField = "aspspRedirectUri"
	}

	fun runForAis(permisionListObject : PermissionList, dialog: WaitingDialog? = null) : Boolean{
		permissionsList = permisionListObject
		if (permissionsList.permissionsArray.isEmpty()) {
			Log.e(TagProduction, "Error, passed null or empty permissionListObject to ApiAuthorized")
			return false
		}
		if(dialog!=null)
			dialog.changeText(callerActivity, R.string.POPUP_auth)
		Log.i(TagProduction, "Authorize started")
		var success = false
		val thread = Thread{
			val request = getRequest(stateValue, ScopeValues.Ais)
			val okResponse = sendRequest(request) ?: return@Thread
			success = handleResponse(okResponse)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(success)
			Log.i(TagProduction, "Authorize ended with sucess")
		else
			Log.e(TagProduction, "Authorize ended with error")
		if(dialog!=null)
			dialog.changeText(callerActivity, R.string.POPUP_empty)
		return success
	}
	fun runForPis(permisionListObject : PermissionList, transferData: TransferData) : Boolean{
		permissionsList = permisionListObject
		this.transferData = transferData

		Log.i(TagProduction, "Authorize for pis started")
		var success = false
		val thread = Thread{
			val request = getRequest(stateValue, ScopeValues.Pis)
			val response = sendRequest(request) ?: return@Thread
			success = handleResponse(response)
		}
		thread.start()
		thread.join(ApiConsts.requestTimeOut)
		if(success)
			Log.i(TagProduction, "Authorize for pis ended with sucess")
		else
			Log.e(TagProduction, "Authorize for pis ended with error")
		return success
	}


	private fun sendRequest (request: Request) : JSONObject?{
		return try{
			val response = OkHttpClient().newCall(request).execute()
			if(response.code!= ResponseCodes.OK.code){
				logResponseError(response, this.javaClass.name)
				return null
			}
			val responseBody = response.body?.string()
			val responseJsonObject = JSONObject(responseBody!!)
			responseJsonObject
		}catch (e : Exception){
			Log.e(TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			null
		}
	}
	private fun getRequest(stateStr : String, scope : ScopeValues) : Request {
		val uuidStr = ApiFunctions.getUUID()

		val requestHeaderJsonObj = JSONObject()
			.put(ApiReqFields.RequestId.text, uuidStr)
			.put(ApiReqFields.UserAgent.text, ApiFunctions.getUserAgent())
			.put(ApiReqFields.IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			.put(ApiReqFields.SendDate.text, OmegaTime.getCurrentTime())
			.put(ApiReqFields.TppId.text, ApiConsts.TTP_ID)
			.put(ApiReqFields.IsCompanyContext.text, false)

		val requestBodyJson = JSONObject()
			.put(ApiReqFields.RequestHeader.text, requestHeaderJsonObj)
			.put(ApiReqFields.ResponseType.text,ResponseTypes.Code.text)
			.put(ApiReqFields.ClientId.text, ApiConsts.userId_ALIOR)
			.put(ApiReqFields.Scope.text, scope.text)
			.put(ApiReqFields.ScopeDetails.text,getScopeDetailsObject(scope))
			.put(ApiReqFields.RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ApiReqFields.State.text,stateStr)
		return ApiFunctions.bodyToRequest(BankUrls.AuthUrl, requestBodyJson, uuidStr)
	}
	private fun getScopeDetailsObject(scope: ScopeValues) : JSONObject? {
		val privilegesListJsonObj = when(scope){
			ScopeValues.Ais->{getPrivilegeScopeDetailsObjAIS()}
			ScopeValues.AisAcc->{getPrivilegeScopeDetailsObjAisAccount()}
			ScopeValues.Pis->{getPrivilegeScopeDetailsObjPIS()}
		}
		return JSONObject()
			.put(ScopeFields.PrivilegeList.text, JSONArray().put(privilegesListJsonObj))
			.put(ScopeFields.ScopeGroupType.text, scope.text)
			.put(ScopeFields.ConsentId.text, ApiConsts.ConsentId)
			.put(ScopeFields.ScopeTimeLimit.text, OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds))
			.put(ScopeFields.ThrottlingPolicy.text, ApiConsts.ThrottlingPolicyVal)
	}
	private fun handleResponse(jsonObject: JSONObject) : Boolean{
		 val authUrl = try {
		 	jsonObject.get(redirectUriField).toString()
		 }
		 catch (e : Exception){
			String()
		 }

		if(authUrl.isEmpty())
			return false

		PreferencesOperator.savePref(callerActivity, R.string.PREF_authURL, authUrl)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_lastRandomValue, stateValue)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_lastUsedPermissionsForAuth, permissionsList.toString())
		val validityTime = OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds)
		PreferencesOperator.savePref(callerActivity, R.string.PREF_authUrlValidityTimeEnd, validityTime)
		return true
	}
	private fun getPrivilegeScopeDetailsObjAIS() : JSONObject{
		val privilegesListJsonObj = JSONObject()
		val privListCpy = permissionsList.permissionsArray

		if(privListCpy.contains(Privileges.AccountsDetails)){
			val privilegeScopeDetailsObj = JSONObject()
				.put(ScopeDetailsFields.ScopeUsageLimit.text,ScopeUsageLimit.Multiple.text)

			privilegesListJsonObj.put(ApiMethodes.AisGetAccount.text, privilegeScopeDetailsObj)
		}

		if(privListCpy.contains(Privileges.AccountsHistory)){
			val privilegeScopeDetailsObj = JSONObject()
				.put(ScopeDetailsFields.ScopeUsageLimit.text,ScopeUsageLimit.Multiple.text)
				.put(ScopeDetailsFields.MaxAllowedHistoryLong.text,800)

			privilegesListJsonObj.put(ApiMethodes.AisGetTransactionsDone.text, privilegeScopeDetailsObj)
		}
		return privilegesListJsonObj
	}
	private fun getPrivilegeScopeDetailsObjPIS() : JSONObject{
		val privilegesListJsonObjToRet = JSONObject()

		val userWantAccessToSinglePayment = permissionsList.permissionsArray.contains(Privileges.SinglePayment)

		if(userWantAccessToSinglePayment){
			val domesticPaymentPriviledgeScopeDetailObj = DomesticPaymentSupportClass(transferData).gePrivilegeScopeDetailsObjForAuth()
			privilegesListJsonObjToRet.put(ApiMethodes.PisDomestic.text, domesticPaymentPriviledgeScopeDetailObj)
		}

		return privilegesListJsonObjToRet
	}
	private fun getPrivilegeScopeDetailsObjAisAccount() : JSONObject{
		//todo implement
		return JSONObject()
	}
}