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
		fun getCurrentTimeStr(secFromNow : Int = 0): String {
			val c: Calendar = Calendar.getInstance()
			c.timeInMillis += 1000*secFromNow

			val y = (c.get(Calendar.YEAR)).toString()
			var m = (c.get(Calendar.MONTH) + 1).toString()//0-11 -> 1-12
			var d = (c.get(Calendar.DAY_OF_MONTH)).toString()
			var hh = (c.get(Calendar.HOUR_OF_DAY)).toString()
			var mm = (c.get(Calendar.MINUTE)).toString()
			var ss = (c.get(Calendar.SECOND)).toString()
			var zzz = (c.get(Calendar.MILLISECOND)).toString()

			if(m.length == 1)
				m = "0${m}"
			if(d.length == 1)
				d = "0${d}"
			if(hh.length == 1)
				hh = "0${hh}"
			if(mm.length == 1)
				mm = "0${mm}"
			if(ss.length == 1)
				ss = "0${ss}"
			if(zzz.length == 1)
				zzz = "00${zzz}"
			if(zzz.length == 2)
				zzz = "0${zzz}"

			return "${y}-${m}-${d}T${hh}:${mm}:${ss}.${zzz}Z"
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
			//return "213.134.179.174"		//tmp for speed
			var ip = ""
			val request: Request = Request.Builder()
				.url("https://wtfismyip.com/text")
				.build()
			val callbackRequest = object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					Log.e(Utilities.TagProduction, "bład pobierania publicznego ip z internetu")
				}
				override fun onResponse(call: Call, response: Response) {
					val bytes : ByteArray? = response.body?.bytes()
					if(bytes != null){
						val str = String(bytes, StandardCharsets.ISO_8859_1)
						if(str.length > 3){
							ip = str.take(str.length - 1) // delete newLine char
							Log.i(Utilities.TagProduction, "Publiczne ip użytkownika:${str}")
						}
					}
				}
			}
			val client = OkHttpClient()
			client.newCall(request).enqueue(callbackRequest)
			//TODO terrible solution but it works
			var tries = 20
			val timeMili = 100L
			while (tries > 0 && ip.isEmpty()){
				Thread.sleep(timeMili)
				tries--
			}
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
	}
}