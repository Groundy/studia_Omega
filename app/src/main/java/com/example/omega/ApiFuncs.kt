package com.example.omega
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





class ApiFuncs {
	companion object{
		fun generate_X_REQUEST_ID() : String{
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
		fun getCurrentTimeStr(): String {
			val c: Calendar = Calendar.getInstance()
			val Y = c.get(Calendar.YEAR)
			val M = c.get(Calendar.MONTH) + 1//0-11 -> 1-12
			val D = c.get(Calendar.DAY_OF_MONTH)
			val HH = c.get(Calendar.HOUR_OF_DAY)
			val MM = c.get(Calendar.MINUTE)
			val SS = c.get(Calendar.SECOND)
			val ZZZZ = c.get(Calendar.MILLISECOND)
			return "${Y}-${M}-${D}T${HH}:${MM}:${SS}.${ZZZZ}Z"
		}
		private fun getUserAgent() : String{
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
							Log.e("WookieTag", "Publiczne ip użytkownika:${str}")
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

		fun request_token(){
			val client = OkHttpClient()
			val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
			val body = "{\"requestHeader\":{\"requestId\":\"4990519373463552\",\"sendDate\":\"2014-04-19T10:11:17.465Z\",\"tppId\":\"2080189108453376\",\"isCompanyContext\":true,\"psuIdentifierType\":\"6010825531719680\",\"psuIdentifierValue\":\"72.17\",\"psuContextIdentifierType\":\"5886997744844800\",\"psuContextIdentifierValue\":\"34.02\"},\"grant_type\":\"adfa\",\"Code\":\"taeretsaduoja\",\"redirect_uri\":\"http://hevvovrev.cc/nobarpe\",\"client_id\":\"1646552563056640\",\"refresh_token\":\"6ae4d48122b0ff08cf0e56b62729862e4ffdce858a3406a4c7ef74207035bda7\",\"exchange_token\":\"337692c093d9427d97d58b66663f8b921fa764641cc798cd014f84725aa4821e\",\"scope\":\"bunifuguluci\",\"scope_details\":{\"privilegeList\":[{\"accountNumber\":\"4855727622977383\",\"ais-accounts:getAccounts\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getAccount\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getHolds\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":1412},\"ais:getTransactionsDone\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":508},\"ais:getTransactionsPending\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":61},\"ais:getTransactionsRejected\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":274},\"ais:getTransactionsCancelled\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":30},\"ais:getTransactionsScheduled\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":1131},\"ais:getTransactionDetail\":{\"scopeUsageLimit\":\"single\"},\"pis:getPayment\":{\"scopeUsageLimit\":\"multiple\",\"paymentId\":\"5981163210407936\",\"tppTransactionId\":\"7117093887541248\"},\"pis:getBundle\":{\"scopeUsageLimit\":\"single\",\"bundleId\":\"2438931415040000\",\"tppBundleId\":\"1131699169656832\"},\"pis:domestic\":{\"scopeUsageLimit\":\"multiple\",\"recipient\":{\"accountNumber\":\"4903961778266075\",\"nameAddress\":{\"value\":[\"uhicegepabujihfejifoagoicawo\"]}},\"sender\":{\"accountNumber\":\"36715102859171\",\"nameAddress\":{\"value\":[\"nobgadzawlakevizawot\"]}},\"transferData\":{\"description\":\"Gor olfab tonejih ujesoowu utunoodo zapige muzizub zooh ezufez kimu nehil na zoftodib tor.\",\"amount\":\"8403506899714048\",\"executionDate\":\"Wed Jan 16\",\"currency\":\"BAM\"},\"deliveryMode\":\"StandardD1\",\"system\":\"Sorbnet\",\"hold\":false,\"executionMode\":\"Immediate\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"4795582793121792\",\"spTaxIdentificationNumber\":\"249408190939136\",\"spTaxAmount\":\"3017604119658496\",\"spDescription\":\"Huvduhmis he ula dellegob zujafper bu dahfur ohduaf itafuh caso fav ez abtib ce zodpum wuklaf ge.\"}},\"pis:EEA\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"348058144852935\",\"nameAddress\":{\"value\":[\"wozelemhajrehutkeuvzu\"]},\"countryCode\":\"NL\"},\"sender\":{\"accountNumber\":\"6334146305340478\",\"nameAddress\":{\"value\":[\"domuwnujibdi\"]}},\"transferData\":{\"description\":\"Uhaletige ra diinne ik gemi rifoz lu ega capivi duheam zizel filuf oso fu gizbi ti tal.\",\"amount\":\"6822221641154560\",\"executionDate\":\"Fri Dec 07\",\"currency\":\"LAK\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"Target\",\"hold\":true,\"executionMode\":\"Immediate\"},\"pis:nonEEA\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"3528886796909470\",\"nameAddress\":{\"value\":[\"huksiacvamadomrefpanelooha\"]},\"countryCode\":\"SE\"},\"recipientBank\":{\"bicOrSwift\":\"beoki\",\"name\":\"Rosalie Byrd\",\"code\":\"titco\",\"countryCode\":\"KR\",\"address\":{\"value\":[\"hajoacfadumduwiliiridukiefawoblu\"]}},\"sender\":{\"accountNumber\":\"6304739211787810\",\"nameAddress\":{\"value\":[\"tesofulle\"]}},\"transferData\":{\"description\":\"Kub li biocne cad kihafov utunuksow efdonafu kaotogeg vuj cilguctip ole epefetu lujaer jifkuvi ovopu dusobu.\",\"amount\":\"3046782927896576\",\"executionDate\":\"Sun Sep 22\",\"currency\":\"ZAR\"},\"transferCharges\":\"fo\",\"deliveryMode\":\"ExpressD0\",\"system\":\"Swift\",\"hold\":false,\"executionMode\":\"Immediate\"},\"pis:tax\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"6374901659041051\",\"nameAddress\":{\"value\":[\"icokivzapolupnila\"]}},\"sender\":{\"accountNumber\":\"6296613435744685\",\"nameAddress\":{\"value\":[\"copziwefocenivhibzogb\"]}},\"transferData\":{\"amount\":\"288554806673408\",\"executionDate\":\"Wed Nov 02\",\"currency\":\"BYR\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"5525547520098304\",\"payorIdType\":1},\"formCode\":\"rinz\",\"periodId\":\"8281932731252736\",\"periodType\":\"awepaut\",\"year\":9998,\"obligationId\":\"3344049859723264\"},\"deliveryMode\":\"StandardD1\",\"system\":\"Elixir\",\"hold\":false,\"executionMode\":\"FutureDated\"},\"pis:cancelPayment\":{\"scopeUsageLimit\":\"multiple\",\"paymentId\":\"4644876849250304\",\"bundleId\":\"4669400567250944\"},\"pis:bundle\":{\"scopeUsageLimit\":\"multiple\",\"transfersTotalAmount\":\"325224528609280\",\"typeOfTransfers\":\"EEA\",\"domesticTransfers\":[{\"recipient\":{\"accountNumber\":\"6279576462746611\",\"nameAddress\":{\"value\":[\"dijafuzmutnusiltiv\"]}},\"sender\":{\"accountNumber\":\"5452345554076902\",\"nameAddress\":{\"value\":[\"secmijahhizupuwloftosokijenet\"]}},\"transferData\":{\"description\":\"Lese potemu colvi hug sebuzo pufoaso wat ijoen tidab pubbusufe hinosilib fireter ucpam ud palehiluv saudu.\",\"amount\":\"282910775050240\",\"executionDate\":\"Wed Jan 24\",\"currency\":\"HTG\"},\"tppTransactionId\":\"3716808473313280\",\"deliveryMode\":\"StandardD1\",\"system\":\"BlueCash\",\"hold\":false,\"executionMode\":\"Immediate\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"2269017513394176\",\"spTaxIdentificationNumber\":\"1017799791083520\",\"spTaxAmount\":\"2294590606409728\",\"spDescription\":\"Pamohdo ej fifgak ka eluwor unaka hikuc zenow bohjir rukonu ne vessu dema.\"}}],\"EEATransfers\":[{\"recipient\":{\"accountNumber\":\"36928998202691\",\"nameAddress\":{\"value\":[\"jepabobjebozuewpicihodrigi\"]},\"countryCode\":\"TO\"},\"sender\":{\"accountNumber\":\"6304231635056840\",\"nameAddress\":{\"value\":[\"andahaspidelpuskii\"]}},\"transferData\":{\"description\":\"Picmago hudejje zalo def isguhfi nozitat ejuce ev tetal ve riju pog ovi sollo an.\",\"amount\":\"5444972706791424\",\"executionDate\":\"Thu Jun 12\",\"currency\":\"COP\"},\"tppTransactionId\":\"2565960533803008\",\"deliveryMode\":\"ExpressD0\",\"system\":\"InstantSEPA\",\"hold\":true,\"executionMode\":\"Immediate\"}],\"nonEEATransfers\":[{\"recipient\":{\"accountNumber\":\"4903896395792246\",\"nameAddress\":{\"value\":[\"ulooruceraci\"]},\"countryCode\":\"GL\"},\"recipientBank\":{\"bicOrSwift\":\"fegopduitov\",\"name\":\"Joe Davis\",\"code\":\"tidhe\",\"countryCode\":\"SY\",\"address\":{\"value\":[\"howcidilupgadofopihjirowi\"]}},\"sender\":{\"accountNumber\":\"6304074670082218\",\"nameAddress\":{\"value\":[\"fogutturjobo\"]}},\"transferData\":{\"description\":\"Mokjupol toh sohena niuzju duter biviz dime taw evihu da kufjab idim we sevwe luzmur.\",\"amount\":\"4478632766472192\",\"executionDate\":\"Mon Jun 29\",\"currency\":\"NIO\"},\"transferCharges\":\"pos\",\"tppTransactionId\":\"3588977435082752\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":false,\"executionMode\":\"FutureDated\"}],\"taxTransfers\":[{\"recipient\":{\"accountNumber\":\"6286845862462546\",\"nameAddress\":{\"value\":[\"petgaw\"]}},\"sender\":{\"accountNumber\":\"4026267098222091\",\"nameAddress\":{\"value\":[\"witroodtahoihaj\"]}},\"transferData\":{\"amount\":\"5938593772077056\",\"executionDate\":\"Tue Oct 12\",\"currency\":\"DKK\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"576719161393152\",\"payorIdType\":2},\"formCode\":\"jowriw\",\"periodId\":\"1875342134345728\",\"periodType\":\"vunujv\",\"year\":9997,\"obligationId\":\"5531203788603392\"},\"tppTransactionId\":\"6879967486935040\",\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":true,\"executionMode\":\"FutureDated\"}]},\"pis:recurring\":{\"scopeUsageLimit\":\"multiple\",\"recurrence\":{\"startDate\":\"Fri Oct 01\",\"frequency\":{\"periodType\":\"week\",\"periodValue\":97170057},\"endDate\":\"Sun Jul 11\",\"dayOffOffsetType\":\"before\"},\"typeOfPayment\":\"domestic\",\"domesticPayment\":{\"recipient\":{\"accountNumber\":\"6011519579971055\",\"nameAddress\":{\"value\":[\"bawimjes\"]}},\"sender\":{\"accountNumber\":\"4026612658941947\",\"nameAddress\":{\"value\":[\"pevcuusozeritjakcozziklitiswunfu\"]}},\"transferData\":{\"description\":\"Ejavusade sisu ja fo am apo od uw ka zid lam kofu ijuini tovul wipihwu.\",\"amount\":\"8541894267109376\",\"executionDate\":\"Thu Aug 17\",\"currency\":\"SZL\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":true,\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"2891238166495232\",\"spTaxIdentificationNumber\":\"7128670684577792\",\"spTaxAmount\":\"1221725559717888\",\"spDescription\":\"La labzawko nikma kocavco osoduhfi fabkon ve ulwac unijo koktetwen giefib igzihme zed arlelu uspev va.\"}},\"EEAPayment\":{\"recipient\":{\"accountNumber\":\"5478647134951904\",\"nameAddress\":{\"value\":[\"adukalew\"]},\"countryCode\":\"TT\"},\"sender\":{\"accountNumber\":\"347804558725823\",\"nameAddress\":{\"value\":[\"pakvuvtig\"]}},\"transferData\":{\"description\":\"Pobvon ce va vefu lijenab alu ekar tihgatowo epi sozsaldu pon kiguwmiv nozben mogvib uvufe iruninuv lir.\",\"amount\":\"6995455697223680\",\"executionDate\":\"Sun Jan 16\",\"currency\":\"JEP\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"Target\",\"hold\":false},\"nonEEAPayment\":{\"recipient\":{\"accountNumber\":\"5018147965157132\",\"nameAddress\":{\"value\":[\"lokvurlupsizaog\"]},\"countryCode\":\"IO\"},\"recipientBank\":{\"bicOrSwift\":\"hibceradp\",\"name\":\"Estelle Brooks\",\"code\":\"curneks\",\"countryCode\":\"DK\",\"address\":{\"value\":[\"hajsadnavfeonown\"]}},\"sender\":{\"accountNumber\":\"4903554529317378\",\"nameAddress\":{\"value\":[\"zevokeladekiklehirubaken\"]}},\"transferData\":{\"description\":\"Zip ho na jis ejeuf sukpebi vad gihiplu ba notka hi pera ceme coedediw kerpan.\",\"amount\":\"3893121798635520\",\"executionDate\":\"Sun Sep 10\",\"currency\":\"MOP\"},\"transferCharges\":\"ja\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":false},\"taxPayment\":{\"recipient\":{\"accountNumber\":\"4039928516589065\",\"nameAddress\":{\"value\":[\"pozlawojvubupiddogd\"]}},\"sender\":{\"accountNumber\":\"5610505511940248\",\"nameAddress\":{\"value\":[\"feagrulamekiwoafecilif\"]}},\"transferData\":{\"amount\":\"3434103051386880\",\"executionDate\":\"Thu May 31\",\"currency\":\"CNY\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"1214749175971840\",\"payorIdType\":\"R\"},\"formCode\":\"cigdag\",\"periodId\":\"5740170963845120\",\"periodType\":\"wuvho\",\"year\":9998,\"obligationId\":\"3477986525315072\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"Elixir\",\"hold\":true}},\"pis:getRecurringPayment\":{\"scopeUsageLimit\":\"single\",\"recurringPaymentId\":\"6061094328074240\",\"tppRecurringPaymentId\":\"6104493802389504\"},\"pis:cancelRecurringPayment\":{\"scopeUsageLimit\":\"multiple\",\"recurringPaymentId\":\"6250883283156992\"}}],\"scopeGroupType\":\"pis\",\"consentId\":\"6781912345477120\",\"scopeTimeLimit\":\"2010-10-18T12:32:35.299Z\",\"throttlingPolicy\":\"psd2Regulatory\"},\"is_user_session\":false,\"user_ip\":\"Jeffery\",\"user_agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36\"}"
				.toRequestBody(mediaType)
			val request: Request = Request.Builder()
				.url("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token")
				.post(body)
				.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
				.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
				.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
				.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
				.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
				.addHeader("x-jws-signature", ApiFuncs.getJWS("test",body.toString()))
				.addHeader("x-request-id", ApiFuncs.generate_X_REQUEST_ID())
				.addHeader("content-type", "application/json")
				.addHeader("accept", "application/json")
				.build()

			val response: Response = client.newCall(request).execute()
			val t = response.body?.byteString()
			val tt=3
		}





		fun tesst_getTokenRequestBody() : String{
			var tokenRequestJson : JSONObject = JSONObject()

			var headersArray = JSONObject()
			headersArray.put("requestId","4570454341713920")
			headersArray.put("sendDate", getCurrentTimeStr())
			headersArray.put("tppId","7912341490368512")
			headersArray.put("isCompanyContext",false)
			headersArray.put("psuIdentifierType","1873422839709696")
			headersArray.put("psuIdentifierValue","53.47")
			headersArray.put("psuContextIdentifierType","8701101880639488")
			headersArray.put("psuContextIdentifierValue","30.64")
			tokenRequestJson.put("requestHeader",headersArray)

			tokenRequestJson.put("grant_type","luwohi")
			tokenRequestJson.put("Code","osnazujhihutcov")
			tokenRequestJson.put("redirect_uri",ApiConsts.REDIRECT_URI)
			tokenRequestJson.put("client_id","2140926986158080")
			tokenRequestJson.put("refresh_token","1f2339c9c784ef62713d1588360dc89a980b25197993e4d485b71afcacbe92c8")
			tokenRequestJson.put("exchange_token","6f1b8542f0d55ab8a1113cfeaa3a3a13266aef0ebe2b485b614d27567ac16ecb")
			tokenRequestJson.put("scope","fiwhahhigifado")


			tokenRequestJson.put("scope_details", test_getScopeDetailsJsonObj())

			tokenRequestJson.put("is_user_session",true)
			tokenRequestJson.put("user_ip", getPublicIPByInternetService())
			tokenRequestJson.put("user_agent",getUserAgent())
			return tokenRequestJson.toString()
		}
		fun test_getScopeDetailsJsonObj() : JSONObject{
			var scopeDetails = JSONObject()
			return scopeDetails
		}
	}

}