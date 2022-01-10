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
import kotlinx.android.synthetic.main.activity_settings.*

class MainActivity : AppCompatActivity() {
	private var nfcIsTurnOnOnApp: Boolean = false
	private lateinit var nfcAdapter: NfcAdapter
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		Utilites.savePref(this,R.integer.PREF_pin,0)
		startActToSetPinIfTheresNoSavedPin()
		initUIVariables()
		TEST_addFunToButton()
		//test()
	}

	private fun test(){
		Utilites.authTransaction(this,"ff",null)
	}
	private fun TEST_addFunToButton(){
		val ttt = findViewById<Button>(R.id.testButton)
		val listener = View.OnClickListener {
			test()
		}
		ttt.setOnClickListener(listener)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		if (!nfcIsTurnOnOnApp)
			return
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
					Log.i("WookieTag", tagData)
					processCode(code)
				}
			}
		}
	}
	private fun processCode(code: Int) {
		Utilites.showToast(this, "process: $code")
	}
	private fun initUIVariables() {
		//QR button
		val goQRScannerButtonListener = View.OnClickListener {
			val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			startActivityForResult(qRScannerActivityIntent, resources.getInteger(R.integer.ACT_RETCODE_QR_SCANNER))
		}
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		goQRActivityButton.setOnClickListener(goQRScannerButtonListener)

		//codeField
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

		//NFC
		val deviceHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		val nfcOnOffButton = findViewById<Button>(R.id.nfcButton)
		if (deviceHasNfc) {
			nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }!!
			val nfcButtonListener = View.OnClickListener {
				val nfcIsTurnedOnOnPhone = checkIfNfcIsTurnedOn()
				if (!nfcIsTurnOnOnApp && nfcIsTurnedOnOnPhone) {
					//Turn on
					nfcOnOffButton.setBackgroundResource(R.drawable.nfc_on_icon)
					nfcIsTurnOnOnApp = !nfcIsTurnOnOnApp
					return@OnClickListener
				}
				if (nfcIsTurnOnOnApp) {
					//Turn off
					nfcOnOffButton.setBackgroundResource(R.drawable.nfc_off_icon)
					nfcIsTurnOnOnApp = !nfcIsTurnOnOnApp
					return@OnClickListener
				}
			}
			nfcOnOffButton.setOnClickListener(nfcButtonListener)
		}
		else
			nfcOnOffButton.isVisible = false
	}
	private fun checkIfNfcIsTurnedOn(): Boolean {
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
			Log.e("WookieTag", "User denied permission to use NFC")
			return false
		}
		val manager = this.getSystemService(NFC_SERVICE) as NfcManager
		val nfcIsOn = manager.defaultAdapter.isEnabled
		if (!nfcIsOn) {
			Log.e("WookieTag", "NFC connection is off")
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
			val startForAuthFieldName = resources.getString(R.string.ACT_COM_PIN_STARTED_FOR_AUTH)
			pinActivityActivityIntent.putExtra(startForAuthFieldName,false)
			val retCodeForActivity = resources.getInteger(R.integer.ACT_RETCODE_PIN_SET)
			startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
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
					if (vailCode) {
						codeField.setText(returnedCode.toString())
						processCode(returnedCode)
					}
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
	private fun enableForegroundDispatch(activity: AppCompatActivity) {
		val intent = Intent(activity.applicationContext, activity.javaClass).addFlags(
			Intent.FLAG_ACTIVITY_SINGLE_TOP
		)
		val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)
		val filters = arrayOfNulls<IntentFilter>(1)
		val techList = arrayOf<Array<String>>()

		filters[0] = IntentFilter()
		with(filters[0]) {
			this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
			this?.addCategory(Intent.CATEGORY_DEFAULT)
			this?.addDataType("text/plain")
		}
		this.nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList)
	}
}
