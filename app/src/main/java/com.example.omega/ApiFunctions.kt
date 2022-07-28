package com.example.omega

import android.os.Build
import android.util.Log
import okhttp3.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import com.fasterxml.uuid.Generators
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiFunctions {
	companion object{
		fun bodyToRequest(url : String, requestBodyJson: JSONObject, uuidStr : String, additionalHeaders: List<Pair<String,String>>? = null): Request {
			val mediaType : MediaType = ApiConsts.CONTENT_TYPE.toMediaType()
			val requestBodyStr = requestBodyJson.toString().toByteArray().toRequestBody(mediaType)

			val request = Request.Builder()
				.url(url)
				.post(requestBodyStr)
				.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
				.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
				.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
				.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
				.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
				//.addHeader("x-jws-signature", ApiFuncs.getJWS(bodyStr))
				.addHeader("x-request-id", uuidStr)
				.addHeader("content-type", ApiConsts.CONTENT_TYPE)
				.addHeader("accept", ApiConsts.CONTENT_TYPE)

			if(!additionalHeaders.isNullOrEmpty()){
				val size = additionalHeaders.size
				for(i in 0 until size){
					val headerTitle = additionalHeaders[i].first
					val headerValue = additionalHeaders[i].second
					request.addHeader(headerTitle,headerValue)
				}
			}

			return request.build()
		}
		fun getUUID() : String{
			return Generators.timeBasedGenerator().generate().toString()
		}
		fun getUserAgent() : String{
			val result = StringBuilder(64)
			result.append("Dalvik/")
			result.append(System.getProperty("java.vm.version")) // such as 1.1.0
			result.append(" (Linux; U; Android ")
			val version = Build.VERSION.RELEASE // "1.0" or "3.4b5"
			result.append(version.ifEmpty { "1.0" })
			// add the model for the release build
			if ("REL" == Build.VERSION.CODENAME) {
				val model = Build.MODEL
				if (model.isNotEmpty()) {
					result.append("; ")
					result.append(model)
				}
			}
			val id = Build.ID // "MASTER" or "M4-rc20"
			if (id.isNotEmpty()) {
				result.append(" Build/")
				result.append(id)
			}
			result.append(")")
			return result.toString()
		}

		fun getPublicIPByInternetService() : String{
			var ip = "0.0.0.0"
			val thread =Thread{
				try {
					val url = "http://www.ip-api.com/json"
					val request = Request.Builder().url(url).build()
					val client = OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build()
					val response = client.newCall(request).execute()
					val responseBody = response.body?.string()
					val responseJsonObject = JSONObject(responseBody!!)
					ip = responseJsonObject.getString("query")
					Log.i(Utilities.TagProduction, "Pobrano publiczne ip z internetu: $ip")
					return@Thread
				}catch (e : Exception){
					Log.e(Utilities.TagProduction, "bład pobierania publicznego ip z internetu ---> $e")
				}
			}
			thread.start()
			thread.join(3 * ApiConsts.requestTimeOut)
			return ip
		}
		fun getRandomStateValue(length: Int = 13) : String{
			val availableChars="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			var toRet = ""
			repeat(length){
				val randomIndex = Random().nextInt(length)
				toRet += availableChars[randomIndex]
			}
			return toRet
		}
		fun getLengthOfCountryBankNumberDigitsOnly(country : ApiConsts.Countries = ApiConsts.Countries.PL) : Int{
			return country.codeLength
		}
	}
}