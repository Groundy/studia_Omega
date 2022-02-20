package com.example.omega

import android.os.Build
import android.os.Environment
import android.util.Log
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import io.jsonwebtoken.security.Keys
import java.security.Key
import com.fasterxml.uuid.Generators




class ApiFuncs {
	companion object{
		fun getUUID() : String{
			val uuid = Generators.timeBasedGenerator().generate()
			return uuid.toString()
		}
		fun getCurrentTimeStr(secFromNow : Int = 0): String {
			val c: Calendar = Calendar.getInstance()
			c.timeInMillis += 1000*secFromNow

			var Y = (c.get(Calendar.YEAR)).toString()
			var M = (c.get(Calendar.MONTH) + 1).toString()//0-11 -> 1-12
			var D = (c.get(Calendar.DAY_OF_MONTH)).toString()
			var HH = (c.get(Calendar.HOUR_OF_DAY)).toString()
			var MM = (c.get(Calendar.MINUTE)).toString()
			var SS = (c.get(Calendar.SECOND)).toString()
			var ZZZ = (c.get(Calendar.MILLISECOND)).toString()

			if(M.length == 1)
				M = "0${M}"
			if(D.length == 1)
				D = "0${D}"
			if(HH.length == 1)
				HH = "0${HH}"
			if(MM.length == 1)
				MM = "0${MM}"
			if(SS.length == 1)
				SS = "0${SS}"
			if(ZZZ.length == 1)
				ZZZ = "00${ZZZ}"
			if(ZZZ.length == 2)
				ZZZ = "0${ZZZ}"

			return "${Y}-${M}-${D}T${HH}:${MM}:${SS}.${ZZZ}Z"
		}
		fun getUserAgent() : String{
			val result = StringBuilder(64)
			result.append("Dalvik/")
			result.append(System.getProperty("java.vm.version")) // such as 1.1.0
			result.append(" (Linux; U; Android ")
			val version = Build.VERSION.RELEASE // "1.0" or "3.4b5"
			result.append(if (version.length > 0) version else "1.0")
			// add the model for the release build
			if ("REL" == Build.VERSION.CODENAME) {
				val model = Build.MODEL
				if (model.length > 0) {
					result.append("; ")
					result.append(model)
				}
			}
			val id = Build.ID // "MASTER" or "M4-rc20"
			if (id.length > 0) {
				result.append(" Build/")
				result.append(id)
			}
			result.append(")")
			return result.toString()
		}
		fun getJWS(payload : String): String {
			val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
			val jws = Jwts.builder()
				.setSubject(payload)
				//.setHeaderParam("kid", "ttt")
				//.setHeaderParam("x5t#S256","aa")
				.signWith(key)
				.compact()
			return jws
		}
		fun getLocalIpAddress(): String {
			try {
				val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
				while (en.hasMoreElements()) {
					val netInterface: NetworkInterface = en.nextElement()
					val enumIpAddr: Enumeration<InetAddress> = netInterface.inetAddresses
					while (enumIpAddr.hasMoreElements()) {
						val inetAddress: InetAddress = enumIpAddr.nextElement()
						val isNotLoopBack = !inetAddress.isLoopbackAddress
						val isV4Type = inetAddress is Inet4Address
						if (isNotLoopBack && isV4Type){
							Log.i("WookieTag", "User Ip: ${inetAddress.hostAddress}")
							return inetAddress.hostAddress
						}
					}
				}
			} catch (ex: Exception) {
				Log.e("WookieTag","Error(getLocalIpAddress) ${ex.toString()}")
			}
			return ""
		}
		fun getPublicIPByInternetService() : String{
			return "213.134.179.174"		//TODO for speed

			var ip = ""
			val request: Request = Request.Builder()
				.url("https://wtfismyip.com/text")
				.build()
			val callbackRequest = object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					Log.e("WookieTag", "bład pobierania publicznego ip z internetu")
				}
				override fun onResponse(call: Call, response: Response) {
					val bytes : ByteArray? = response.body?.bytes()
					if(bytes != null){
						val str = String(bytes, StandardCharsets.ISO_8859_1)
						if(str.length > 3){
							ip = str.take(str.length - 1) // delete newLine char
							Log.i("WookieTag", "Publiczne ip użytkownika:${str}")
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
		fun saveJsonToHardDriver(jsonObjContent : String, fileNameToSet: String = ""){
			var fileName = ""
			if(fileNameToSet.length == 0){
				var fileName = ApiFuncs.getCurrentTimeStr()
					.replace('-','_')
					.replace(':','_')
					.replace('.','_')
				fileName += ".json"
			}
			else
				fileName = fileNameToSet

			//Checking the availability state of the External Storage.
			val state = Environment.getExternalStorageState()
			if (Environment.MEDIA_MOUNTED != state) {
				return
			}

			//Create a new file that points to the root directory, with the given name:
			val file: File = File(ApiConsts.pathToSaveFolder, fileName)

			//This point and below is responsible for the write operation
			var outputStream: FileOutputStream? = null
			try {
				file.createNewFile()
				//second argument of FileOutputStream constructor indicates whether
				//to append or create new file if one exists
				outputStream = FileOutputStream(file, true)
				val data = jsonObjContent.toString().replace("\\/","/").toByteArray()
				outputStream.write(data)
				outputStream.flush()
				outputStream.close()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}