package com.example.omega

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception
import okhttp3.RequestBody

import okhttp3.OkHttpClient
import org.json.JSONArray
import java.nio.charset.Charset
import kotlin.contracts.Returns


class TestClass {
	companion object{
		fun test_request(){
			val client = OkHttpClient()
			val mediaType = ("application/json").toMediaTypeOrNull()
			val body: RequestBody = RequestBody.create(
				mediaType,"{\"requestHeader\":{\"requestId\":\"4570454341713920\",\"sendDate\":\"2015-09-04T16:29:32.795Z\",\"tppId\":\"7912341490368512\",\"isCompanyContext\":false,\"psuIdentifierType\":\"1873422839709696\",\"psuIdentifierValue\":\"53.47\",\"psuContextIdentifierType\":\"8701101880639488\",\"psuContextIdentifierValue\":\"30.64\"},\"grant_type\":\"luwohi\",\"Code\":\"osnazujhihutcov\",\"redirect_uri\":\"http://azazada.pm/pi\",\"client_id\":\"2140926986158080\",\"refresh_token\":\"1f2339c9c784ef62713d1588360dc89a980b25197993e4d485b71afcacbe92c8\",\"exchange_token\":\"6f1b8542f0d55ab8a1113cfeaa3a3a13266aef0ebe2b485b614d27567ac16ecb\",\"scope\":\"fiwhahhigifado\",\"scope_details\":{\"privilegeList\":[{\"accountNumber\":\"4076032290964570\",\"ais-accounts:getAccounts\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getAccount\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getHolds\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":1044},\"ais:getTransactionsDone\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":946},\"ais:getTransactionsPending\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":148},\"ais:getTransactionsRejected\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":392},\"ais:getTransactionsCancelled\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":467},\"ais:getTransactionsScheduled\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":988},\"ais:getTransactionDetail\":{\"scopeUsageLimit\":\"single\"},\"pis:getPayment\":{\"scopeUsageLimit\":\"multiple\",\"paymentId\":\"4870696689729536\",\"tppTransactionId\":\"5253254776619008\"},\"pis:getBundle\":{\"scopeUsageLimit\":\"multiple\",\"bundleId\":\"3496681783951360\",\"tppBundleId\":\"2597798318964736\"},\"pis:domestic\":{\"scopeUsageLimit\":\"multiple\",\"recipient\":{\"accountNumber\":\"5610817729251783\",\"nameAddress\":{\"value\":[\"moenifelzeidnuelazegot\"]}},\"sender\":{\"accountNumber\":\"6334326465490392\",\"nameAddress\":{\"value\":[\"hunuvifopmifugilame\"]}},\"transferData\":{\"description\":\"Adho sa kajail wes abavudjol razezub das nuc doforev vomvit cacsurru sivladuco ukouvo baseamu mot roto.\",\"amount\":\"1107539638878208\",\"executionDate\":\"Wed Jul 29\",\"currency\":\"GYD\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"6868578378711040\",\"spTaxIdentificationNumber\":\"2456281166118912\",\"spTaxAmount\":\"591644808183808\",\"spDescription\":\"Dapivu laj tat negeppod muawubu cifbi ce mofeg da jetozuki riawala dowesfik.\"}},\"pis:EEA\":{\"scopeUsageLimit\":\"multiple\",\"recipient\":{\"accountNumber\":\"6011218360407573\",\"nameAddress\":{\"value\":[\"igsahis\"]},\"countryCode\":\"FM\"},\"sender\":{\"accountNumber\":\"340888503332279\",\"nameAddress\":{\"value\":[\"nujewmawuapocozdacujeciwsewwakeedmi\"]}},\"transferData\":{\"description\":\"Opgu it sulbim bakkamvu mumekih saowomu ludiju ev he elo jebeje mimu facese lavuprel hela go tegmemeto to.\",\"amount\":\"6155958076047360\",\"executionDate\":\"Tue Oct 20\",\"currency\":\"MXN\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"SEPA\",\"hold\":true,\"executionMode\":\"Immediate\"},\"pis:nonEEA\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"4482892373468197\",\"nameAddress\":{\"value\":[\"gozik\"]},\"countryCode\":\"MA\"},\"recipientBank\":{\"bicOrSwift\":\"bemsociabul\",\"name\":\"Ethel Rodriguez\",\"code\":\"jiwluhiumw\",\"countryCode\":\"VA\",\"address\":{\"value\":[\"visisedamiwkihahawenobufeicawovjuf\"]}},\"sender\":{\"accountNumber\":\"348697413066972\",\"nameAddress\":{\"value\":[\"tobzuadaowazihmikategeebuvoswalg\"]}},\"transferData\":{\"description\":\"Leppivpi arabulzot niv ru ah voz dap hi taug tuhnazow gedgofuf vazilet jul zo luron mis.\",\"amount\":\"8695065855655936\",\"executionDate\":\"Mon Jan 27\",\"currency\":\"XPF\"},\"transferCharges\":\"ni\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":false,\"executionMode\":\"FutureDated\"},\"pis:tax\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"36364247059932\",\"nameAddress\":{\"value\":[\"pehmenehivusfabemaosmawd\"]}},\"sender\":{\"accountNumber\":\"30067215994461\",\"nameAddress\":{\"value\":[\"fejoozumomdoowoseotmidgacuhe\"]}},\"transferData\":{\"amount\":\"414962075828224\",\"executionDate\":\"Sun Oct 28\",\"currency\":\"TZS\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"872575980797952\",\"payorIdType\":3},\"formCode\":\"ecata\",\"periodId\":\"1023959912939520\",\"periodType\":\"ahiwgo\",\"year\":9998,\"obligationId\":\"1057200692789248\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\"},\"pis:cancelPayment\":{\"scopeUsageLimit\":\"single\",\"paymentId\":\"1516012201574400\",\"bundleId\":\"32784036921344\"},\"pis:bundle\":{\"scopeUsageLimit\":\"single\",\"transfersTotalAmount\":\"6329189628116992\",\"typeOfTransfers\":\"tax\",\"domesticTransfers\":[{\"recipient\":{\"accountNumber\":\"201449854168693\",\"nameAddress\":{\"value\":[\"rudakiofodazzesofohouvad\"]}},\"sender\":{\"accountNumber\":\"36726714237235\",\"nameAddress\":{\"value\":[\"kiunkobomlawnozdonimkuiradoane\"]}},\"transferData\":{\"description\":\"Na ma zad ife ribhuasu dejis nuag mupsejdoz fothidmi co fazukuca tu pawomo izjo muzpicizo.\",\"amount\":\"584502996893696\",\"executionDate\":\"Sat May 21\",\"currency\":\"BHD\"},\"tppTransactionId\":\"7430427413315584\",\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"1158366808244224\",\"spTaxIdentificationNumber\":\"5411885165838336\",\"spTaxAmount\":\"1688149952561152\",\"spDescription\":\"Nilohhu bo lulnih kovuiva al ziisamad vofig robpabvoj lup iwridu woorve kicsavu.\"}}],\"EEATransfers\":[{\"recipient\":{\"accountNumber\":\"5177447497327303\",\"nameAddress\":{\"value\":[\"vulgotwerakjajnosafragnafjibvilciwa\"]},\"countryCode\":\"FO\"},\"sender\":{\"accountNumber\":\"5018915326110697\",\"nameAddress\":{\"value\":[\"wamudahbufaskaazucinim\"]}},\"transferData\":{\"description\":\"Ekuig tusnacwal acafijoz tup ofnef sijeh zusaama ocositum be ohugodig ehanim kilforum tihcav akda tosalop.\",\"amount\":\"3346429646995456\",\"executionDate\":\"Fri Feb 14\",\"currency\":\"AED\"},\"tppTransactionId\":\"6831315007569920\",\"deliveryMode\":\"StandardD1\",\"system\":\"InstantSEPA\",\"hold\":true,\"executionMode\":\"Immediate\"}],\"nonEEATransfers\":[{\"recipient\":{\"accountNumber\":\"6304346405567741\",\"nameAddress\":{\"value\":[\"nudh\"]},\"countryCode\":\"QA\"},\"recipientBank\":{\"bicOrSwift\":\"sutafeeb\",\"name\":\"Jonathan Griffith\",\"code\":\"fawicih\",\"countryCode\":\"GA\",\"address\":{\"value\":[\"uwutijtujamwalobsomawowce\"]}},\"sender\":{\"accountNumber\":\"6304789290625290\",\"nameAddress\":{\"value\":[\"rolwiduvigozarofukezjuwno\"]}},\"transferData\":{\"description\":\"Ogusafar zuduc nekuco davlega zildeti hurbe tefwirub suhzu sorunzi ej uf lu mujamnu pariha.\",\"amount\":\"7647385534595072\",\"executionDate\":\"Sat Nov 03\",\"currency\":\"BBD\"},\"transferCharges\":\"oj\",\"tppTransactionId\":\"6987825213865984\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":true,\"executionMode\":\"Immediate\"}],\"taxTransfers\":[{\"recipient\":{\"accountNumber\":\"4903930100711082\",\"nameAddress\":{\"value\":[\"nozupalawutufocluhpibeallahwu\"]}},\"sender\":{\"accountNumber\":\"6011011349684291\",\"nameAddress\":{\"value\":[\"lisrenarjeziorise\"]}},\"transferData\":{\"amount\":\"795430296223744\",\"executionDate\":\"Sun Feb 04\",\"currency\":\"THB\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"324706873901056\",\"payorIdType\":2},\"formCode\":\"zibpik\",\"periodId\":\"1807535784853504\",\"periodType\":\"vatev\",\"year\":9998,\"obligationId\":\"951490082504704\"},\"tppTransactionId\":\"7944593234460672\",\"deliveryMode\":\"StandardD1\",\"system\":\"Elixir\",\"hold\":false,\"executionMode\":\"FutureDated\"}]},\"pis:recurring\":{\"scopeUsageLimit\":\"single\",\"recurrence\":{\"startDate\":\"Sun Feb 17\",\"frequency\":{\"periodType\":\"week\",\"periodValue\":19137407},\"endDate\":\"Tue Nov 08\",\"dayOffOffsetType\":\"after\"},\"typeOfPayment\":\"nonEEA\",\"domesticPayment\":{\"recipient\":{\"accountNumber\":\"5146781370786740\",\"nameAddress\":{\"value\":[\"bojbuzacobn\"]}},\"sender\":{\"accountNumber\":\"6250516724752240\",\"nameAddress\":{\"value\":[\"gabzovehumirucuwamuhowapj\"]}},\"transferData\":{\"description\":\"Pu uzofodu vu ofnuc belsirzem uga koplofdul taw tajiznuc oruposa lod zuofo bojme ru me hirro vidasgo gekek.\",\"amount\":\"3797895964786688\",\"executionDate\":\"Fri Jan 07\",\"currency\":\"IMP\"},\"deliveryMode\":\"StandardD1\",\"system\":\"BlueCash\",\"hold\":true,\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"8756747646795776\",\"spTaxIdentificationNumber\":\"4119901660774400\",\"spTaxAmount\":\"836195250601984\",\"spDescription\":\"Mu amajo cu kiovo lopjofat enis udo zumorurom lafire wiwavdob notev sidaod.\"}},\"EEAPayment\":{\"recipient\":{\"accountNumber\":\"201486648957593\",\"nameAddress\":{\"value\":[\"akocitesemohuzjezzijebudup\"]},\"countryCode\":\"TT\"},\"sender\":{\"accountNumber\":\"4910790639364162\",\"nameAddress\":{\"value\":[\"wizohokmutcucvewannibfuffackaikagi\"]}},\"transferData\":{\"description\":\"Pebezca sap gejrol ih janicdem mot ko wokzew zezemakuz diin nezu sedizah jog.\",\"amount\":\"3598040732532736\",\"executionDate\":\"Thu May 15\",\"currency\":\"VND\"},\"deliveryMode\":\"StandardD1\",\"system\":\"Target\",\"hold\":false},\"nonEEAPayment\":{\"recipient\":{\"accountNumber\":\"340082527077686\",\"nameAddress\":{\"value\":[\"cajtestuigo\"]},\"countryCode\":\"NL\"},\"recipientBank\":{\"bicOrSwift\":\"irdofato\",\"name\":\"Lelia Martin\",\"code\":\"ejjih\",\"countryCode\":\"BI\",\"address\":{\"value\":[\"cokopoadbenappikucjajtobec\"]}},\"sender\":{\"accountNumber\":\"6011621078051072\",\"nameAddress\":{\"value\":[\"horzamzubobovzailhuangewvemamk\"]}},\"transferData\":{\"description\":\"Kafe wockeuzo ke zuit heraz kotug micmakec we valde jumzajwe uwe gifizdu mu.\",\"amount\":\"4468669283303424\",\"executionDate\":\"Sat Jul 12\",\"currency\":\"CHF\"},\"transferCharges\":\"pak\",\"deliveryMode\":\"UrgentD1\",\"system\":\"Swift\",\"hold\":true},\"taxPayment\":{\"recipient\":{\"accountNumber\":\"4026209536145716\",\"nameAddress\":{\"value\":[\"sivuva\"]}},\"sender\":{\"accountNumber\":\"5128214833841664\",\"nameAddress\":{\"value\":[\"atpoen\"]}},\"transferData\":{\"amount\":\"3187612093251584\",\"executionDate\":\"Mon Oct 13\",\"currency\":\"ILS\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"6593649068474368\",\"payorIdType\":3},\"formCode\":\"megsoco\",\"periodId\":\"1203152728621056\",\"periodType\":\"nosilze\",\"year\":9998,\"obligationId\":\"2969074466291712\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":true}},\"pis:getRecurringPayment\":{\"scopeUsageLimit\":\"single\",\"recurringPaymentId\":\"2454959765323776\",\"tppRecurringPaymentId\":\"7080453683019776\"},\"pis:cancelRecurringPayment\":{\"scopeUsageLimit\":\"multiple\",\"recurringPaymentId\":\"2415731874463744\"}}],\"scopeGroupType\":\"ais\",\"consentId\":\"8991838557962240\",\"scopeTimeLimit\":\"2011-03-30T11:45:30.703Z\",\"throttlingPolicy\":\"psd2Regulatory\"},\"is_user_session\":true,\"user_ip\":\"Myrtie\",\"user_agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36\"}")
			val body2 = RequestBody.create(mediaType,test_getTokenRequestBody().toString())
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token"
			val request: Request = Request.Builder()
				.url(url)
				.post(body2)
				.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
				.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
				.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
				.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
				.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
				.addHeader("x-jws-signature", ApiFuncs.getJWS("abc",body.toString()))
				.addHeader("x-request-id", ApiFuncs.getUUID())
				.addHeader("content-type", "application/json")
				.addHeader("accept", "application/json")
				.build()

			val response: Response = client.newCall(request).execute()
			val resStr = response.body?.byteString()
			val t = 5
		}
		fun doTestRequestInThread(){
			val thread = Thread {
				try {
					test_authorize()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			thread.start()
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
				.addHeader("x-request-id", ApiFuncs.getUUID())
				.addHeader("content-type", "application/json")
				.addHeader("accept", "application/json")
				.build()

			val response: Response = client.newCall(request).execute()
			val t = response.body?.byteString()
			val tt=3
		}
		fun test_getTokenRequestBody() : String{
			var tokenRequestJson : JSONObject = JSONObject()

			var headersArray = JSONObject()
			headersArray.put("requestId","4570454341713920")
			headersArray.put("sendDate", ApiFuncs.getCurrentTimeStr())
			//headersArray.put("tppId","7912341490368512")//tppID is required only for production, for sandbox it can be omitted
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
			tokenRequestJson.put("user_ip", ApiFuncs.getPublicIPByInternetService())
			tokenRequestJson.put("user_agent", ApiFuncs.getUserAgent())
			return tokenRequestJson.toString()
		}
		fun test_getScopeDetailsJsonObj() : JSONObject {
			var scopeDetails = JSONObject()
			return scopeDetails
		}



		fun test_authorize(){
			val uuidStr = ApiFuncs.getUUID()
			val url = "https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"
			val mediaType : MediaType? = ApiConsts.CONTENT_TYPE.toMediaTypeOrNull()
			var bodyStr = test_getAuthorizeRequestBody(uuidStr)

			val client = OkHttpClient()
			val body = bodyStr.toByteArray().toRequestBody(mediaType)
			val request = Request.Builder()
				.url(url)
				.post(body)
				.addHeader("x-ibm-client-id", ApiConsts.userId_ALIOR)
				.addHeader("x-ibm-client-secret", ApiConsts.appSecret_ALIOR)
				.addHeader("accept-encoding", ApiConsts.PREFERED_ENCODING)
				.addHeader("accept-language", ApiConsts.PREFERED_LAUNGAGE)
				.addHeader("accept-charset", ApiConsts.PREFERED_CHARSET)
				.addHeader("x-jws-signature", ApiFuncs.getJWS("key",body.toString()))
				.addHeader("x-request-id", uuidStr)
				.addHeader("content-type", ApiConsts.CONTENT_TYPE)
				.addHeader("accept", ApiConsts.CONTENT_TYPE)
				.build()
			val response = client.newCall(request).execute()

			val bodyResponse = response.body
			val c = bodyResponse?.contentLength()
			val str = bodyResponse?.byteString()
			val g = 2+2
		}
		fun test_getAuthorizeRequestBody(UUID : String) : String {
			val currentTimeStr = ApiFuncs.getCurrentTimeStr()
			val endValidityTimeStr = ApiFuncs.getCurrentTimeStr(600)

			var headersArray = JSONObject()
				headersArray.put("requestId", UUID)
				headersArray.put("userAgent", ApiFuncs.getUserAgent())
				headersArray.put("ipAddress", ApiFuncs.getPublicIPByInternetService())
				headersArray.put("sendDate", currentTimeStr)
				headersArray.put("tppId", "123456789")
				headersArray.put("isCompanyContext", false)
				headersArray.put("psuIdentifierType", "IDontKnowWhatShouldBeHere")
				headersArray.put("psuIdentifierValue", "IDontKnowWhatShouldBeHere")


			var privilegeObj = JSONObject()
				privilegeObj.put("accountNumber", "")//????????????
				var ttt = JSONObject()
					.put("scopeUsageLimit","multiple")
				privilegeObj.put("ais-accounts:getAccounts",ttt)


			var scopeDetailsObj = JSONObject()
				scopeDetailsObj.put("privilegeList", privilegeObj)
				scopeDetailsObj.put("scopeGroupType", "ais-accounts")
				scopeDetailsObj.put("consentId", "MYTPP-b3ae3d34")
				scopeDetailsObj.put("scopeTimeLimit", endValidityTimeStr)
				scopeDetailsObj.put("throttlingPolicy", "psd2Regulatory")



			var bodyJsonObj = JSONObject()
			bodyJsonObj.put("requestHeader",headersArray)
			bodyJsonObj.put("response_type","code")
			bodyJsonObj.put("client_id",ApiConsts.userId_ALIOR)
			bodyJsonObj.put("scope","ais-accounts")
			bodyJsonObj.put("scope_details",scopeDetailsObj)
			bodyJsonObj.put("redirect_uri",ApiConsts.REDIRECT_URI)
			bodyJsonObj.put("state","your state")//??????

			var toRet = bodyJsonObj.toString().replace("\\/","/")
			return "{${toRet}}" //wrapping obj to another JsonObj
		}
	}
}