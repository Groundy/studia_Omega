package com.example.omega
import android.Manifest
import android.app.Activity
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.internal.wait
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread
import android.R.string.no
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.file.Files
import android.os.Environment





class ApiFuncs {
	companion object{
		fun getUUID() : String{
			val random = Random()
			val random63BitLong = random.nextLong() and 0x3FFFFFFFFFFFFFFFL
			val variant1BitFlag : Long = (-0x6000000000000000L).toLong()
			val least64SigBits = random63BitLong + variant1BitFlag

			val start: LocalDateTime = LocalDateTime.of(1582, 10, 15, 0, 0, 0)
			val duration: Duration = Duration.between(start, LocalDateTime.now())
			val timeForUuidIn100Nanos = duration.seconds * 10000000 + duration.nano * 100
			val least12SignificantBitOfTime = timeForUuidIn100Nanos and 0x000000000000FFFFL shr 4
			val version = (1 shl 12).toLong()
			val most64SigBits = (timeForUuidIn100Nanos and -0x10000L) + version + least12SignificantBitOfTime

			val uuid = UUID(most64SigBits, least64SigBits)
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
		fun getJWS(key: String, payload: String): String {
			val toRet = Jwts.builder()
				.setPayload(payload)
				.compressWith(CompressionCodecs.DEFLATE)
				.signWith(SignatureAlgorithm.HS512, key)
				.compact()
			return toRet
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
			//TODO for speed
			//return "1.2.3.4"

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