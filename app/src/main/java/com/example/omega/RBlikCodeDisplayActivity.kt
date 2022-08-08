package com.example.omega

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.graphics.drawable.toBitmap
import com.example.omega.Utilities.Companion.TagProduction

class RBlikCodeDisplayActivity : AppCompatActivity() {
	private lateinit var imgWidget : ImageView
	private lateinit var codeDisplayField : TextView

	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_blik_code_display)
		setUpGui()

		val code = getCodeFromIntent()
		if(code == -1){
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_incorrectCodePassed))
			Log.e(TagProduction, "Error, passed code to display RBlik class was incorrect")
			finish()
		}
		setProperWidgetText(code)
		var qrCodeBitmap = generateQRCodeImg(code)
		if(qrCodeBitmap == null){
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_qrGeneratorError))
			Log.e(TagProduction, "[onCreate/${this.javaClass.name}] qr generator return null instead of bitmap.")
			qrCodeBitmap = resources.getDrawable(R.drawable.ico_failure, null).toBitmap()
		}
		imgWidget.setImageBitmap(qrCodeBitmap)
	}

	private fun generateQRCodeImg(code: Int) : Bitmap?{
		val qrgEncoder = QRGEncoder(code.toString(), null, QRGContents.Type.TEXT, 300)
		return try {
			qrgEncoder.encodeAsBitmap()
		} catch (e: WriterException) {
			Log.e(TagProduction, "[generateQRCodeImg/${this.javaClass.name}] error txt = $e")
			null
		}
	}
	private fun getCodeFromIntent() : Int{
		val fieldName = getString(R.string.ACT_COM_CODEGENERATOR_CODE_FOR_DISPLAY_FIELDNAME)
		return intent.getIntExtra(fieldName, -1)
	}
	private fun setProperWidgetText(code : Int){
		var codeStr = code.toString()
		if(codeStr.length < 6){
			val missingZerosOnFrontOfStr = 6 - codeStr.length
			repeat(missingZerosOnFrontOfStr){
				codeStr = StringBuilder(codeStr).insert(0,"0").toString()
			}
		}
		codeStr = StringBuilder(codeStr).insert(3," ").toString()
		codeDisplayField.text = codeStr
	}
	private fun setUpGui(){
		codeDisplayField = findViewById(R.id.BLIKDISPLAY_code_TextView)
		imgWidget = findViewById(R.id.BLIKDISPLAY_QR_ImageView)
		findViewById<Button>(R.id.BLIKDISPLAY_back_button).setOnClickListener{
			finish()
		}
	}
}