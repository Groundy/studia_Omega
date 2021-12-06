package com.example.omega
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.settings_activity.*

class MainActivity: AppCompatActivity() {
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText
	private lateinit var nfcOnOffButton : Button
	private val QR_SCANNER_ACTIVITY_RET_CODE = 0x101

	@SuppressLint("ResourceType")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		initUIVariables()
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
		goQRActivityButton = findViewById(R.id.goToQRScannerButton)
		codeField = findViewById(R.id.enterCodeField)
		codeField.requestFocus()
		goQRActivityButton.setOnClickListener(goQRScannerButtonListener)
		codeField.addTextChangedListener(codeFieldTextListener)
		nfcOnOffButton = findViewById(R.id.nfcButton)
		nfcOnOffButton.setOnClickListener(nfcButtonListener)
	}
	private fun test(){

	}

	private val codeFieldTextListener = object : TextWatcher {

		override fun afterTextChanged(s: Editable) {
			if(s.length == 6){
				val code = s.toString().toInt()
				processCode(code)
			}
		}

		override fun beforeTextChanged(s: CharSequence, start: Int,
		                               count: Int, after: Int) {
		}

		override fun onTextChanged(s: CharSequence, start: Int,
		                           before: Int, count: Int) {
		}
	}
	private val goQRScannerButtonListener =  View.OnClickListener{
		val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
		startActivityForResult(qRScannerActivityIntent, QR_SCANNER_ACTIVITY_RET_CODE)
	}
	private val nfcButtonListener = View.OnClickListener {
		val imgFirst = nfcOnOffButton.background.constantState
		val imgSecond = getDrawable(R.drawable.nfc_on_icon)!!.constantState
		val isAlreadyTurnedOn = imgFirst == imgSecond
		if(isAlreadyTurnedOn){
			//Turn off
			nfcOnOffButton.setBackgroundResource(R.drawable.nfc_off_icon)
		}else{
			nfcOnOffButton.setBackgroundResource(R.drawable.nfc_on_icon)
			//Turn off
		}
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



