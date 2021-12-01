package com.example.omega
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.Intent
import android.widget.Button


class MainActivity: AppCompatActivity() {
	lateinit var goQRActivityButton: Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		//var qrScanner : QrScanner = QrScanner(this)
		//BlikNumberEditText.requestFocus()
		//startNfcConnection()
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		goQRActivityButton.setOnClickListener() {
			val QRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			var returnCode: Int = 0
			startActivityForResult(QRScannerActivityIntent, returnCode)
		}
	}


	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu,menu)
		return super.onCreateOptionsMenu(menu)
	}
	/*
	var nfcAdapter : NfcAdapter = NfcAdapter.getDefaultAdapter(this)

	private fun checkIfDeviceSupportNFC(): Boolean {
		var toRet = true
		 if(nfcAdapter==null){
			Toast.makeText(this, "That device doesn't support NFC ", Toast.LENGTH_LONG)
			toRet = false
		}
		return toRet
	}

	fun askForNFCPermissions(){
		val permissionArray= Array(1){ Manifest.permission.NFC}.toMutableList()
		val permissionListener=object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

			}

			override fun onPermissionRationaleShouldBeShown(
				permissions: MutableList<PermissionRequest>?,
				token: PermissionToken?
			) {
				Toast.makeText(this@MainActivity, "Accept permissions first!", Toast.LENGTH_LONG)
			}
		}
		Dexter.withActivity(this).withPermissions(permissionArray).withListener(permissionListener).check()

	}
	fun checkIfNfcIsEnabled():Boolean{
		return if(nfcAdapter.isEnabled)
			true
		else{
			Toast.makeText(this,"Turn on NFC", Toast.LENGTH_LONG)
			false
		}
	}
	fun startNfcConnection(){
		askForNFCPermissions()
		nfcAdapter = NfcAdapter.getDefaultAdapter(this)
		//checkIfDeviceSupportNFC()
	}
*/
}


