package com.example.omega
import android.Manifest
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.settings_activity.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import android.app.PendingIntent
import android.nfc.tech.NfcA
import android.widget.Toast
import android.nfc.NdefMessage

class MainActivity: AppCompatActivity() {
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText
	private lateinit var nfcOnOffButton : Button
	private var nfcIsTurnOnOnApp : Boolean = false
	private val scannerRetCode = 0x101
	private lateinit var nfcAdapter : NfcAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		initUIVariables()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == scannerRetCode && resultCode == RESULT_OK && data!=null){
			val returnedCode = data.getIntExtra("codeFromQR",-1)
			val vailCode = returnedCode in 0..999999
			if(vailCode) {
				codeField.setText(returnedCode.toString())
				processCode(returnedCode)
			}
		}
	}
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if(item.itemId == R.id.ConfigurationsTab){
			val settingsActivityIntent = Intent(this@MainActivity,SettingsActivity::class.java)
			startActivity(settingsActivityIntent)
			return true
		}
		return false
	}
	private fun processCode(code : Int){
		Utilites.showToast(this,"process: " + code.toString())
	}
	private fun initUIVariables(){
		val goQRScannerButtonListener =  View.OnClickListener{
			val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			startActivityForResult(qRScannerActivityIntent, scannerRetCode)
		}
		val nfcButtonListener = View.OnClickListener {
			val nfcIsTurnedOnOnPhone = checkIfNfcIsTurnedOn()
			if(!nfcIsTurnOnOnApp && nfcIsTurnedOnOnPhone){
				//Turn on
				nfcOnOffButton.setBackgroundResource(R.drawable.nfc_on_icon)
				nfcIsTurnOnOnApp = !nfcIsTurnOnOnApp
				return@OnClickListener
			}
			if(nfcIsTurnOnOnApp){
				//Turn off
				nfcOnOffButton.setBackgroundResource(R.drawable.nfc_off_icon)
				nfcIsTurnOnOnApp = !nfcIsTurnOnOnApp
				return@OnClickListener
			}
		}
		val codeFieldTextListener = object : TextWatcher {
			override fun afterTextChanged(s: Editable) {
				if(s.length == 6){
					val code = s.toString().toInt()
					if(code in  0..999999)
						processCode(code)
				}
			}
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int,before: Int, count: Int) {}
		}

		nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }!!
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		codeField = findViewById(R.id.enterCodeField)
		codeField.requestFocus()
		goQRActivityButton.setOnClickListener(goQRScannerButtonListener)
		codeField.addTextChangedListener(codeFieldTextListener)
		nfcOnOffButton = findViewById(R.id.nfcButton)
		nfcOnOffButton.setOnClickListener(nfcButtonListener)
	}
	private fun checkIfNfcIsTurnedOn() : Boolean{
		val deviceHasNfc = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)
		if(!deviceHasNfc){
			Log.e("WookieTag", "There's no NFC on user's phone")
			//TODO dać info userowi że nie ma NFC
			return false
		}


		val permissionListener = object : PermissionListener {
			override fun onPermissionGranted(response: PermissionGrantedResponse?) {}
			override fun onPermissionDenied(response: PermissionDeniedResponse?) {
				val toastText = "Do płatności zbliżeniowych konieczne jest włączenie NFC"
				Utilites.showToast(this@MainActivity,toastText)
				Log.e("WookieTag", "User denied permission to use NFC")
			}
			override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?,	token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(this).withPermission(Manifest.permission.NFC).withListener(permissionListener).check()
		val permissionNfcDenied= checkSelfPermission(Manifest.permission.NFC) == PackageManager.PERMISSION_DENIED
		if(permissionNfcDenied){
			Log.e("WookieTag", "There's no permission to use")
			//TODO dać info userowi że nie ma pozwolenia na NFC
			return false
		}


		val manager = this.getSystemService(NFC_SERVICE) as NfcManager
		val nfcIsOn = manager.defaultAdapter.isEnabled
		if(!nfcIsOn){
			Log.e("WookieTag", "NFC connection is off")
			//TODO dać info userowi że NFC jest wylaczone, przeniesc go z aplikacji do ustawien NFC
			return false
		}

		return true
	}
	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		if(!nfcIsTurnOnOnApp)
			return
		val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
		if(tagFromIntent != null){
			val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
			val relayRecord = (rawMsgs!![0] as NdefMessage).records[0]
			var tagData = String(relayRecord.payload)
			//format UNKOWN_BYTE,LAUNGAGE BYTES(probably 2 bytes), CODE
			if(tagData.count() >= 6){
				val codeCandidate = tagData.takeLast(6).toIntOrNull()
				if(codeCandidate != null && codeCandidate in 0..999999){
					val code = codeCandidate.toInt()
					Log.i("WookieTag",tagData)
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
