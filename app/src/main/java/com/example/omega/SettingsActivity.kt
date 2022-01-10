package com.example.omega
import android.app.Dialog
import android.content.DialogInterface
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.dialog_select_auth_methode.*


class SettingsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		fillGuiSettingsWithSavedState()

		val selectAuthMethodeField = findViewById<TextView>(R.id.selectAuthMethodeTextView)
		val selectAuthMethodeFieldListener = View.OnClickListener {
			showSelectAuthMethodeDialog()
		}
		selectAuthMethodeField.setOnClickListener(selectAuthMethodeFieldListener)

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
		var methodeName = Utilites.getAuthMethodeText(this,methodeCode)
		val textToSetOnSelectAuthMethode = getString(R.string.GUI_selectAuthMethodeText_base) + methodeName
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

		val radioButtonGroup = dialog.selectAuthMethodeButtonGroup
		val userPreferredMethodeCode = Utilites.readPref_Int(this,R.integer.PREF_preferedAuthMethode)
		when(userPreferredMethodeCode){
			0 -> {dialog.selectAuthMethodeButton_PIN.isChecked = true}
			1 -> {dialog.selectAuthMethodeButton_patern.isChecked = true}
			2 -> {dialog.selectAuthMethodeButton_finger.isChecked = true}
		}
		val radioButtonGroupListener = RadioGroup.OnCheckedChangeListener { radioButtonGroup, radioButtonId -> // checkedId is the RadioButton selected
			dialog.dismiss()
		}
		radioButtonGroup.setOnCheckedChangeListener(radioButtonGroupListener)

		val dialogOnDismissListener = DialogInterface.OnDismissListener{
			var methodCode = Utilites.readPref_Int(this,R.integer.PREF_preferedAuthMethode)
			var methodeName = Utilites.getAuthMethodeText(this,methodCode)
			when(radioButtonGroup.checkedRadioButtonId){
				dialog.selectAuthMethodeButton_PIN.id ->{
					methodeName = getString(R.string.GUI_selectAuthMethodeText_pin)
					methodCode = 0
				}
				dialog.selectAuthMethodeButton_patern.id ->{
					methodeName = getString(R.string.GUI_selectAuthMethodeText_pattern)
					methodCode = 1
				}
				dialog.selectAuthMethodeButton_finger.id ->{
					methodeName = getString(R.string.GUI_selectAuthMethodeText_finger)
					methodCode = 2
				}
			}
			Utilites.savePref(this,R.integer.PREF_preferedAuthMethode,methodCode)
			val textToSetOnWidget = getString(R.string.GUI_selectAuthMethodeText_base) + methodeName
			findViewById<TextView>(R.id.selectAuthMethodeTextView).text = textToSetOnWidget
		}
		dialog.setOnDismissListener(dialogOnDismissListener)

		dialog.show()
	}
	override fun onBackPressed() {
		super.onBackPressed()
		saveResults()
	}

}