package com.example.omega
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class SettingsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
		fillGuiSettingsWithSavedState()
	}

	private fun fillGuiSettingsWithSavedState() {
		val phoneHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		if(phoneHasNfc){
			val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)
			nfcSwitch.isChecked = Utilites.readPref_Bool(this, R.bool.turnNfcOnAppStart)
		}
		else{
			findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch).isVisible = false
		}

	}

	private fun saveResults() {
		val phoneHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		if(phoneHasNfc){
			val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)
			Utilites.savePref(this, R.bool.turnNfcOnAppStart, nfcSwitch.isChecked)
		}

	}

	override fun onBackPressed() {
		super.onBackPressed()
		saveResults()
	}

}