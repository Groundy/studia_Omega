package com.example.omega

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CodeServerApi {
	private enum class Fields(val text : String){
		Code("code"),
		Status("status"),
		Ok("ok"),
		//Failed("failed"),
		ErrorMsg("errorMsg"),
		ExpirationTime("expirationTime"),
		TransferData("transferData");
	}
	private enum class GateWay(val text : String) {
		Set("https://omegaserver.azurewebsites.net/OmegaServer-v1/setCode"),
		Get("https://omegaserver.azurewebsites.net/OmegaServer-v1/getCode"),
		Done("https://omegaserver.azurewebsites.net/OmegaServer-v1/codeDone"),
		Waiting("https://omegaserver.azurewebsites.net/OmegaServer-v1/waitCodeDone"),
	}

	companion object{
		private fun getInternalRequest(url : GateWay, requestBodyJson: JSONObject) : Request{
		val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
		val requestBodyStr = requestBodyJson.toString().toByteArray().toRequestBody(mediaType)

		val request = Request.Builder()
			.url(url.text)
			.post(requestBodyStr)
			.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
			.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
			.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
			.addHeader("x-request-id", ApiFunctions.getUUID())
			.addHeader("content-type", ApiConsts.CONTENT_TYPE)
			.addHeader("accept", ApiConsts.CONTENT_TYPE)

		return request.build()
	}

		suspend fun setCode(callerActivity: Activity, transferData: TransferData) : ServerSetCodeResponse?{
			val transferDataStr = transferData.toJsonObject()
			val body = JSONObject()
				.put("data", transferDataStr)
			val request = getInternalRequest(GateWay.Set, body)
			return try {
				val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
				val response = client.newCall(request).execute()
				val resBodyStr = response.body!!.string()
				val responseJson = JSONObject(resBodyStr)
				val success = responseJson.get(Fields.Status.text) == Fields.Ok.text
				if(!success){
					val errorMsg = responseJson.getString(Fields.ErrorMsg.text)
					withContext(Main){
						ActivityStarter.startOperationResultActivity(callerActivity, errorMsg)
						Log.e(Utilities.TagProduction, "[setCode/CodeServerApi] $errorMsg")
					}
					return null
				}

				val expTimeStamp = responseJson.getString(Fields.ExpirationTime.text)
				val code = responseJson.getInt(Fields.Code.text)
				val okResponse = ServerSetCodeResponse(code, expTimeStamp)
				okResponse
			}catch (e : Exception){
				Log.e(Utilities.TagProduction, "[setCode/CodeServerApi] error in obtaing code from azure app e=[$e]")
				withContext(Main){
					val msg = callerActivity.resources.getString(R.string.UserMsg_SERVER_UNAVAILABLE)
					ActivityStarter.startOperationResultActivity(callerActivity, msg)
				}
				null
			}
		}
		suspend fun getCodeData(callerActivity: Activity, code: Int) : TransferData?{
			val body = JSONObject()
				.put(Fields.Code.text, code)
			val request = getInternalRequest(GateWay.Get, body)
			return try {
				val response = OkHttpClient().newCall(request).execute()
				val responseJson = JSONObject(response.body!!.string())
				val success = responseJson.get(Fields.Status.text) == Fields.Ok.text
				if(!success){
					val errorMsg = responseJson.getString(Fields.ErrorMsg.text)
					withContext(Main){
						ActivityStarter.startOperationResultActivity(callerActivity, errorMsg)
						Log.e(Utilities.TagProduction, "[getCodeData/CodeServerApi] $errorMsg")
					}
					return null
				}

				var transferDataStr = responseJson.getString(Fields.TransferData.text)
				transferDataStr = transferDataStr.replace("\r","")
				transferDataStr = transferDataStr.replace("\n","")
				val transferData = TransferData.fromJsonSerialized(transferDataStr)
				transferData
			}catch (e : Exception){
				Log.e(Utilities.TagProduction, "[getCodeData/CodeServerApi] error in obtaing code from azure app e=[$e]")
				withContext(Main){
					ActivityStarter.startOperationResultActivity(callerActivity, "Serwer nieosiągalny")
				}
				null
			}

		}
		suspend fun codeDone(callerActivity: Activity, code: Int) : Boolean{
			val body = JSONObject()
				.put(Fields.Code.text, code)
			val request = getInternalRequest(GateWay.Done, body)
			try {
				val client = OkHttpClient.Builder().connectTimeout(ApiConsts.requestTimeOutMiliSeconds, TimeUnit.MILLISECONDS).build()
				val response = client.newCall(request).execute()
				val responseJson = JSONObject(response.body!!.string())
				val success = responseJson.get(Fields.Status.text) == Fields.Ok.text
				if(!success){
					val errorMsg = responseJson.getString(Fields.ErrorMsg.text)
					withContext(Main){
						ActivityStarter.startOperationResultActivity(callerActivity, errorMsg)
						Log.e(Utilities.TagProduction, "[codeDone/CodeServerApi] $errorMsg")
					}
					return false
				}
				return true
			}catch (e : Exception){
				Log.e(Utilities.TagProduction, "[getCodeData/CodeServerApi] error in obtaing code from azure app e=[$e]")
				return false
			}

		}
		suspend fun waitCodeDone(callerActivity: Activity, code: Int) : Boolean{
			//long request
			val body = JSONObject()
				.put(Fields.Code.text, code)
			val request = getInternalRequest(GateWay.Waiting, body)
			return try {
				val client = OkHttpClient.Builder().readTimeout(10, TimeUnit.MINUTES).build()
				val response = client.newCall(request).execute()
				val responseJson = JSONObject(response.body!!.string())
				val success = responseJson.get(Fields.Status.text) == Fields.Ok.text
				if(!success){
					val errorMsg = responseJson.getString(Fields.ErrorMsg.text)
					Log.e(Utilities.TagProduction, "[waitCodeDone/CodeServerApi] $errorMsg")
					false
				}
				true
			}catch (e : Exception){
				Log.e(Utilities.TagProduction, "[getCodeData/CodeServerApi] error in obtaing code from azure app e=[$e]")
				false
			}
		}
	}

}

class ServerSetCodeResponse{
	var code = -1
	var timestamp = String()
	override fun toString(): String {
		val json = JSONObject()
			.put("code", code)
			.put("timestamp", timestamp)
		return json.toString()
	}
	constructor(string: String){
		val json = JSONObject(string)
		code = json.getInt("code")
		timestamp = json.getString("timestamp")
	}
	constructor(code : Int, timestamp : String){
		this.code = code
		this.timestamp = timestamp
	}
}
