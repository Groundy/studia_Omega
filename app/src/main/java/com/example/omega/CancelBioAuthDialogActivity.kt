package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView

class CancelBioAuthDialogActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activityasdialog_cancel_bio_auth)

		setFinishOnTouchOutside(false)
		val okButton = findViewById<TextView>(R.id.fingerAuthCancelOkTextTextView)
		val cancelButton = findViewById<TextView>(R.id.fingerAuthCancelCancelTextTextView)
		okButton.setOnClickListener {
			closeActivity(true)
		}
		cancelButton.setOnClickListener {
			closeActivity(false)
		}
	}
	private fun closeActivity(ok: Boolean){
		if(ok)
			setResult(RESULT_OK, Intent())
		else
			setResult(RESULT_CANCELED, Intent())
		finish()
	}
}