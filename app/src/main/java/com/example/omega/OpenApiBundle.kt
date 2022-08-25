package com.example.omega

import android.app.Activity
import android.util.Log
import com.example.omega.ApiConsts.ApiReqFields.*
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenApiBundle(private val callerActivity : Activity, private val paymentToken : Token) {
	companion object{
		private enum class BundleFields(val text : String){
			TppBundleId("tppBundleId"),
			TransfersTotalAmount("transfersTotalAmount"),
			TypeOfTransfers("typeOfTransfers"),
			DomesticTransfers("domesticTransfers"),
			Domestic("domestic")
		}
	}
	suspend fun run() : Boolean{
		return try {
			Log.i(TagProduction, "Bundle started")
			val request = getRequest()
			val errorStr = sendRequest(request)
			if(errorStr == null){
				Log.i(TagProduction, "Bundle ended with success")
				true
			}
			else{
				Log.e(TagProduction, "Bundle ended with failure")
				withContext(Main){
					ActivityStarter.startOperationResultActivity(callerActivity, errorStr)
				}
				false
			}
		}catch (e : Exception){
			Log.e(TagProduction, "Bundle ended with failure, e=$e")
			withContext(Main){
				val msg = callerActivity.resources.getString(R.string.UserMsg_UNKNOWN_ERROR)
				ActivityStarter.startOperationResultActivity(callerActivity, msg)
			}
			false
		}
	}
	private fun getRequest() : Request{
		val uuidStr = ApiFunctions.getUUID()
		val currentTime = OmegaTime.getCurrentTime()
		val authFieldValue = paymentToken.getAuthFieldValue()
		val trDataList = TransferData.fromBundleToken(paymentToken)

		var totalAmount = 0.0
		trDataList.forEach{
			if(it.amount != null)
				totalAmount += it.amount!!
			else{
				Log.e(TagProduction,"[getRequest/${this.javaClass.name}] error, one trData passed to bundleClass has null amount")
			}
		}
		val totalAmountStr = Utilities.doubleToTwoDigitsAfterCommaString(totalAmount)

		val trasferArray = JSONArray()
		trDataList.forEach{
			val toAdd = PaymentSuppClass(it).toBundleJsonArrayElement()
			trasferArray.put(toAdd)
		}

		val bodyHeaders = JSONObject()
		with(bodyHeaders){
			put(RequestId.text, uuidStr)
			put(UserAgent.text, ApiFunctions.getUserAgent())
			put(IpAddress.text, ApiFunctions.getPublicIPByInternetService(callerActivity))
			put(SendDate.text, currentTime)
			put(TppId.text, ApiConsts.TTP_ID)
			put(IsCompanyContext.text, false)
			put(TokenField.text, authFieldValue)
		}

		val body = JSONObject()
		with(body){
			put(RequestHeader.text,bodyHeaders)
			put(BundleFields.TppBundleId.text, ApiFunctions.getRandomValueForTppTransId())
			put(BundleFields.TransfersTotalAmount.text, totalAmountStr)
			put(BundleFields.TypeOfTransfers.text, BundleFields.Domestic.text)
			put(BundleFields.DomesticTransfers.text, trasferArray)
		}

		return ApiFunctions.bodyToRequest(ApiConsts.BankUrls.Bundle, body, uuidStr, authFieldValue)
	}
	private fun sendRequest(request: Request) : String?{
		return try{
			val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
			val response = client.newCall(request).execute()
			if(response.code!= ApiConsts.ResponseCodes.OK.code){
				ApiFunctions.logResponseError(response, this.javaClass.name)
				"Nieznany błąd ${response.code}"
			}
			null
		}catch (e : Exception){
			Log.e(TagProduction,"[sendRequest/${this.javaClass.name}] Error catch $e")
			"Nieznany błąd"
		}
	}
}