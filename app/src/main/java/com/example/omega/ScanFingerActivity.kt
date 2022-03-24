package com.example.omega

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import kotlinx.android.synthetic.main.dialog_select_auth_methode.*
import kotlinx.android.synthetic.main.activityasdialog_cancel_bio_auth.view.*

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
			.setNegativeButtonText(getString(R.string.GUI_usePinInsteadOfFingerPrint))
			.build()
		val authCallBack = object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				Log.e(Utilites.TagProduction,"Authentication error code: $errorCode")

				when (errorCode) {
					BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
						Log.i(Utilites.TagProduction, "User wants to other auth methode than fingerPrint")
						finishActivity(false, errorCode)
					}
					BiometricPrompt.ERROR_USER_CANCELED -> showCancelDialog(this@ScanFingerActivity)
					else -> finishActivity(false,errorCode)
				}
			}
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				Log.i(Utilites.TagProduction,"Auth by fingerPrint was correct")
				finishActivity(true,0)
			}
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				Log.i(Utilites.TagProduction,"Auth by fingerPrint was incorrect")
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
				Log.i(Utilites.TagProduction, "App can authenticate using biometrics.")
			BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
				Log.e(Utilites.TagProduction, "No biometric features available on this device.")
			BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
				Log.e(Utilites.TagProduction, "Biometric features are currently unavailable.")
			BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
				Log.e(Utilites.TagProduction, "Biometric features should take user to fingerPrint.")
			else -> {
				//Prawodobonie brak zapisanego odcisku palca, android z niewiadomych przyczyn zwraca kod -1
				// przy braku zapisanego odcisku palca zamiast kodu 11(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
				Log.e(Utilites.TagProduction, "Unknown behaviour in checking if finger auth is possible, probably no finer enrolled")
			}
		}
		return errorCode
	}
	private fun finishActivity(result: Boolean, errorCode : Int){
		val errorCodeFieldName = resources.getString(R.string.ACT_COM_FINGER_FIELD_NAME)
		val descriptionFieldName = getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
		val output = Intent()
		output.putExtra(descriptionFieldName, getAdditionalDescription())
		output.putExtra(errorCodeFieldName, errorCode)
		if(result)
			setResult(RESULT_OK, output)
		else
			setResult(RESULT_CANCELED, output)
		finish()
	}
	private fun getAdditionalDescription(): String? {
		return intent.getStringExtra(getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME))
	}

	private fun showCancelDialog(activity: ScanFingerActivity){
		val activityIntent = Intent(this@ScanFingerActivity, CancelBioAuthDialogActivity::class.java)
		startActivityForResult(activityIntent,resources.getInteger(R.integer.ACT_RETCODE_CANCEL_BIO_AUTH))
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == resources.getInteger(R.integer.ACT_RETCODE_CANCEL_BIO_AUTH)){
			if(resultCode == RESULT_OK){
				finishActivity(false,BiometricPrompt.ERROR_USER_CANCELED)
			}
			else{
				showAuthDialog()
			}
		}
	}

}