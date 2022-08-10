package com.example.omega
//  Minimize: CTRL + SHIFT + '-'
//  Expand:   CTRL + SHIFT + '+'
//  Ctrl + B go to definition

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.example.omega.Utilities.Companion.TagProduction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.omega.BankLoginWebPageActivity.Companion.WebActivtyRedirect
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {
	private var nfcCapturingIsOn = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		ActivityStarter.startPinActivity(this, PinActivity.Companion.Purpose.Set)
		initGUI()
		startNfcOnStartIfUserWishTo()
		//PreferencesOperator.clearAuthData(this)
		//val dialog = WaitingDialog(this, "Obtaining token from memory")
		//basicTransferTabCliked()
	}

	//Menus
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId){
			R.id.ConfigurationsTab ->
				ActivityStarter.startConfigurationActivity(this)
			R.id.AskForTokenTab ->
				ActivityStarter.startResetPermissionsActivity(this)
		}
		return true
	}

	//Results
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when(requestCode){
			resources.getInteger(R.integer.ACT_RETCODE_QrScanner) -> qrScannerActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_FINGER) ->fingerAuthActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_PIN_SET) ->pinActivityResult(resultCode)
			resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW) ->webViewActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_PERMISSION_LIST) -> resetPermissionActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_DIALOG_userWantToLoginToBank) -> dialogIfUserWantToResetBankAuthResult(resultCode)
			resources.getInteger(R.integer.ACT_RETCODE_BASIC_TRANSFER_ACT) -> startSinglePaymentAuthorization(resultCode, data)
		}
	}
	private fun resetPermissionActivityResult(resultCode: Int, data: Intent?){
		if(resultCode != RESULT_OK){
			Log.i(TagProduction, "Canceled getting authUrl")
			return
		}

		val field = getString(R.string.ACT_COM_USERPERMISSIONLIST_FIELDNAME)
		val serializedPermissionList = data?.getStringExtra(field)
		if(serializedPermissionList.isNullOrEmpty()){
			Log.i(TagProduction, "Canceled getting authUrl")
			return
		}

		val permissionListObject = PermissionList(serializedPermissionList)
		PreferencesOperator.clearAuthData(this)
		CoroutineScope(IO).launch {
			OpenApiAuthorize(this@MainActivity).runForAis(permissionListObject)
			val authUrl = PreferencesOperator.readPrefStr(this@MainActivity, R.string.PREF_authURL)
			val state = PreferencesOperator.readPrefStr(this@MainActivity, R.string.PREF_lastRandomValue)
			val fieldsAreFilled = authUrl.isNotEmpty() && state.isNotEmpty()
			if(!fieldsAreFilled){
				Log.e(TagProduction, "[resetPermissionActivityResult/${this.javaClass.name}]Failed to obtain auth url, tried to pass no authUrl or stateValue")
				withContext(Main){
					val userMsg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
					Utilities.showToast(this@MainActivity, userMsg)
				}
				return@launch
			}

			ActivityStarter.openBrowserForLogin(this@MainActivity, WebActivtyRedirect.None)
		}



	}
	private fun webViewActivityResult(resultCode: Int, data: Intent?){
		CoroutineScope(IO).launch {
			if(resultCode != RESULT_OK)
				return@launch

			val redirectField = getString(R.string.ACT_COM_WEBVIEW_REDIRECT_FIELD_NAME)
			val redirectPlace : WebActivtyRedirect = try{
				val redirectStr = data?.extras!!.getString(redirectField)
				WebActivtyRedirect.fromStr(redirectStr!!)
			}catch (e : Exception){
				BankLoginWebPageActivity.Companion.WebActivtyRedirect.None
			}

			val scopeUsedForLogin = if(redirectPlace == WebActivtyRedirect.DomesticPaymentProcess)
				ApiConsts.ScopeValues.Pis
			else
				ApiConsts.ScopeValues.Ais

			val success = OpenApiGetToken(this@MainActivity, scopeUsedForLogin).run()
			if (!success){
				val userMsg = getString(R.string.UserMsg_Banking_errorObtaingToken)
				Utilities.showToast(this@MainActivity, userMsg)
				return@launch
			}
			withContext(Main){
				when(redirectPlace){
					WebActivtyRedirect.AccountHistory -> {accHistoryTabClicked()}
					WebActivtyRedirect.PaymentCreation ->{basicTransferTabCliked()}
					WebActivtyRedirect.GenerateRBlikCode ->{generateRBlickCodeClicked()}
					WebActivtyRedirect.DomesticPaymentProcess -> {continueSinglePaymentAfterLogin()}
					else->{
						return@withContext
					}
				}
			}

		}

	}
	private fun pinActivityResult(resultCode: Int){
		if(resultCode == RESULT_OK)
			return

		//TODO to tworzy infinity loop w ktorym urzytkownik do upadlego o utworzenie Pinu
		val msgForUser = getString(R.string.PIN_UserMsg_failedToSetNewPin_differentPinsInserted)
		Utilities.showToast(this, msgForUser)
		ActivityStarter.startPinActivity(this, PinActivity.Companion.Purpose.Set)
	}
	private fun fingerAuthActivityResult(resultCode: Int, data: Intent?){
		if(resultCode == RESULT_OK){
			ActivityStarter.startOperationResultActivity(this, R.string.Result_GUI_OK)
			return
		}

		val errorCodeFieldName = getString(R.string.ACT_COM_FINGER_FIELD_NAME)
		val errorCode = data?.getIntExtra(errorCodeFieldName, -1)
		if(errorCode == 13){
			val descriptionFieldName = resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val description = data.extras?.getString(descriptionFieldName)
			ActivityStarter.startAuthActivity(this, description, 0)
		}
		else{
			val textToShow = when(errorCode){
				//0 -> "Uzyskano autoryzację!"
				1 -> "Sensor jest chwilowo niedostępny, należy spróbować później."
				2 -> "Czujnik nie był w stanie przetworzyć odcisku palca."
				3 -> "Nie wykryto palca przez 30s."
				4 -> "Urządzenie nie ma wystarczającej ilości miejsca żeby wykonać operacje."
				5,10 -> "Użytkownik anulował uwierzytelnianie za pomocą biometrii."
				7 -> "Pięciorkotnie nierozpoznano odcisku palca, sensor będzie dostępny ponownie za 30s."
				9 -> "Sensor jest zablokowany, należy go odblokować wporwadzająć wzór/pin telefonu."
				11 -> "Nieznany błąd, upewnij się czy w twoim urządzeniu jest zapisany odcis palca."
				12 -> "Urządzenie nie posiada odpowiedniego sensora."
				14 -> "Urządzenie musi posiadać pin,wzór lub hasło."
				15 -> "Operacja nie może zostać wykonana bez aktualizacji systemu."
				else ->"Operacja zakończona niepowodzeniem z nieznanego powodu."
			}
			Utilities.showToast(this, textToShow)
			Utilities.showToast(this, "Auth failed!")
		}
	}
	private fun qrScannerActivityResult(resultCode: Int, data: Intent?){
		if(resultCode != RESULT_OK || data == null)
			return

		val returnFieldName = resources.getString(R.string.ACT_COM_QrScanner_FIELD_NAME)
		val returnedCode = data.getIntExtra(returnFieldName, -1)
		val vailCode = returnedCode in 0..999999
		if (vailCode)
			processCode(returnedCode)

	}
	private fun dialogIfUserWantToResetBankAuthResult(resultCode: Int){
		if(resultCode != RESULT_OK)
			return
		ActivityStarter.startResetPermissionsActivity(this)
	}
	private fun startSinglePaymentAuthorization(resultCode: Int, data: Intent?){
		if(resultCode != RESULT_OK)
			return

		val transferData = try {
			val fieldName = resources.getString(R.string.ACT_COM_MANY_RetTransferData_FIELDNAME)
			val transferDataStr = data!!.extras!!.getString(fieldName)
			val transferDataObj = TransferData(transferDataStr!!)
			transferDataObj
		}catch (e : Exception){
			Log.e(TagProduction, "[startSinglePaymentAuthorization/${this.javaClass.name}] cant obtain data transfer from intent")
			null
		}
		if(transferData == null){
			ActivityStarter.startOperationResultActivity(this, R.string.Result_GUI_WRONG_ELSE)
			return
		}

		val permListObj = PermissionList(ApiConsts.Privileges.SinglePayment)

		val dialog = WaitingDialog(this, R.string.POPUP_auth)
		CoroutineScope(IO).launch {
			val authOk = OpenApiAuthorize(this@MainActivity).runForPis(permListObj, transferData)
			withContext(Main){
				dialog.hide()
				if(!authOk){
					Log.e(TagProduction, "[startSinglePaymentAuthorization/${this@MainActivity.javaClass.name}] Failed To auth, ending payment process")
					ActivityStarter.startOperationResultActivity(this@MainActivity, R.string.Result_GUI_WRONG_ELSE)
				}
				else
					ActivityStarter.openBrowserForLogin(this@MainActivity, WebActivtyRedirect.DomesticPaymentProcess)
			}
		}
	}
	private fun continueSinglePaymentAfterLogin(){
		val paymentTokenStr = PreferencesOperator.readPrefStr(this, R.string.PREF_PaymentToken)
		val paymentToken  = Token(paymentTokenStr)
		val dialog = WaitingDialog(this, R.string.POPUP_auth)
		CoroutineScope(IO).launch{
			val paymentSuccessed = OpenApiDomesticPayment(this@MainActivity, paymentToken).run()

			val strMsg = if(paymentSuccessed)
				R.string.Result_GUI_OK
			else
				R.string.Result_GUI_WRONG_ELSE
			withContext(Main){
				dialog.hide()
				ActivityStarter.startOperationResultActivity(this@MainActivity, strMsg)
			}
		}
	}

	//OptionsClicked
	private fun basicTransferTabCliked(){
		val dialog = WaitingDialog(this, R.string.POPUP_getToken)
		CoroutineScope(IO).launch{
			val ok = getToken(WebActivtyRedirect.PaymentCreation)
			withContext(Main){
				if(ok){
					ActivityStarter.startTransferActivity(this@MainActivity)
					dialog.hide()
				}
				else{
					Log.w(TagProduction, "Nie można otworzyć okna płatności, brak możliwości pobrania tokenu, prwadopodobnie nastąpiło przekierowanie")
					dialog.hide()
				}
			}
		}
	}
	private fun accHistoryTabClicked(){
		val dialog = WaitingDialog(this, R.string.POPUP_getToken)
		CoroutineScope(IO).launch{
			val ok = getToken(WebActivtyRedirect.AccountHistory, dialog)
			withContext(Main){
				dialog.changeText(this@MainActivity, R.string.POPUP_empty)
				if(ok){
					ActivityStarter.openAccountTransfersHistoryActivity(this@MainActivity)
					dialog.hide()
				}
				else{
					Log.e(TagProduction, "[accHistoryTabClicked/${this@MainActivity.javaClass.name}] Nie można otworzyć historii rachunek, brak możliwości pobrania tokenu")
					dialog.hide()
				}
			}
			return@launch
		}
	}
	private fun generateRBlickCodeClicked(){
		val dialog = WaitingDialog(this, R.string.POPUP_getToken)
		CoroutineScope(IO).launch {
			val ok = getToken(WebActivtyRedirect.GenerateRBlikCode)
			withContext(Main){
				dialog.changeText(this@MainActivity, R.string.POPUP_empty)
				if(ok){
					ActivityStarter.startRBlikCodeCreatorActivity(this@MainActivity)
					dialog.hide()
				}
				else{
					Log.e(TagProduction, "[generateRBlickCodeClicked/${this@MainActivity.javaClass.name}] Nie można otworzyć historii rachunek, brak możliwości pobrania tokenu")
					dialog.hide()
				}
			}
		}
	}
	private fun qrScannerTabClicked(){
		ActivityStarter.startQrScannerActivity(this)
	}
	private fun wookieTestFunClicked(){
		wookieTestFun()
	}

	//Other
	private suspend fun getToken(redirectPlace : WebActivtyRedirect, dialog: WaitingDialog? = null) : Boolean{
		val token = PreferencesOperator.getToken(this)
		val tokenOk = token.isOk(this)
		if(tokenOk){
			dialog?.hide()
			return true
		}

		val obj = PermissionList(ApiConsts.Privileges.AccountsDetails, ApiConsts.Privileges.AccountsHistory)
		PreferencesOperator.clearAuthData(this)
		val authOk = OpenApiAuthorize(this).runForAis(obj, dialog)
		if(!authOk){
			withContext(Main){
				Utilities.showToast(this@MainActivity, "Nie udało się automatycznie pobrać tokenu.")//todo to file
			}
			dialog?.hide()
			return false
		}
		dialog?.hide()
		ActivityStarter.openBrowserForLogin(this, redirectPlace)
		return false
	}
	private fun processCode(code: Int) {
		val codeField = findViewById<EditText>(R.id.MainAct_enterCodeField)
		codeField.text.clear()
		val dialog = WaitingDialog(this, R.string.POPUP_codeProccess)
		CoroutineScope(IO).launch {
			val transferData = CodeServerApi.getCodeData(this@MainActivity, code)
			withContext(Main){
				dialog.hide()
				if(transferData!=null)
					ActivityStarter.startTransferActivity(this@MainActivity, transferData)
			}
		}
	}
	@SuppressLint("UseCompatLoadingForDrawables")
	private fun initGUI() {
		val codeFieldTextListener = object : TextWatcher {
			override fun afterTextChanged(s: Editable) {
				if (s.length == 6) {
					val code = s.toString().toInt()
					if (code in 0..999999)
						processCode(code)
				}
			}
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
		}
		val codeField = findViewById<EditText>(R.id.MainAct_enterCodeField)
		codeField.addTextChangedListener(codeFieldTextListener)
		codeField.requestFocus()

		val listner = BottomNavigationView.OnNavigationItemSelectedListener { item ->
			when(item.itemId){
				R.id.AccHistoryTab-> accHistoryTabClicked()
				R.id.TransferTab ->	basicTransferTabCliked()
				R.id.GenerateBlikCodeTab -> generateRBlickCodeClicked()
				R.id.NfcTab -> nfcButtonClicked()
				R.id.QrScannerTab->qrScannerTabClicked()
				R.id.ToImplementTab->wookieTestFunClicked()
			}
			true
		}
		val firstBar = findViewById<BottomNavigationView>(R.id.MainAct_firstBar)
		val secondBar = findViewById<BottomNavigationView>(R.id.MainAct_secondBar)
		firstBar.setOnNavigationItemSelectedListener(listner)
		secondBar.setOnNavigationItemSelectedListener(listner)

		//te funkcje są po to aby ikoni pojawiały się czarne a nie białe
		firstBar.itemIconTintList = null
		secondBar.itemIconTintList = null
		firstBar.menu.findItem(R.id.AccHistoryTab).icon = getDrawable(R.drawable.ico_history)
		firstBar.menu.findItem(R.id.GenerateBlikCodeTab).icon = getDrawable(R.drawable.ico_qr)
		firstBar.menu.findItem(R.id.TransferTab).icon = getDrawable(R.drawable.ico_payment)
		secondBar.menu.findItem(R.id.NfcTab).icon = if(nfcCapturingIsOn)
			getDrawable(R.drawable.ico_nfc_on)
		else
			getDrawable(R.drawable.ico_nfc_off)
		secondBar.menu.findItem(R.id.ToImplementTab).icon = getDrawable(R.drawable.ico_cancel)
		secondBar.menu.findItem(R.id.QrScannerTab).icon = getDrawable(R.drawable.ico_qr_scanner)
	}
	private fun wookieTestFun(){

	}
	//NFC
	@SuppressLint("UseCompatLoadingForDrawables")
	private fun turnNfcOn(){
		Log.d(TagProduction, "Nfc turning On started")

		val secondBar = findViewById<BottomNavigationView>(R.id.MainAct_secondBar)
		val button = secondBar.menu.findItem(R.id.NfcTab)

		turnForegroundDispatchOn()
		val onIco = getDrawable(R.drawable.ico_nfc_on)
		button.icon = onIco
		nfcCapturingIsOn = true
	}
	@SuppressLint("UseCompatLoadingForDrawables")
	private fun turnNfcOff(){
		Log.d(TagProduction, "Nfc turning Off started")
		val secondBar = findViewById<BottomNavigationView>(R.id.MainAct_secondBar)
		val button = secondBar.menu.findItem(R.id.NfcTab)
		val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
		nfcAdapter.disableForegroundDispatch(this)
		val offIco = getDrawable(R.drawable.ico_nfc_off)
		button.icon = offIco
		nfcCapturingIsOn = false
	}
	@SuppressLint("UnspecifiedImmutableFlag")
	private fun turnForegroundDispatchOn(){
		val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

		val intent = Intent(this, this.javaClass)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

		val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

		val intentFiltersArray = arrayOf(
			IntentFilter().also {
				it.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
				it.addCategory(Intent.CATEGORY_DEFAULT)
				it.addDataType("text/plain")
			}
		)
		val techListEmptyArray = arrayOf<Array<String>>()

		nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListEmptyArray)
	}
	private fun startNfcOnStartIfUserWishTo(){
		val turnOnNfcOnStart = PreferencesOperator.readPrefBool(this, R.string.PREF_turnNfcOnAppStart)
		if(!turnOnNfcOnStart)
			return

		if(nfcCapturingIsOn)
			return

		mainExecutor.execute{
			turnNfcOn()
		}
	}
	private fun nfcButtonClicked(){
		val nfcIsTurnedOnOnPhone = checkIfNfcIsTurnedOnPhone()
		if (!nfcCapturingIsOn && nfcIsTurnedOnOnPhone)
			turnNfcOn()
		else if (nfcCapturingIsOn)
			turnNfcOff()
	}
	private fun nfcIntentGet(intent: Intent){
		val code : Int? = try {
			val tagFromIntent: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
			if(tagFromIntent == null){
				Utilities.showToast(this, "Wykryto pusty tag NFCC")
				return
			}

			val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
			val relayRecord = (rawMsgs!![0] as NdefMessage).records[0]
			val tagData = String(relayRecord.payload)
			val tagDataExpectedLength = 6//format UNKOWN_BYTE,LAUNGAGE BYTES(probably 2 bytes), CODE
			if(tagData.count() < tagDataExpectedLength)
				return

			val codeFromIntent = tagData.takeLast(6).toIntOrNull()
			val codeOk = codeFromIntent != null && codeFromIntent in 0..999999
			if(!codeOk){
				Utilities.showToast(this, "Wykryto nieprawidłowy NFCC")
				return
			}


			Log.i(TagProduction, "NFC TAG data found:$tagData")
			codeFromIntent
		}catch (e : Exception){
			Log.e(TagProduction,"Error in cathing NFC tag $e")
			null
		}

		if(code!=null)
			processCode(code)
	}
	override fun onPause() {
		super.onPause()
		if(nfcCapturingIsOn)
			turnNfcOff()
	}
	private fun checkIfNfcIsTurnedOnPhone(): Boolean {
		val deviceHasNfc = this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
		if (!deviceHasNfc) {
			Log.e(TagProduction, "[checkIfNfcIsTurnedOnPhone/${this.javaClass.name}]There's no NFC hardware on user's phone")
			Utilities.showToast(this, resources.getString(R.string.NFC_UserMsg_NoHardwareSupport))
			return false
		}

		val permissionListener = object : PermissionListener {
			override fun onPermissionGranted(response: PermissionGrantedResponse?) {}
			override fun onPermissionDenied(response: PermissionDeniedResponse?) {
				Utilities.showToast(
					this@MainActivity,
					resources.getString(R.string.NFC_UserMsg_NeedPermission)
				)
				Log.e(TagProduction, "User denied permission to use NFC")
			}

			override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(this).withPermission(Manifest.permission.NFC)
			.withListener(permissionListener).check()
		val permissionNfcDenied = checkSelfPermission(Manifest.permission.NFC) == PackageManager.PERMISSION_DENIED
		if (permissionNfcDenied) {
			Log.e(TagProduction, "There's no permission to use")
			val displayMsg = resources.getString(R.string.NFC_UserMsg_NeedPermission)
			Utilities.showToast(this, displayMsg)
			return false
		}

		val manager = this.getSystemService(NFC_SERVICE) as NfcManager
		val nfcIsOn = manager.defaultAdapter.isEnabled
		if (!nfcIsOn) {
			val displayMsg = resources.getString(R.string.NFC_UserMsg_TurnOff)
			Utilities.showToast(this, displayMsg)
			Log.e(TagProduction, "User denied permission to use NFC")
			return false
		}
		return true
	}
	override fun onNewIntent(intent: Intent){
		super.onNewIntent(intent)
		nfcIntentGet(intent)
	}

}

