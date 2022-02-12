package com.example.omega

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.lang.Exception
import org.json.JSONObject

import org.json.JSONArray

import org.json.JSONException







class ApiMethodes {
	companion object{
		fun test_request(){
			val client = OkHttpClient()

			val mediaType = ("application/json").toMediaTypeOrNull()
			val body: RequestBody = RequestBody.create(
				mediaType,"{\"requestHeader\":{\"requestId\":\"4570454341713920\",\"sendDate\":\"2015-09-04T16:29:32.795Z\",\"tppId\":\"7912341490368512\",\"isCompanyContext\":false,\"psuIdentifierType\":\"1873422839709696\",\"psuIdentifierValue\":\"53.47\",\"psuContextIdentifierType\":\"8701101880639488\",\"psuContextIdentifierValue\":\"30.64\"},\"grant_type\":\"luwohi\",\"Code\":\"osnazujhihutcov\",\"redirect_uri\":\"http://azazada.pm/pi\",\"client_id\":\"2140926986158080\",\"refresh_token\":\"1f2339c9c784ef62713d1588360dc89a980b25197993e4d485b71afcacbe92c8\",\"exchange_token\":\"6f1b8542f0d55ab8a1113cfeaa3a3a13266aef0ebe2b485b614d27567ac16ecb\",\"scope\":\"fiwhahhigifado\",\"scope_details\":{\"privilegeList\":[{\"accountNumber\":\"4076032290964570\",\"ais-accounts:getAccounts\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getAccount\":{\"scopeUsageLimit\":\"multiple\"},\"ais:getHolds\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":1044},\"ais:getTransactionsDone\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":946},\"ais:getTransactionsPending\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":148},\"ais:getTransactionsRejected\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":392},\"ais:getTransactionsCancelled\":{\"scopeUsageLimit\":\"multiple\",\"maxAllowedHistoryLong\":467},\"ais:getTransactionsScheduled\":{\"scopeUsageLimit\":\"single\",\"maxAllowedHistoryLong\":988},\"ais:getTransactionDetail\":{\"scopeUsageLimit\":\"single\"},\"pis:getPayment\":{\"scopeUsageLimit\":\"multiple\",\"paymentId\":\"4870696689729536\",\"tppTransactionId\":\"5253254776619008\"},\"pis:getBundle\":{\"scopeUsageLimit\":\"multiple\",\"bundleId\":\"3496681783951360\",\"tppBundleId\":\"2597798318964736\"},\"pis:domestic\":{\"scopeUsageLimit\":\"multiple\",\"recipient\":{\"accountNumber\":\"5610817729251783\",\"nameAddress\":{\"value\":[\"moenifelzeidnuelazegot\"]}},\"sender\":{\"accountNumber\":\"6334326465490392\",\"nameAddress\":{\"value\":[\"hunuvifopmifugilame\"]}},\"transferData\":{\"description\":\"Adho sa kajail wes abavudjol razezub das nuc doforev vomvit cacsurru sivladuco ukouvo baseamu mot roto.\",\"amount\":\"1107539638878208\",\"executionDate\":\"Wed Jul 29\",\"currency\":\"GYD\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"6868578378711040\",\"spTaxIdentificationNumber\":\"2456281166118912\",\"spTaxAmount\":\"591644808183808\",\"spDescription\":\"Dapivu laj tat negeppod muawubu cifbi ce mofeg da jetozuki riawala dowesfik.\"}},\"pis:EEA\":{\"scopeUsageLimit\":\"multiple\",\"recipient\":{\"accountNumber\":\"6011218360407573\",\"nameAddress\":{\"value\":[\"igsahis\"]},\"countryCode\":\"FM\"},\"sender\":{\"accountNumber\":\"340888503332279\",\"nameAddress\":{\"value\":[\"nujewmawuapocozdacujeciwsewwakeedmi\"]}},\"transferData\":{\"description\":\"Opgu it sulbim bakkamvu mumekih saowomu ludiju ev he elo jebeje mimu facese lavuprel hela go tegmemeto to.\",\"amount\":\"6155958076047360\",\"executionDate\":\"Tue Oct 20\",\"currency\":\"MXN\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"SEPA\",\"hold\":true,\"executionMode\":\"Immediate\"},\"pis:nonEEA\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"4482892373468197\",\"nameAddress\":{\"value\":[\"gozik\"]},\"countryCode\":\"MA\"},\"recipientBank\":{\"bicOrSwift\":\"bemsociabul\",\"name\":\"Ethel Rodriguez\",\"code\":\"jiwluhiumw\",\"countryCode\":\"VA\",\"address\":{\"value\":[\"visisedamiwkihahawenobufeicawovjuf\"]}},\"sender\":{\"accountNumber\":\"348697413066972\",\"nameAddress\":{\"value\":[\"tobzuadaowazihmikategeebuvoswalg\"]}},\"transferData\":{\"description\":\"Leppivpi arabulzot niv ru ah voz dap hi taug tuhnazow gedgofuf vazilet jul zo luron mis.\",\"amount\":\"8695065855655936\",\"executionDate\":\"Mon Jan 27\",\"currency\":\"XPF\"},\"transferCharges\":\"ni\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":false,\"executionMode\":\"FutureDated\"},\"pis:tax\":{\"scopeUsageLimit\":\"single\",\"recipient\":{\"accountNumber\":\"36364247059932\",\"nameAddress\":{\"value\":[\"pehmenehivusfabemaosmawd\"]}},\"sender\":{\"accountNumber\":\"30067215994461\",\"nameAddress\":{\"value\":[\"fejoozumomdoowoseotmidgacuhe\"]}},\"transferData\":{\"amount\":\"414962075828224\",\"executionDate\":\"Sun Oct 28\",\"currency\":\"TZS\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"872575980797952\",\"payorIdType\":3},\"formCode\":\"ecata\",\"periodId\":\"1023959912939520\",\"periodType\":\"ahiwgo\",\"year\":9998,\"obligationId\":\"1057200692789248\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\"},\"pis:cancelPayment\":{\"scopeUsageLimit\":\"single\",\"paymentId\":\"1516012201574400\",\"bundleId\":\"32784036921344\"},\"pis:bundle\":{\"scopeUsageLimit\":\"single\",\"transfersTotalAmount\":\"6329189628116992\",\"typeOfTransfers\":\"tax\",\"domesticTransfers\":[{\"recipient\":{\"accountNumber\":\"201449854168693\",\"nameAddress\":{\"value\":[\"rudakiofodazzesofohouvad\"]}},\"sender\":{\"accountNumber\":\"36726714237235\",\"nameAddress\":{\"value\":[\"kiunkobomlawnozdonimkuiradoane\"]}},\"transferData\":{\"description\":\"Na ma zad ife ribhuasu dejis nuag mupsejdoz fothidmi co fazukuca tu pawomo izjo muzpicizo.\",\"amount\":\"584502996893696\",\"executionDate\":\"Sat May 21\",\"currency\":\"BHD\"},\"tppTransactionId\":\"7430427413315584\",\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":false,\"executionMode\":\"FutureDated\",\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"1158366808244224\",\"spTaxIdentificationNumber\":\"5411885165838336\",\"spTaxAmount\":\"1688149952561152\",\"spDescription\":\"Nilohhu bo lulnih kovuiva al ziisamad vofig robpabvoj lup iwridu woorve kicsavu.\"}}],\"EEATransfers\":[{\"recipient\":{\"accountNumber\":\"5177447497327303\",\"nameAddress\":{\"value\":[\"vulgotwerakjajnosafragnafjibvilciwa\"]},\"countryCode\":\"FO\"},\"sender\":{\"accountNumber\":\"5018915326110697\",\"nameAddress\":{\"value\":[\"wamudahbufaskaazucinim\"]}},\"transferData\":{\"description\":\"Ekuig tusnacwal acafijoz tup ofnef sijeh zusaama ocositum be ohugodig ehanim kilforum tihcav akda tosalop.\",\"amount\":\"3346429646995456\",\"executionDate\":\"Fri Feb 14\",\"currency\":\"AED\"},\"tppTransactionId\":\"6831315007569920\",\"deliveryMode\":\"StandardD1\",\"system\":\"InstantSEPA\",\"hold\":true,\"executionMode\":\"Immediate\"}],\"nonEEATransfers\":[{\"recipient\":{\"accountNumber\":\"6304346405567741\",\"nameAddress\":{\"value\":[\"nudh\"]},\"countryCode\":\"QA\"},\"recipientBank\":{\"bicOrSwift\":\"sutafeeb\",\"name\":\"Jonathan Griffith\",\"code\":\"fawicih\",\"countryCode\":\"GA\",\"address\":{\"value\":[\"uwutijtujamwalobsomawowce\"]}},\"sender\":{\"accountNumber\":\"6304789290625290\",\"nameAddress\":{\"value\":[\"rolwiduvigozarofukezjuwno\"]}},\"transferData\":{\"description\":\"Ogusafar zuduc nekuco davlega zildeti hurbe tefwirub suhzu sorunzi ej uf lu mujamnu pariha.\",\"amount\":\"7647385534595072\",\"executionDate\":\"Sat Nov 03\",\"currency\":\"BBD\"},\"transferCharges\":\"oj\",\"tppTransactionId\":\"6987825213865984\",\"deliveryMode\":\"StandardD2\",\"system\":\"Swift\",\"hold\":true,\"executionMode\":\"Immediate\"}],\"taxTransfers\":[{\"recipient\":{\"accountNumber\":\"4903930100711082\",\"nameAddress\":{\"value\":[\"nozupalawutufocluhpibeallahwu\"]}},\"sender\":{\"accountNumber\":\"6011011349684291\",\"nameAddress\":{\"value\":[\"lisrenarjeziorise\"]}},\"transferData\":{\"amount\":\"795430296223744\",\"executionDate\":\"Sun Feb 04\",\"currency\":\"THB\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"324706873901056\",\"payorIdType\":2},\"formCode\":\"zibpik\",\"periodId\":\"1807535784853504\",\"periodType\":\"vatev\",\"year\":9998,\"obligationId\":\"951490082504704\"},\"tppTransactionId\":\"7944593234460672\",\"deliveryMode\":\"StandardD1\",\"system\":\"Elixir\",\"hold\":false,\"executionMode\":\"FutureDated\"}]},\"pis:recurring\":{\"scopeUsageLimit\":\"single\",\"recurrence\":{\"startDate\":\"Sun Feb 17\",\"frequency\":{\"periodType\":\"week\",\"periodValue\":19137407},\"endDate\":\"Tue Nov 08\",\"dayOffOffsetType\":\"after\"},\"typeOfPayment\":\"nonEEA\",\"domesticPayment\":{\"recipient\":{\"accountNumber\":\"5146781370786740\",\"nameAddress\":{\"value\":[\"bojbuzacobn\"]}},\"sender\":{\"accountNumber\":\"6250516724752240\",\"nameAddress\":{\"value\":[\"gabzovehumirucuwamuhowapj\"]}},\"transferData\":{\"description\":\"Pu uzofodu vu ofnuc belsirzem uga koplofdul taw tajiznuc oruposa lod zuofo bojme ru me hirro vidasgo gekek.\",\"amount\":\"3797895964786688\",\"executionDate\":\"Fri Jan 07\",\"currency\":\"IMP\"},\"deliveryMode\":\"StandardD1\",\"system\":\"BlueCash\",\"hold\":true,\"splitPayment\":false,\"transactionInfoSp\":{\"spInvoiceNumber\":\"8756747646795776\",\"spTaxIdentificationNumber\":\"4119901660774400\",\"spTaxAmount\":\"836195250601984\",\"spDescription\":\"Mu amajo cu kiovo lopjofat enis udo zumorurom lafire wiwavdob notev sidaod.\"}},\"EEAPayment\":{\"recipient\":{\"accountNumber\":\"201486648957593\",\"nameAddress\":{\"value\":[\"akocitesemohuzjezzijebudup\"]},\"countryCode\":\"TT\"},\"sender\":{\"accountNumber\":\"4910790639364162\",\"nameAddress\":{\"value\":[\"wizohokmutcucvewannibfuffackaikagi\"]}},\"transferData\":{\"description\":\"Pebezca sap gejrol ih janicdem mot ko wokzew zezemakuz diin nezu sedizah jog.\",\"amount\":\"3598040732532736\",\"executionDate\":\"Thu May 15\",\"currency\":\"VND\"},\"deliveryMode\":\"StandardD1\",\"system\":\"Target\",\"hold\":false},\"nonEEAPayment\":{\"recipient\":{\"accountNumber\":\"340082527077686\",\"nameAddress\":{\"value\":[\"cajtestuigo\"]},\"countryCode\":\"NL\"},\"recipientBank\":{\"bicOrSwift\":\"irdofato\",\"name\":\"Lelia Martin\",\"code\":\"ejjih\",\"countryCode\":\"BI\",\"address\":{\"value\":[\"cokopoadbenappikucjajtobec\"]}},\"sender\":{\"accountNumber\":\"6011621078051072\",\"nameAddress\":{\"value\":[\"horzamzubobovzailhuangewvemamk\"]}},\"transferData\":{\"description\":\"Kafe wockeuzo ke zuit heraz kotug micmakec we valde jumzajwe uwe gifizdu mu.\",\"amount\":\"4468669283303424\",\"executionDate\":\"Sat Jul 12\",\"currency\":\"CHF\"},\"transferCharges\":\"pak\",\"deliveryMode\":\"UrgentD1\",\"system\":\"Swift\",\"hold\":true},\"taxPayment\":{\"recipient\":{\"accountNumber\":\"4026209536145716\",\"nameAddress\":{\"value\":[\"sivuva\"]}},\"sender\":{\"accountNumber\":\"5128214833841664\",\"nameAddress\":{\"value\":[\"atpoen\"]}},\"transferData\":{\"amount\":\"3187612093251584\",\"executionDate\":\"Mon Oct 13\",\"currency\":\"ILS\"},\"usInfo\":{\"payerInfo\":{\"payorId\":\"6593649068474368\",\"payorIdType\":3},\"formCode\":\"megsoco\",\"periodId\":\"1203152728621056\",\"periodType\":\"nosilze\",\"year\":9998,\"obligationId\":\"2969074466291712\"},\"deliveryMode\":\"ExpressD0\",\"system\":\"ExpressElixir\",\"hold\":true}},\"pis:getRecurringPayment\":{\"scopeUsageLimit\":\"single\",\"recurringPaymentId\":\"2454959765323776\",\"tppRecurringPaymentId\":\"7080453683019776\"},\"pis:cancelRecurringPayment\":{\"scopeUsageLimit\":\"multiple\",\"recurringPaymentId\":\"2415731874463744\"}}],\"scopeGroupType\":\"ais\",\"consentId\":\"8991838557962240\",\"scopeTimeLimit\":\"2011-03-30T11:45:30.703Z\",\"throttlingPolicy\":\"psd2Regulatory\"},\"is_user_session\":true,\"user_ip\":\"Myrtie\",\"user_agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36\"}")
			val body2 = RequestBody.create(mediaType,ApiFuncs.tesst_getTokenRequestBody().toString())
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
				.addHeader("x-request-id", ApiFuncs.generate_X_REQUEST_ID())
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
					test_request()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			thread.start()
		}
		/*
		fun AIS_deleteConsent(){
			//Usuwa zezwolenie / Removes consent
		}
		fun AIS_getAccount(){
			//Uzyskanie szczegółowych informacji o koncie płatniczym użytkownika / Get detailed information about user payment account
			//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
		}
		fun AIS_getAccounts(){
			//Uzyskanie informacji na temat wszystkich kont płatniczych użytkownika / Get information about all user's payment account
			//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
		}
		fun AIS_getHolds(){
			//Pobranie informacji o blokadach na koncie użytkownika / Get list of user's held operations
		}
		fun AIS_getTransactionDetail(){
			//Pobranie szczegółowych informacji o pojedynczej transkacji użytkownika / Get detailed information about user's single transaction
		}
		fun AIS_getTransactionsCancelled(){
			//Pobranie informacji o anulowanych transakcjach użytkownika / Get list of user cancelled transactions
		}
		fun AIS_getTransactionsDone(){
			//Pobranie informacji o zaksięgowanych transakcjach użytkownika / Get list of user done transactions
		}
		fun AIS_getTransactionsPending(){
			//Pobranie informacji o oczekujących transakcjach użytkownika / Get list of user's pending transactions
		}
		fun AIS_getTransactionsRejected(){
			//Pobranie informacji o odrzuconych transakcjach użytkownika / Get list of user's rejected transactions
		}
		fun AIS_getTransactionsScheduled(){
			//Pobranie informacji o zaplanowanych transakcjach użytkownika / Get list of user scheduled transactions
		}

		//AS

		fun AS_authorize(){
			//Żądanie kodu autoryzacji OAuth2 / Requests OAuth2 authorization code
		}
		fun AS_authorizeExt(){
			/*
			Żądanie wydania kodu autoryzacji OAuth2 na podstawie jednorazowego kodu autoryzacji wydanego przez narzędzie autoryzacji zewnętrznej.
			Kod autoryzacji zostanie dostarczony do TPP jako zapytanie zwrotne z ASPSP, jeśli uwierzytelnienie PSU zostanie potwierdzone przez EAT.
			Funkcja zwrotna musi zapewniać podobne powiadomienie również w przypadku nieudanego uwierzytelnienia lub jego rezygnacji.
			Requests OAuth2 authorization code based One-time authorization code issued by External Authorization Tool.
			Authorization code will be delivered to TPP as callback request from ASPSP if PSU authentication is confirmed by EAT.
			Callback function must provide similar notification also in case of unsuccessful authentication or its abandonment.
			 */
		}
		fun AS_token(){
			//Żądanie wydania tokena dostępu OAuth2 / Requests OAuth2 access token value
		}

		//CAF

		fun CAF_getConfirmationOfFunds(){
			/*
			Potwierdzenie dostępności na rachunku płatnika kwoty niezbędnej do wykonania transakcji płatniczej, o której mowa w Art. 62 PSD2.
			Confirming the availability on the payers account of the amount necessary to execute the payment transaction, as defined in Art. 65 PSD2.
			*/
		}

		//PIS

		fun PIS_bundle(){
			//Inicjacja wielu przelewów / Initiate many transfers as bundle
		}
		fun PIS_cancelPayments(){
			//Anulowanie zaplanowanych płatności / Cancelation of future dated payment
		}
		fun PIS_cancelRecurringPayment(){
			//Anulowanie płatności cyklicznej / Cancelation of recurring payment
		}
		fun PIS_domestic(){
			//Inicjacja przelewu krajowego / Initiate domestic transfer
		}
		fun PIS_eEA(){
			//Inicjacja przelewów zagranicznych SEPA / Initiate SEPA foreign transfers
		}
		fun PIS_getBundle(){
			//Uzyskanie status paczki przelewów / Get the status of bundle of payments
		}
		fun PIS_getMultiplePayments(){
			//Uzyskanie statusu wielu płatności / Get the status of multiple payments
		}
		fun PIS_getPayment(){
			//Uzyskanie statusu płatności / Get the status of payment
		}
		fun PIS_getRecurringPayment(){
			//Uzyskanie status płatności cyklicznej / Get the status of recurring payment
		}
		fun PIS_nonEEA(){
			//Inicjacja przelewów zagranicznych niezgodnych z SEPA / Initiate non SEPA foreign transfers
		}
		fun PIS_recurring(){
			//Definicja nowej płatności cyklicznej / Defines new recurring payment
		}
		fun PIS_tax(){
			//Inicjacja przelewu do organu podatkowego / Initiate tax transfer
		}

		 */
	}
}