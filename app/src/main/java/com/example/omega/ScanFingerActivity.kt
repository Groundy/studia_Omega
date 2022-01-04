package com.example.omega

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.net.wifi.hotspot2.pps.Credential
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt

class ScanFingerActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scan_finger)
		val errorText = checkIfFingerScanningIsPossible(this)
		val canAuth = errorText == null
		if(canAuth)
			showAuthDialog()
		else
			finishActivity(false,errorText)
	}
	private fun showAuthDialog(){
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle(getString(R.string.fingerAuthTitle))
			.setSubtitle(getString(R.string.fingerAuthSubTitlt))
			.setNegativeButtonText(getString(R.string.usePinInsteadOfFingerPrint))
			.build()
		val executor = mainExecutor
		val authCallBack = object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				Utilites.showToast(this@ScanFingerActivity,"Authentication error: $errString")
				Utilites.showToast(this@ScanFingerActivity,"Authentication error code: $errorCode")
				when(errString){
					getString(R.string.usePinInsteadOfFingerPrint) ->{
						//TODO obsługa wypadku w którym użytkownik woli podać pin zamiast odcisku palca
					}
				}
			}
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				//TODO obsługa wypadku prawidłowej autoryzacji
				super.onAuthenticationSucceeded(result)
				Toast.makeText(this@ScanFingerActivity,"Authentication succeeded!", Toast.LENGTH_SHORT).show()
				finishActivity(true,null)
			}
			override fun onAuthenticationFailed() {
				//TODO obsługa wypadku NIEprawidłowej autoryzacji
				super.onAuthenticationFailed()
				Utilites.showToast(this@ScanFingerActivity, "Authentication failed")
				finishActivity(false,null)
			}
		}
		val biometricPrompt = BiometricPrompt(this@ScanFingerActivity, executor, authCallBack)
		biometricPrompt.authenticate(promptInfo)
	}
	private fun checkIfFingerScanningIsPossible(context : Context) : String?{
		val biometricManager = BiometricManager.from(context)
		val retCode = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
		when (retCode) {
			BiometricManager.BIOMETRIC_SUCCESS ->{
				Log.i("WookieTag", "App can authenticate using biometrics.")
				return null
			}
			BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
				val errorText = "No biometric features available on this device."
				Log.e("WookieTag", errorText)
				return errorText
			}
			BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
				val errorText = "Biometric features are currently unavailable."
				Log.e("WookieTag", errorText)
				return errorText
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
				return errorText
			}
			else -> {
				//Prawodobonie brak zapisanego odcisku palca, android z niewiadomych przyczyn zwraca kod -1
				// przy braku zapisanego odcisku palca zamiast kodu 11(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
				val errorText = "Unkown behaviour in chechking if finger auth is possible."
				Log.e("WookieTag", errorText)//TODO tutaj powinien byc false, true jest tylko do testów
				return errorText
			}
		}
	}
	private fun finishActivity(result: Boolean, textToShowToUser : String?){
		val output = Intent()
		output.putExtra("result", result)
		if(textToShowToUser != null)
			Utilites.showToast(this,textToShowToUser)
		setResult(RESULT_OK, output)
		finish()
	}
}