package com.example.omega

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

class ScanFingerActivity : AppCompatActivity() {
	private val USER_WANT_TO_USE_OTHER_AUTH_METHODE = 13
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scan_finger)

		val errorCode = checkIfFingerScanningIsPossible(this)
		val canAuth = errorCode == 0
		if(canAuth)
			showAuthDialog()
		else
			finishActivity(false,errorCode)
	}
	private fun showAuthDialog(){
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle(getString(R.string.GUI_fingerAuthTitle))
			.setDescription(additionalDescription())
			.setNegativeButtonText(getString(R.string.GUI_usePinInsteadOfFingerPrint))
			.build()
		val authCallBack = object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				Log.e("WookieTag","Authentication error code: $errorCode")
				if(errorCode == USER_WANT_TO_USE_OTHER_AUTH_METHODE)
					Log.i("WookieTag", "User wants to other auth methode than fingerPrint")
				finishActivity(false, errorCode)
			}
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				Log.i("WookieTag","Auth by fingerPrint was correct")
				finishActivity(true,0)
			}
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				Log.i("WookieTag","Auth by fingerPrint was incorrect")
			}
		}
		val biometricPrompt = BiometricPrompt(this@ScanFingerActivity, mainExecutor, authCallBack)
		biometricPrompt.authenticate(promptInfo)
	}
	private fun checkIfFingerScanningIsPossible(context : Context) : Int{
		val biometricManager = BiometricManager.from(context)
		val errorCode = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
		when (errorCode) {
			BiometricManager.BIOMETRIC_SUCCESS ->{
				Log.i("WookieTag", "App can authenticate using biometrics.")
			}
			BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
				val errorText = "No biometric features available on this device."
				Log.e("WookieTag", errorText)
			}
			BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
				val errorText = "Biometric features are currently unavailable."
				Log.e("WookieTag", errorText)
			}
			BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
				val errorText = "Biometric features should take user to fingerPrint."
				/*
					// Prompts the user to create credentials that your app accepts.
					val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
						putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,BIOMETRIC_STRONG)
					}
					startActivityForResult(enrollIntent, 101)
				*/
				Log.e("WookieTag", errorText)
			}
			else -> {
				//Prawodobonie brak zapisanego odcisku palca, android z niewiadomych przyczyn zwraca kod -1
				// przy braku zapisanego odcisku palca zamiast kodu 11(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
				val errorText = "Unkown behaviour in chechking if finger auth is possible."
				Log.e("WookieTag", errorText)//TODO tutaj powinien byc false, true jest tylko do testów
			}
		}
		return errorCode
	}
	private fun finishActivity(result: Boolean, errorCode : Int){
		val errorCodeFieldName = resources.getString(R.string.ACT_COM_Fnger_fieldName)
		val output = Intent()
		output.putExtra(errorCodeFieldName, errorCode)
		if(result)
			setResult(RESULT_OK, output)
		else
			setResult(RESULT_CANCELED, output)
		finish()
	}
	private fun additionalDescription() : String?{
		val description : String? = this.intent.getStringExtra(getString(R.string.ACT_COM_Fnger_fieldName))
		return description
	}
}