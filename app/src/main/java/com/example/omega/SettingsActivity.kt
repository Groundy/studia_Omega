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
		setContentView(R.layout.settings_activity)
		fillGuiSettingsWithSavedState()

		val selectAuthMethodeField = findViewById<TextView>(R.id.selectAuthMethodeTextView)
		val selectAuthMethodeFieldListener = View.OnClickListener {
			showSelectAuthMethodeDialog()
		}
		selectAuthMethodeField.setOnClickListener(selectAuthMethodeFieldListener)

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

		val selectAuthMethodeField = findViewById<TextView>(R.id.selectAuthMethodeTextView)
		val methodeCode = Utilites.readPref_Int(this, R.integer.preferedAuthMethode)
		var methodeName: String = ""
		methodeName = when(methodeCode){
			0 -> getString(R.string.selectAuthMethodeText_pin)
			1 -> getString(R.string.selectAuthMethodeText_pattern)
			2 -> getString(R.string.selectAuthMethodeText_finger)
			else -> getString(R.string.selectAuthMethodeText_pin)
		}
		val textToSetOnSelectAuthMethode = getString(R.string.selectAuthMethodeText_base) + methodeName
		selectAuthMethodeField.text = textToSetOnSelectAuthMethode


	}
	private fun saveResults() {
		val phoneHasNfc = NfcAdapter.getDefaultAdapter(this) != null
		if(phoneHasNfc){
			val nfcSwitch = findViewById<Switch>(R.id.turnOnNFCWhenAppStartsSwitch)
			Utilites.savePref(this, R.bool.turnNfcOnAppStart, nfcSwitch.isChecked)
		}

	}
	private fun showSelectAuthMethodeDialog(){
		val dialog : Dialog = Dialog(this)
		dialog.setContentView(R.layout.dialog_select_auth_methode)
		val radioButtonGroup = dialog.selectAuthMethodeButtonGroup
		val radioButtonGroupListener = RadioGroup.OnCheckedChangeListener { radioButtonGroup, radioButtonId -> // checkedId is the RadioButton selected
			dialog.dismiss()
		}
		val dialogOnDismissListener = DialogInterface.OnDismissListener{
			var valueCodeToSave = 0
			var methodeName = ""
			when(radioButtonGroup.checkedRadioButtonId){
				dialog.selectAuthMethodeButton_PIN.id ->{
					methodeName = getString(R.string.selectAuthMethodeText_pin)
					valueCodeToSave = 0
				}
				dialog.selectAuthMethodeButton_patern.id ->{
					methodeName = getString(R.string.selectAuthMethodeText_pattern)
					valueCodeToSave = 1
				}
				dialog.selectAuthMethodeButton_finger.id ->{
					methodeName = getString(R.string.selectAuthMethodeText_finger)
					valueCodeToSave = 2
				}
				else ->{
					methodeName = getString(R.string.selectAuthMethodeText_pin)
					valueCodeToSave = 0
				}
			}
			Utilites.savePref(this,R.integer.preferedAuthMethode,valueCodeToSave)
			val textToSetOnWidget = getString(R.string.selectAuthMethodeText_base) + methodeName
			findViewById<TextView>(R.id.selectAuthMethodeTextView).text = textToSetOnWidget
		}
		dialog.setOnDismissListener(dialogOnDismissListener)
		radioButtonGroup.setOnCheckedChangeListener(radioButtonGroupListener)
		dialog.show()
	}
	override fun onBackPressed() {
		super.onBackPressed()
		saveResults()
	}

}