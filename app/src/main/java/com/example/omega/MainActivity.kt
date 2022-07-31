package com.example.omega

import android.Manifest
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
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.FirebaseApp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.example.omega.Utilities.Companion.TagProduction

//  Minimize: CTRL + SHIFT + '-'
//  Expand:   CTRL + SHIFT + '+'
//  Ctrl + B go to definition

class MainActivity : AppCompatActivity() {
	private var nfcSignalCatchingIsOn: Boolean = false
	private var nfcAdapter: NfcAdapter? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		ActivityStarter.startActToSetPinIfTheresNoSavedPin(this)
		initGUI()
		if(!getTokenOnAppStart())
			return
		developerInitaialFun()
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when(requestCode){
			resources.getInteger(R.integer.ACT_RETCODE_QrScanner) -> qrScannerActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_FINGER) ->fingerAuthActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_PIN_SET) ->pinActivityResult(resultCode)
			resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW) ->webViewActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_PERMISSION_LIST) -> resetPermissionActivityResult(resultCode, data)
			resources.getInteger(R.integer.ACT_RETCODE_DIALOG_userWantToLoginToBank) -> dialogIfUserWantToResetBankAuthResult(resultCode)
		}
	}
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId){
			R.id.ConfigurationsTab ->
				ActivityStarter.startConfigurationActivity(this)
			R.id.TransferTab ->
				openBasicTransferTabClicked()
			R.id.AskForTokenTab ->
				ActivityStarter.startResetPermissionsActivity(this)
			R.id.GenerateBlikCodeTab ->
				ActivityStarter.startRBlikCodeCreatorActivity(this)
			R.id.AccHistoryTab ->
				ActivityStarter.openAccountTransfersHistoryActivity(this)
		}
		return true
	}
	override fun onNewIntent(intent: Intent){
		super.onNewIntent(intent)
		nfcIntentGet(intent)
	}

	private fun processCode(code: Int) {
		val codeField = findViewById<EditText>(R.id.MainAct_enterCodeField)
		codeField.text.clear()
		val transferData = Utilities.checkBlikCode(code)
		if(transferData == null){
			ActivityStarter.startOperationResultActivity(this, R.string.Result_GUI_WRONG_CODE)
			return
		}
		ActivityStarter.startTransferSummaryActivity(this, transferData.toString())
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
	private fun switchNfcSignalCatching() {
		if(nfcSignalCatchingIsOn){
			nfcAdapter?.disableForegroundDispatch(this)
			nfcSignalCatchingIsOn = false
			return
		}

		val intent = Intent(applicationContext, this.javaClass)
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

		val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
		val filters = arrayOfNulls<IntentFilter>(1)
		val techList = arrayOf<Array<String>>()

		filters[0] = IntentFilter()
		with(filters[0]) {
			this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
			this?.addCategory(Intent.CATEGORY_DEFAULT)
			this?.addDataType("text/plain")
		}
		nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techList)
		nfcSignalCatchingIsOn = true
	}
	private fun initGUI() {
		initQR()
		initCodeField()
		initNFC()
	}
	private fun initNFC(){
		nfcAdapter = NfcAdapter.getDefaultAdapter(this)
		val deviceHasNfc = nfcAdapter != null
		val nfcOnOffButton = findViewById<Button>(R.id.MainAct_nfcButton)
		if(!deviceHasNfc){
			nfcOnOffButton.isVisible = false
			return
		}

		val nfcIsTurnedOnOnPhone = checkIfNfcIsTurnedOnPhone()
		nfcOnOffButton.setOnClickListener{
			if (!nfcSignalCatchingIsOn && nfcIsTurnedOnOnPhone) {//Turn on
				nfcOnOffButton.setBackgroundResource(R.drawable.nfc_on_icon)
				switchNfcSignalCatching()
			}
			else if (nfcSignalCatchingIsOn) {//Turn off
				nfcOnOffButton.setBackgroundResource(R.drawable.nfc_off_icon)
				switchNfcSignalCatching()
			}
		}
	}
	private fun initCodeField(){
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
	}
	private fun initQR(){
		val goQrScannerButtonListener = View.OnClickListener {
			val qrScannerActivityIntent = Intent(this, QrScannerActivity::class.java)
			startActivityForResult(qrScannerActivityIntent, resources.getInteger(R.integer.ACT_RETCODE_QrScanner))
		}
		val goQRActivityButton = findViewById<Button>(R.id.MainAct_goToQrScannerButton)
		goQRActivityButton.setOnClickListener(goQrScannerButtonListener)
	}

	private fun startNfcOnStartIfUserWishTo(){
		//TODO ten kod mimo iż jest kopią kodu z przycisku włączającego wyłączającego, ale nie chce się uruchomić automatycznie
		val turnNfcOnAppStart = PreferencesOperator.readPrefBool(this, R.bool.PREF_turnNfcOnAppStart)
		if(turnNfcOnAppStart && !nfcSignalCatchingIsOn){
			findViewById<Button>(R.id.MainAct_nfcButton).setBackgroundResource(R.drawable.nfc_on_icon)
			switchNfcSignalCatching()
		}
	}

	//Intents
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
		ApiAuthorize(this, permissionListObject).run(ApiConsts.ScopeValues.Ais)

		val authUrl = PreferencesOperator.readPrefStr(this, R.string.PREF_authURL)
		val state = PreferencesOperator.readPrefStr(this, R.string.PREF_lastRandomValue)
		val fieldsAreFilled = authUrl.isNotEmpty() && state.isNotEmpty()
		if(!fieldsAreFilled){
			Log.e(TagProduction, "[resetPermissionActivityResult/${this.javaClass.name}]Failed to obtain auth url, tried to pass no authUrl or stateValue")
			val userMsg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
			Utilities.showToast(this, userMsg)
			return
		}

		ActivityStarter.openBrowserForLogin(this)
	}
	private fun webViewActivityResult(resultCode: Int, data: Intent?){
		//todo zastanowić się czy token pobierać tutaj
		val success = ApiGetToken(this).run()
		if (!success){
			val userMsg = getString(R.string.UserMsg_Banking_errorObtaingToken)
			Utilities.showToast(this, userMsg)
		}
	}
	private fun pinActivityResult(resultCode: Int){
		if(resultCode == RESULT_OK)
			return

		//TODO to tworzy infinity loop w ktorym urzytkownik do upadlego jest proszony o pin
		val msgForUser = getString(R.string.PIN_UserMsg_failedToSetNewPin_differentPinsInserted)
		Utilities.showToast(this, msgForUser)
		ActivityStarter.startActToSetPinIfTheresNoSavedPin(this)
	}
	private fun fingerAuthActivityResult(resultCode: Int, data: Intent?){
		if(resultCode == RESULT_OK){
			Utilities.authSuccessed(this)
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
			Utilities.authFailed(this)
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
	private fun nfcIntentGet(intent: Intent){
		val tagFromIntent: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
		val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
		val relayRecord = (rawMsgs!![0] as NdefMessage).records[0]
		val tagData = String(relayRecord.payload)
		val tagDataExpectedLength = 6			//format UNKOWN_BYTE,LAUNGAGE BYTES(probably 2 bytes), CODE
		if(tagData.count() < tagDataExpectedLength)
			return
		val code = tagData.takeLast(6).toIntOrNull()
		val codeOk = code != null && code in 0..999999
		if(!codeOk)
			return
		Log.i(TagProduction, "NFC TAG data found:$tagData")
		processCode(code!!.toInt())
	}
	private fun dialogIfUserWantToResetBankAuthResult(resultCode: Int){
		if(resultCode != RESULT_OK)
			return
		ActivityStarter.startResetPermissionsActivity(this)
	}
	private fun openBasicTransferTabClicked(){
		val tokenCpy = PreferencesOperator.getToken(this)
		val authTokenAvaible = tokenCpy.isOk(this)
		if(!authTokenAvaible){
			Log.e(TagProduction,"[openBasicTransferTabClicked/${this.javaClass.name}] token not available, asked user if he want to reset token")
			ActivityStarter.openDialogWithDefinedPurpose(this, YesNoDialogActivity.Companion.DialogPurpose.ResetAuthUrl)
			return
		}
		ActivityStarter.startTransferActivityFromMenu(this)
	}

	private fun developerInitaialFun(){
		val tmpTest = 3
		val token = PreferencesOperator.getToken(this)
		val tokenOk = token.isOk(this)
		if(!tokenOk)
			return
		val transferData = TransferData.developerGetTestObjWithFilledData()
		ApiDomesticPayment(this, token, transferData).run()
	}
	private fun getTokenOnAppStart() : Boolean{
		val token = PreferencesOperator.getToken(this)
		val tokenOk = token.isOk(this)
		if(!tokenOk){
			val obj = PermissionList(ApiConsts.Privileges.SinglePayment)
			PreferencesOperator.clearAuthData(this)
			val authOk = ApiAuthorize(this, obj).run(ApiConsts.ScopeValues.Pis)
			if(!authOk)
				return true
			ActivityStarter.openBrowserForLogin(this)
			return false
		}
		else
			Log.i(TagProduction, "seconds left to token exp:  ${token.getSecondsLeftToTokenExpiration().toString()}")
		return true
	}
}
