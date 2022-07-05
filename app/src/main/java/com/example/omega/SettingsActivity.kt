package com.example.omega
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.dialog_select_auth_methode.*


class SettingsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		fillGuiSettingsWithSavedState()
		addListenersToGuiElements()
	}

	private fun fillGuiSettingsWithSavedState() {
		val phoneHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)
		if(phoneHasNfc)
			nfcSwitch.isChecked = Utilites.readPref_Bool(this,R.bool.PREF_turnNfcOnAppStart)
		else
			nfcSwitch.isVisible = false

		val selectAuthMethodeField = findViewById<TextView>(R.id.selectAuthMethodeTextView)
		val methodeCode = Utilites.readPref_Int(this, R.integer.PREF_preferedAuthMethode)
		var methodeName = when(methodeCode){
			0->getString(R.string.GUI_selectAuthMethodeText_pin)
			1->getString(R.string.Settings_GUI_selectAuthMethodeTextPattern)
			2->getString(R.string.Settings_GUI_selectAuthMethodeTextFinger)
			else ->getString(R.string.GUI_selectAuthMethodeText_pin)
		}

		val textToSetOnSelectAuthMethode = getString(R.string.Settings_GUI_selectAuthMethodeTextBase) + methodeName
		selectAuthMethodeField.text = textToSetOnSelectAuthMethode
	}
	private fun saveResults() {
		val phoneHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		if(phoneHasNfc){
			val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)
			Utilites.savePref(this, R.bool.PREF_turnNfcOnAppStart, nfcSwitch.isChecked)
		}

	}
	private fun showSelectAuthMethodeDialog(){
		val dialog : Dialog = Dialog(this)
		dialog.setContentView(R.layout.dialog_select_auth_methode)
		val phoneHasFingerSensor = phoneHasFingerSensor()
		if(!phoneHasFingerSensor)
			dialog.selectAuthMethodeButton_finger.isVisible = false
		dialog.selectAuthMethodeButton_patern.isVisible = false //TODO to nie jest zaimplementowane
		val radioButtonGroup = dialog.selectAuthMethodeButtonGroup
		val userPreferredMethodeCode = Utilites.readPref_Int(this,R.integer.PREF_preferedAuthMethode)
		when(userPreferredMethodeCode){
			0 -> {dialog.selectAuthMethodeButton_PIN.isChecked = true}
			1 -> {dialog.selectAuthMethodeButton_patern.isChecked = true}
			2 -> {
				if(phoneHasFingerSensor)
					dialog.selectAuthMethodeButton_finger.isChecked = true
				else {
					dialog.selectAuthMethodeButton_PIN.isChecked = true
					Utilites.savePref(this,R.integer.PREF_preferedAuthMethode,0)
				}
			}
		}
		val radioButtonGroupListener = RadioGroup.OnCheckedChangeListener { radioButtonGroup, radioButtonId -> // checkedId is the RadioButton selected
			dialog.dismiss()
		}
		radioButtonGroup.setOnCheckedChangeListener(radioButtonGroupListener)

		val dialogOnDismissListener = DialogInterface.OnDismissListener{
			var methodCode = Utilites.readPref_Int(this,R.integer.PREF_preferedAuthMethode)
			var methodeName = when(methodCode){
				0->getString(R.string.GUI_selectAuthMethodeText_pin)
				1->getString(R.string.Settings_GUI_selectAuthMethodeTextPattern)
				2->getString(R.string.Settings_GUI_selectAuthMethodeTextFinger)
				else ->getString(R.string.GUI_selectAuthMethodeText_pin)
			}
			when(radioButtonGroup.checkedRadioButtonId){
				dialog.selectAuthMethodeButton_PIN.id ->{
					methodeName = getString(R.string.GUI_selectAuthMethodeText_pin)
					methodCode = 0
				}
				dialog.selectAuthMethodeButton_patern.id ->{
					methodeName = getString(R.string.Settings_GUI_selectAuthMethodeTextPattern)
					methodCode = 1
				}
				dialog.selectAuthMethodeButton_finger.id ->{
					methodeName = getString(R.string.Settings_GUI_selectAuthMethodeTextFinger)
					methodCode = 2
				}
			}
			Utilites.savePref(this,R.integer.PREF_preferedAuthMethode,methodCode)
			val textToSetOnWidget = getString(R.string.Settings_GUI_selectAuthMethodeTextBase) + methodeName
			findViewById<TextView>(R.id.selectAuthMethodeTextView).text = textToSetOnWidget
		}
		dialog.setOnDismissListener(dialogOnDismissListener)
		dialog.show()
	}
	private fun startPinChangeActivity(){
		val changePinActivityIntent = Intent(this, PinActivity::class.java)
		val activityReason = resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[2]
		val activityReasonFieldName = resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
		changePinActivityIntent.putExtra(activityReasonFieldName,activityReason)
		val retCodeForActivity  = resources.getInteger(R.integer.ACT_RETCODE_PIN_CHANGE)
		startActivityForResult(changePinActivityIntent, retCodeForActivity)
	}
	override fun onBackPressed() {
		super.onBackPressed()
		saveResults()
	}
	private fun addListenersToGuiElements(){
		val selectAuthMethodeField = findViewById<TextView>(R.id.selectAuthMethodeTextView)
		selectAuthMethodeField.setOnClickListener {
			showSelectAuthMethodeDialog()
		}
		val changePinField = findViewById<TextView>(R.id.changePinSettingsTextView)
		changePinField.setOnClickListener{
			startPinChangeActivity()
		}
	}
	private fun phoneHasFingerSensor() : Boolean{
		val biometricManager = BiometricManager.from(this)
		val errorCode = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
		val phoneCanUseFingerAuth = errorCode == 0
		return phoneCanUseFingerAuth
	}

}