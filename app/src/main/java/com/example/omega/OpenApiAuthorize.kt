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
import java.util.concurrent.TimeUnit
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
			PisBundle("pis:bundle"),
		}
		private enum class BundleFields(val text : String){
			//TppBundleId("tppBundleId"),
			TransfersTotalAmount("transfersTotalAmount"),
			TypeOfTransfers("typeOfTransfers"),
			DomesticTransfers("domesticTransfers"),
			Domestic("domestic")
		}
		const val redirectUriField = "aspspRedirectUri"
	}

	suspend fun runForAis() : Boolean{
		permissionsList = PermissionList(Privileges.AccountsDetails, Privileges.AccountsHistory)
		Log.i(TagProduction, "Authorize started")
		val request = getRequest(stateValue, ScopeValues.Ais)
		val okResponse = sendRequest(request)
		val success = handleResponse(okResponse)
		if(success)
			Log.i(TagProduction, "Authorize ended with sucess")
		else
			Log.e(TagProduction, "Authorize ended with error")
		return success
	}
	suspend fun runForPis(transferData: TransferData) : Boolean{
		permissionsList = PermissionList(Privileges.SinglePayment)
		this.transferData = transferData
		Log.i(TagProduction, "Authorize for pis started")
		val request = getRequest(stateValue, ScopeValues.Pis)
		val response = sendRequest(request)
		val success = handleResponse(response)
		if(success)
			Log.i(TagProduction, "Authorize for pis ended with sucess")
		else
			Log.e(TagProduction, "Authorize for pis ended with error")
		return success
	}
	suspend fun runForBundle(trDataList: List<TransferData>) : Boolean{
		permissionsList = PermissionList(Privileges.Bundle)
		Log.i(TagProduction, "Authorize for pis:bundle started")
		val request = getRequestForBundle(trDataList)
		val response = sendRequest(request)
		val success = handleResponse(response)
		if(success)
			Log.i(TagProduction, "Authorize for pis:bundle ended with sucess")
		else
			Log.e(TagProduction, "Authorize for pis:bundle ended with error")
		return success
	}


	private suspend fun sendRequest (request: Request) : JSONObject?{
		return try{
			val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
			val response = client.newCall(request).execute()
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
			ScopeValues.Pis->{getPrivilegeScopeDetailsObjPIS()}
		}
		return JSONObject()
			.put(ScopeFields.PrivilegeList.text, JSONArray().put(privilegesListJsonObj))
			.put(ScopeFields.ScopeGroupType.text, scope.text)
			.put(ScopeFields.ConsentId.text, ApiConsts.ConsentId)
			.put(ScopeFields.ScopeTimeLimit.text, OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds))
			.put(ScopeFields.ThrottlingPolicy.text, ApiConsts.ThrottlingPolicyVal)
	}
	private fun handleResponse(jsonObject: JSONObject?) : Boolean{
		if(jsonObject == null)
			return false

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
			val domesticPaymentPriviledgeScopeDetailObj = PaymentSuppClass(transferData).toDomesticPaymentScopeDetialObjForAuth()
			privilegesListJsonObjToRet.put(ApiMethodes.PisDomestic.text, domesticPaymentPriviledgeScopeDetailObj)
		}

		return privilegesListJsonObjToRet
	}


	private fun getRequestForBundle(trDataList: List<TransferData>) : Request{
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
			.put(ApiReqFields.Scope.text, ScopeValues.Pis.text)
			.put(ApiReqFields.ScopeDetails.text,getBundleScopeDetailsObject(trDataList))
			.put(ApiReqFields.RedirectUri.text, ApiConsts.REDIRECT_URI)
			.put(ApiReqFields.State.text,stateValue)
		return ApiFunctions.bodyToRequest(BankUrls.AuthUrl, requestBodyJson, uuidStr)
	}
	private fun getBundleScopeDetailsObject(trDataList: List<TransferData>) : JSONObject{
		var amount = 0.0
		trDataList.forEach{
			if(it.amount != null){
				if(it.amount!! > 0.0)
					amount += it.amount!!
			}else{
				Log.e(TagProduction, "[getBundleScopeDetailsObject/${this.javaClass.name}] bad amount double")
			}
		}
		val amountStr = Utilities.doubleToTwoDigitsAfterCommaString(amount)

		val transfersArray = JSONArray()
		trDataList.forEach {
			val toAdd = PaymentSuppClass(it).toBundleJsonArrayElement()
			transfersArray.put(toAdd)
		}

		val methodeJsonObj = JSONObject()
			.put(ScopeDetailsFields.ScopeUsageLimit.text, ScopeUsageLimit.Single.text)
			.put(BundleFields.TransfersTotalAmount.text, amountStr)
			.put(BundleFields.TypeOfTransfers.text, BundleFields.Domestic.text)
			.put(BundleFields.DomesticTransfers.text, transfersArray)

		val privilegesArray = JSONArray()
			.put(JSONObject()
				.put(ApiMethodes.PisBundle.text, methodeJsonObj)
			)

		val scopeDetailsObj = JSONObject()
			.put(ScopeFields.PrivilegeList.text, privilegesArray)
			.put(ScopeFields.ScopeGroupType.text, ScopeValues.Pis.text)
			.put(ScopeFields.ConsentId.text, ApiConsts.ConsentId)
			.put(ScopeFields.ScopeTimeLimit.text, OmegaTime.getCurrentTime(ApiConsts.AuthUrlValidityTimeSeconds))
			.put(ScopeFields.ThrottlingPolicy.text, ApiConsts.ThrottlingPolicyVal)

		return scopeDetailsObj
	}
}