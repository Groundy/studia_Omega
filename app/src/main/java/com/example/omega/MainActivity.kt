package com.example.omega
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.Intent
import android.widget.Button
import android.widget.EditText


class MainActivity: AppCompatActivity() {
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText
	private val QR_SCANNER_ACTIVITY_RET_CODE = 0x101

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		codeField = findViewById(R.id.enterCodeField)
		codeField.requestFocus()
		goQRActivityButton.setOnClickListener() {
			var returnCode: Int = 0
			val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
			startActivityForResult(qRScannerActivityIntent, QR_SCANNER_ACTIVITY_RET_CODE)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == QR_SCANNER_ACTIVITY_RET_CODE && resultCode == RESULT_OK){
			if(data!= null){
				val returnedCode = data.getIntExtra("codeFromQR",-1)
				val vailCode = returnedCode in 0..999999
				if(vailCode) {
					codeField.setText(returnedCode.toString())
					processCode(returnedCode)
				}
			}
		}


	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	private fun processCode(code : Int){
		Utilites.showToast(this,"process: " + code.toString())
	}
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



