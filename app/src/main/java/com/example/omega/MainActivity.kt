package com.example.omega
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.settings_activity.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter


class MainActivity: AppCompatActivity() {
	private lateinit var goQRActivityButton: Button
	private lateinit var codeField: EditText
	private lateinit var nfcOnOffButton : Button

	var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent) {
			val s1 = intent.getStringExtra("code")
			codeField.setText(s1.toString())
		}
	}
	private val codeFieldTextListener = object : TextWatcher {
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
	private val goQRScannerButtonListener =  View.OnClickListener{
		val qRScannerActivityIntent = Intent(this, QRScannerActivity::class.java)
		startActivityForResult(qRScannerActivityIntent, scannerRetCode)
	}
	private val nfcButtonListener = View.OnClickListener {
		val imgFirst = nfcOnOffButton.background.constantState
		val imgSecond = getDrawable(R.drawable.nfc_on_icon)!!.constantState
		val isAlreadyTurnedOn = imgFirst == imgSecond
		if(isAlreadyTurnedOn) turnNFCOFF() else turnNFCON()
	}
	private var nfcConnectorIntent : Intent? = null
	private val scannerRetCode = 0x101

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
	private fun turnNFCON(){
		val currentlyTurnedOff = nfcConnectorIntent == null
		if(currentlyTurnedOff){
			val intentFilter = IntentFilter()
			nfcConnectorIntent = Intent (this,NFCThread::class.java)
			intentFilter.addAction("NFCThread")
			registerReceiver(broadcastReceiver,intentFilter)
			startService(nfcConnectorIntent)
			nfcOnOffButton.setBackgroundResource(R.drawable.nfc_on_icon)
		}
	}
	private fun turnNFCOFF(){
		val alreadyTurnedOn = nfcConnectorIntent != null
		if(alreadyTurnedOn){
			nfcOnOffButton.setBackgroundResource(R.drawable.nfc_off_icon)
			unregisterReceiver(broadcastReceiver)
			stopService(nfcConnectorIntent)
			nfcConnectorIntent =  null
		}
	}
	override fun onStop() {
		super.onStop()
		unregisterReceiver(broadcastReceiver);
	}
}
