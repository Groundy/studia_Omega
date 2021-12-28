package com.example.omega

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
		fillGuiSettingsWithSavedState()
	}

	private fun fillGuiSettingsWithSavedState() {
		val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)

		nfcSwitch.isChecked = Utilites.readPref_Bool(this, R.bool.turnNfcOnAppStart)
	}

	private fun saveResults() {
		val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)

		Utilites.savePref(this, R.bool.turnNfcOnAppStart, nfcSwitch.isChecked)
	}

	override fun onBackPressed() {
		super.onBackPressed()
		saveResults()
	}

}