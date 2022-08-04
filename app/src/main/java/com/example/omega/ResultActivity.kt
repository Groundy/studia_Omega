package com.example.omega

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.omega.Utilities.Companion.TagProduction

class ResultActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_result)
		setUpGUI()
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private fun setUpGUI(){
		findViewById<Button>(R.id.result_backButton).setOnClickListener{
			finish()
		}
		val textToSet = intent.getStringExtra(resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME))
		findViewById<TextView>(R.id.result_TextView).text = textToSet

		val imageToSet = when(textToSet){
			resources.getString(R.string.Result_GUI_OK)->resources.getDrawable(R.drawable.ok_img,null)
			else -> resources.getDrawable(R.drawable.wrong_img,null)
		}
		findViewById<ImageView>(R.id.result_imageView).setImageDrawable(imageToSet)
	}
}