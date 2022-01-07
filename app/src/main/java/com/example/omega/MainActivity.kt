package com.example.omega

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.settings_activity.*

class MainActivity : AppCompatActivity() {
	private var nfcIsTurnOnOnApp: Boolean = false
	private lateinit var nfcAdapter: NfcAdapter
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		initUIVariables()
		test()
	}

	private fun test(){
		val scanFingerActivityIntent = Intent(this, PinActivity::class.java)
		startActivityForResult(scanFingerActivityIntent, resources.getInteger(R.integer.FINGER_SCANNER_RET_CODE))
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(resultCode == RESULT_OK && data != null){
			when(requestCode){
				resources.getInteger(R.integer.QR_SCANNER_RET_CODE) -> {
					val returnedCode = data.getIntExtra("codeFromQR", -1)
					val vailCode = returnedCode in 0..999999
					if (vailCode) {
						codeField.setText(returnedCode.toString())
						processCode(returnedCode)
					}
				}
				resources.getInteger(R.integer.FINGER_SCANNER_RET_CODE) ->{
					//Tylko do testów
					Log.i("WookieTag", "finger activity returned")
				}
			}
		}

	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == R.id.ConfigurationsTab) {
			val settingsActivityIntent = Intent(this@MainActivity, SettingsActivity::class.java)
			startActivity(settingsActivityIntent)
			return true
		}
		return false
	}

	private fun processCode(code: Int) {
		Utilites.showToast(this, "process: $code")
	}

	private fun initUIVariables() {
		val goQRScannerButtonListener = View.OnClickListener {
			val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			startActivityForResult(qRScannerActivityIntent, resources.getInteger(R.integer.QR_SCANNER_RET_CODE))
		}
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
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)

		codeField = findViewById(R.id.enterCodeField)
		codeField.requestFocus()
		goQRActivityButton.setOnClickListener(goQRScannerButtonListener)
		codeField.addTextChangedListener(codeFieldTextListener)

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
		else{
			nfcOnOffButton.isVisible = false
		}
	}

	private fun checkIfNfcIsTurnedOn(): Boolean {
		val deviceHasNfc = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)
		if (!deviceHasNfc) {
			Log.e("WookieTag", "There's no NFC hardware on user's phone")
			//TODO dać info userowi że nie ma NFC
			return false
		}


		val permissionListener = object : PermissionListener {
			override fun onPermissionGranted(response: PermissionGrantedResponse?) {}
			override fun onPermissionDenied(response: PermissionDeniedResponse?) {
				val toastText = "Do płatności zbliżeniowych konieczne jest włączenie NFC"
				Utilites.showToast(this@MainActivity, toastText)
				Log.e("WookieTag", "User denied permission to use NFC")
			}

			override fun onPermissionRationaleShouldBeShown(
				permission: PermissionRequest?,
				token: PermissionToken?
			) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(this).withPermission(Manifest.permission.NFC)
			.withListener(permissionListener).check()
		val permissionNfcDenied =
			checkSelfPermission(Manifest.permission.NFC) == PackageManager.PERMISSION_DENIED
		if (permissionNfcDenied) {
			Log.e("WookieTag", "There's no permission to use")
			//TODO dać info userowi że nie ma pozwolenia na NFC
			return false
		}


		val manager = this.getSystemService(NFC_SERVICE) as NfcManager
		val nfcIsOn = manager.defaultAdapter.isEnabled
		if (!nfcIsOn) {
			Log.e("WookieTag", "NFC connection is off")
			//TODO dać info userowi że NFC jest wylaczone, przeniesc go z aplikacji do ustawien NFC
			return false
		}

		return true
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
