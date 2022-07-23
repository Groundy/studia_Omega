package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView

class YesNoDialogActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_yes_no_dialog)
		setFinishOnTouchOutside(false)
		setListenersToButtons()
		setText()
	}

	private fun closeActivity(ok: Boolean){
		val returnValue = if(ok) RESULT_OK else RESULT_CANCELED
		setResult(returnValue, Intent())
		finish()
	}
	private fun setListenersToButtons(){
		findViewById<TextView>(R.id.DialogAct_OK_Button).setOnClickListener {
			closeActivity(true)
		}
		findViewById<TextView>(R.id.DialogAct_No_Button).setOnClickListener {
			closeActivity(false)
		}
	}
	private fun setText(){
		val fieldName = getString(R.string.ACT_COM_DIALOG_TEXT_FIELDNAME)
		val mainTextToDisplay = intent.getStringExtra(fieldName)
		findViewById<TextView>(R.id.DialogAct_TextView).text = mainTextToDisplay
	}
}