package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class ResultActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_result)
		setUpGUI()
	}
	private fun setUpGUI(){
		findViewById<Button>(R.id.result_backButton).setOnClickListener{
			finish()
		}
		val textToSet = intent.getStringExtra(resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME))
		findViewById<TextView>(R.id.result_TextView).text = textToSet

		val imageToSet = when(textToSet){
			resources.getString(R.string.Result_GUI_OK)->ContextCompat.getDrawable(this, R.drawable.ico_success)
			resources.getString(R.string.Result_GUI_PAYMENT_ACCEPTED)->ContextCompat.getDrawable(this, R.drawable.ico_success)
			else -> ContextCompat.getDrawable(this, R.drawable.ico_failure)
		}
		findViewById<ImageView>(R.id.result_imageView).setImageDrawable(imageToSet)
	}
}