package com.example.omega

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


class RBlikCodeDisplayActivity : AppCompatActivity() {
	private lateinit var imgWidget : ImageView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_blik_code_display)
		imgWidget = findViewById(R.id.BLIKDISPLAY_QR_ImageView)
		findViewById<Button>(R.id.BLIKDISPLAY_back_button).setOnClickListener{
			this.finish()
		}

		val code = getCodeFromIntent()
		if(code == -1){
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_incorrectCodePassed))
			Log.e(Utilities.TagProduction, "Error, passed code to display RBlik class was incorrect")
			finish()
		}
		setProperWidgetText(code)

		val qrCodeBitmap = generateQRCodeImg(code)
		val qrBitmapOk = qrCodeBitmap != null

		if(qrBitmapOk){
			imgWidget.setImageBitmap(qrCodeBitmap)
		}
		else{
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_qrGeneratorError))
			Log.e(Utilities.TagProduction, "Error in RBlik display class, qr generator return null instead of bitmap.")
			val errorImg = resources.getDrawable(R.drawable.wrong_img).toBitmap()
			imgWidget.setImageBitmap(errorImg)
		}
	}

	private fun generateQRCodeImg(code: Int) : Bitmap?{
		val qrgEncoder = QRGEncoder(code.toString(), null, QRGContents.Type.TEXT, 300)
		var bitmap = try {
			qrgEncoder.encodeAsBitmap()
		} catch (e: WriterException) {
			null//todo
		}
		return bitmap
	}
	private fun getCodeFromIntent() : Int{
		val fieldName = getString(R.string.ACT_COM_CODEGENERATOR_CODE_FOR_DISPLAY)
		return intent.getIntExtra(fieldName, -1)
	}
	private fun setProperWidgetText(code : Int){
		val textView = findViewById<TextView>(R.id.BLIKDISPLAY_QR_code_ImageView)
		var codeStr = code.toString()
		if(codeStr.length < 6){
			val missingZerosOnFrontOfStr = 6 - codeStr.length
			repeat(missingZerosOnFrontOfStr){
				codeStr = StringBuilder(codeStr).insert(0,"0").toString()
			}
		}
		codeStr = StringBuilder(codeStr).insert(3," ").toString()
		textView.text = codeStr
	}
}