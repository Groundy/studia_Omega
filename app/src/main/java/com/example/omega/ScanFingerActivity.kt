package com.example.omega

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.example.omega.Utilities.Companion.TagProduction

class ScanFingerActivity : AppCompatActivity() {
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
			.setTitle(getString(R.string.GUI_authTransactionTitle))
			.setDescription(getAdditionalDescription())
			.setNegativeButtonText(getString(R.string.FingerScan_GUI_usePinInsteadOfFingerPrint))
			.build()
		val authCallBack = object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				Log.e(TagProduction,"Authentication error code: $errorCode")

				when (errorCode) {
					BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
						Log.i(TagProduction, "User wants to other auth methode than fingerPrint")
						finishActivity(false, errorCode)
					}
					BiometricPrompt.ERROR_USER_CANCELED ->
						ActivityStarter.openDialogWithDefinedPurpose(this@ScanFingerActivity, YesNoDialogActivity.Companion.DialogPurpose.CancelBioAuth)
					else -> finishActivity(false,errorCode)
				}
			}
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				Log.i(TagProduction,"Auth by fingerPrint was correct")
				finishActivity(true,0)
			}
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				Log.i(TagProduction,"Auth by fingerPrint was incorrect")
			}
		}
		val biometricPrompt = BiometricPrompt(this@ScanFingerActivity, mainExecutor, authCallBack)
		biometricPrompt.authenticate(promptInfo)
	}
	private fun checkIfFingerScanningIsPossible(context : Context) : Int{
		val biometricManager = BiometricManager.from(context)
		val errorCode = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
		when (errorCode) {
			BiometricManager.BIOMETRIC_SUCCESS ->
				Log.i(TagProduction, "App can authenticate using biometrics.")
			BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
				Log.e(TagProduction, "No biometric features available on this device.")
			BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
				Log.e(TagProduction, "Biometric features are currently unavailable.")
			BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
				Log.e(TagProduction, "Biometric features should take user to fingerPrint.")
			else -> {
				//Prawodobonie brak zapisanego odcisku palca, android z niewiadomych przyczyn zwraca kod -1
				// przy braku zapisanego odcisku palca zamiast kodu 11(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
				Log.e(TagProduction, "Unknown behaviour in checking if finger auth is possible, probably no finer enrolled")
			}
		}
		return errorCode
	}
	private fun finishActivity(result: Boolean, errorCode : Int){
		val errorCodeFieldName = resources.getString(R.string.ACT_COM_FINGER_FIELD_NAME)
		val descriptionFieldName = getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
		val output = Intent()
			.putExtra(descriptionFieldName, getAdditionalDescription())
			.putExtra(errorCodeFieldName, errorCode)

		if(result)
			setResult(RESULT_OK, output)
		else
			setResult(RESULT_CANCELED, output)
		finish()
	}
	private fun getAdditionalDescription(): String? {
		return intent.getStringExtra(getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME))
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == resources.getInteger(R.integer.ACT_RETCODE_DIALOG_CancelBioAuth)){
			if(resultCode == RESULT_OK)
				finishActivity(false,BiometricPrompt.ERROR_USER_CANCELED)
			else
				showAuthDialog()
		}
	}

}