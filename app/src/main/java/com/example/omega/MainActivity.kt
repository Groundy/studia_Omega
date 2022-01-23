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
import android.service.autofill.CharSequenceTransformation
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*

class MainActivity : AppCompatActivity() {
	private var nfcSignalCatchingIsOn: Boolean = false
	private var nfcAdapter: NfcAdapter? = null
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		//Utilites.savePref(this,R.integer.PREF_pin,0)
		startActToSetPinIfTheresNoSavedPin()
		initUIVariables()
		TEST_addFunToButton()
		//test()
	}

	private fun test(){
		//Utilites.authTransaction(this,"line1TextTextText\n\"line2TextTextText\n\"line3TextTextText",null)
		val basicTransferIntent = Intent(this, BasicTransferActivity::class.java)
		startActivity(basicTransferIntent)
	}
	private fun TEST_addFunToButton(){
		findViewById<Button>(R.id.testButton).setOnClickListener{
			test()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when(requestCode){
			resources.getInteger(R.integer.ACT_RETCODE_QR_SCANNER) -> {
				if(resultCode == RESULT_OK && data != null) {
					val returnFieldName = resources.getString(R.string.ACT_COM_QR_SCANNER_FIELD_NAME)
					val returnedCode = data.getIntExtra(returnFieldName, -1)
					val vailCode = returnedCode in 0..999999
					if (vailCode)
						codeField.setText(returnedCode.toString())
				}
			}
			resources.getInteger(R.integer.ACT_RETCODE_FINGER) ->{
				val errorCodeFieldName = getString(R.string.ACT_COM_FINGER_FIELD_NAME)
				val errorCode = data?.getIntExtra(errorCodeFieldName, -1)
				if(resultCode == RESULT_OK)
					Utilites.authSuccessed(this)
				else{
					if(errorCode == 13){
						val descriptionFieldName = resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
						val description = data.extras?.getString(descriptionFieldName)
						Utilites.authTransaction(this,description,0)
					}
					else{
						val textToShow = Utilites.getMessageToDisplayToUserAfterBiometricAuthError(errorCode!!)
						Utilites.showToast(this, textToShow)
						Utilites.authFailed(this)
					}
				}
			}
			resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH) ->{
				if(resultCode == RESULT_OK)
					Utilites.authSuccessed(this)
				else
					Utilites.authFailed(this)
			}
			resources.getInteger(R.integer.ACT_RETCODE_PIN_SET) ->{
				if(resultCode == RESULT_OK){
					//do nothing
				}
				else{
					//TODO to tworzy infinity loop w ktorym urzytkownik do upadlego jest proszony o pin
					Utilites.showToast(this,getString(R.string.USER_MSG_PIN_FAILED_TO_SET_NEW_PIN_DIFFRENT_PINS_INSERTED))
					startActToSetPinIfTheresNoSavedPin()
				}
			}
		}
	}
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId){
			R.id.ConfigurationsTab->{
				val settingsActivityIntent = Intent(this@MainActivity, SettingsActivity::class.java)
				startActivity(settingsActivityIntent)
				return true
			}
		}
		return false
	}
	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
		if (tagFromIntent != null) {
			val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
			val relayRecord = (rawMsgs!![0] as NdefMessage).records[0]
			val tagData = String(relayRecord.payload)
			//format UNKOWN_BYTE,LAUNGAGE BYTES(probably 2 bytes), CODE
			if (tagData.count() >= 6) {
				val codeCandidate = tagData.takeLast(6).toIntOrNull()
				if (codeCandidate != null && codeCandidate in 0..999999) {
					val code = codeCandidate.toInt()
					Log.i("WookieTag", "NFC TAG data found:$tagData")
					codeField.setText(code.toString())
				}
			}
		}
	}

	private fun processCode(code: Int) {
		Utilites.showToast(this, "process: $code")
	}
	private fun checkIfNfcIsTurnedOnPhone(): Boolean {
		val deviceHasNfc = this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
		if (!deviceHasNfc) {
			Log.e("WookieTag", "There's no NFC hardware on user's phone")
			Utilites.showToast(this,resources.getString(R.string.USER_MSG_NFC_NO_HW_SUPP))
			return false
		}

		val permissionListener = object : PermissionListener {
			override fun onPermissionGranted(response: PermissionGrantedResponse?) {}
			override fun onPermissionDenied(response: PermissionDeniedResponse?) {
				Utilites.showToast(this@MainActivity, resources.getString(R.string.USER_MSG_NFC_NEED_PERMISSION))
				Log.e("WookieTag", "User denied permission to use NFC")
			}

			override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(this).withPermission(Manifest.permission.NFC)
			.withListener(permissionListener).check()
		val permissionNfcDenied = checkSelfPermission(Manifest.permission.NFC) == PackageManager.PERMISSION_DENIED
		if (permissionNfcDenied) {
			Log.e("WookieTag", "There's no permission to use")
			Utilites.showToast(this@MainActivity, resources.getString(R.string.USER_MSG_NFC_NEED_PERMISSION))
			return false
		}
		val manager = this.getSystemService(NFC_SERVICE) as NfcManager
		val nfcIsOn = manager.defaultAdapter.isEnabled
		if (!nfcIsOn) {
			Utilites.showToast(this@MainActivity, resources.getString(R.string.USER_MSG_NFC_TURN_OFF))
			Log.e("WookieTag", "User denied permission to use NFC")
			return false
		}
		return true
	}
	private fun startActToSetPinIfTheresNoSavedPin(){
		val pinAlreadySet = Utilites.checkIfAppHasAlreadySetPin(this)
		if(!pinAlreadySet){
			val pinActivityActivityIntent = Intent(this, PinActivity::class.java)
			val pinActPurpose = resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[0]
			val pinActPurposeFieldName = resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
			pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
			val retCodeForActivity = resources.getInteger(R.integer.ACT_RETCODE_PIN_SET)
			startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
		}
	}
	private fun switchNfcSignalCatching() {
		if(nfcSignalCatchingIsOn){
			nfcAdapter?.disableForegroundDispatch(this)
			nfcSignalCatchingIsOn = false
		}
		else{
			val intent = Intent(applicationContext, this.javaClass).addFlags(
				Intent.FLAG_ACTIVITY_SINGLE_TOP
			)
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
	}

	private fun initUIVariables() {
		initQR()
		initCodeField()
		initNFC()
	}
	private fun initNFC(){
		nfcAdapter = NfcAdapter.getDefaultAdapter(this)
		val deviceHasNfc = nfcAdapter != null
		val nfcOnOffButton = findViewById<Button>(R.id.nfcButton)
		if (deviceHasNfc) {
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
		else
			nfcOnOffButton.isVisible = false
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
		codeField = findViewById(R.id.enterCodeField)
		codeField.requestFocus()
		codeField.addTextChangedListener(codeFieldTextListener)
	}
	private fun initQR(){
		val goQRScannerButtonListener = View.OnClickListener {
			val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			startActivityForResult(qRScannerActivityIntent, resources.getInteger(R.integer.ACT_RETCODE_QR_SCANNER))
		}
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		goQRActivityButton.setOnClickListener(goQRScannerButtonListener)
	}
	private fun startNfcOnStartIfUserWishTo(){
		//TODO ten kod mimo iż jest kopią kodu z przycisku włączającego wyłączającego, ale nie chce się uruchomić automatycznie
		val turnNfcOnAppStart = Utilites.readPref_Bool(this, R.bool.PREF_turnNfcOnAppStart)
		if(turnNfcOnAppStart && !nfcSignalCatchingIsOn){
			findViewById<Button>(R.id.nfcButton).setBackgroundResource(R.drawable.nfc_on_icon)
			switchNfcSignalCatching()
		}
	}
}
