package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

class RBlikCodeDisplayActivity : AppCompatActivity() {
	private lateinit var imgWidget : ImageView
	private lateinit var codeDisplayField : TextView
	private lateinit var data : ServerSetCodeResponse
	private lateinit var timer : CountDownTimer

	override fun onResume() {
		super.onResume()
		val timeLeft = OmegaTime.getSecondsToStampExpiration(data.timestamp,0)
		if(timeLeft <0 ){
			finish()
		}
	}
	override fun onDestroy() {
		super.onDestroy()
		timer.cancel()
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_blik_code_display)
		setUpGui()

		val success = getServerResponseFromIntent()
		if(!success){
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_incorrectCodePassed))
			finish()
			return
		}
		setProperWidgetText()
		var qrCodeBitmap = generateQRCodeImg(data.code)
		if(qrCodeBitmap == null){
			Utilities.showToast(this, getString(R.string.RBLIKDISPLAY_UserMsg_qrGeneratorError))
			Log.e(TagProduction, "[onCreate/${this.javaClass.name}] qr generator return null instead of bitmap.")
			qrCodeBitmap = ContextCompat.getDrawable(this, R.drawable.ico_failure)!!.toBitmap()
		}
		imgWidget.setImageBitmap(qrCodeBitmap)
		startTimer()
		CoroutineScope(IO).launch{
			val paymentAccepted = CodeServerApi.waitCodeDone(this@RBlikCodeDisplayActivity, data.code)
			if(paymentAccepted)
				endActivityWithPaymentAcceptance()
		}
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
	private fun getServerResponseFromIntent() : Boolean{
		val fieldName = getString(R.string.ACT_COM_CODEGENERATOR_SERIALIZED_SERVER_RES_FIELD)
		return try {
			val serverResStr = intent.extras!!.getString(fieldName)
			data = ServerSetCodeResponse(serverResStr!!)
			true
		}catch (e : Exception){
			Log.e(TagProduction,"[getServerResponseFromIntent/${this.javaClass.name}] Cant parse data from intent")
			false
		}
	}
	private fun setProperWidgetText(){
		var codeStr = data.code.toString()
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
	private fun startTimer(){
		val secondsLeft = OmegaTime.getSecondsToStampExpiration(data.timestamp, 0)
		if(secondsLeft < 0){
			ActivityStarter.startOperationResultActivity(this@RBlikCodeDisplayActivity, R.string.Result_GUI_CODE_EXPIRED)
			setResult(RESULT_CANCELED)
			return
		}
		val timerView = findViewById<TextView>(R.id.BLIKDISPLAY_timer_textView)
		timer = object: CountDownTimer(secondsLeft*1000, 1000) {
			override fun onTick(millisUntilFinished: Long) {
				val secondsToLeft = floor((millisUntilFinished / 1000).toDouble())
				var minutesToDisplay = (floor(secondsToLeft / 60).toInt()).toString()
				if(minutesToDisplay.length == 1)
					minutesToDisplay = "0$minutesToDisplay"
				var secondsToDisplay = (secondsToLeft % 60).toInt().toString()
				if(secondsToDisplay.length == 1)
					secondsToDisplay = "0$secondsToDisplay"
				val textToSet = "$minutesToDisplay:$secondsToDisplay"
				timerView.text = textToSet
			}

			override fun onFinish() {
				ActivityStarter.startOperationResultActivity(this@RBlikCodeDisplayActivity, R.string.Result_GUI_CODE_EXPIRED)
				setResult(RESULT_CANCELED)
			}
		}
		timer.start()
	}
	private fun endActivityWithPaymentAcceptance(){
		timer.cancel()
		setResult(RESULT_OK)
		this.finish()
	}
}

