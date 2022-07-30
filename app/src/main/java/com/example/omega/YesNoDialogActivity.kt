package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView

class YesNoDialogActivity : AppCompatActivity() {
	companion object{
		enum class DialogPurpose{ CancelBioAuth, ResetAuthUrl, LoginToBankAccount}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_yes_no_dialog)
		setFinishOnTouchOutside(false)
		setListenersToButtons()
		setText()
	}

	private fun setListenersToButtons(){
		findViewById<TextView>(R.id.DialogAct_OK_Button).setOnClickListener {
			setResult(RESULT_OK, Intent())
			finish()
		}
		findViewById<TextView>(R.id.DialogAct_No_Button).setOnClickListener {
			setResult(RESULT_CANCELED, Intent())
			finish()
		}
	}
	private fun setText(){
		val fieldName = getString(R.string.ACT_COM_DIALOG_TextToDisplay_FIELDNAME)
		val mainTextToDisplay = intent.getStringExtra(fieldName)
		findViewById<TextView>(R.id.DialogAct_TextView).text = mainTextToDisplay
	}

}