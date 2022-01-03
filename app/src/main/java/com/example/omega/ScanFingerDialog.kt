package com.example.omega

import android.Manifest
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

class ScanFingerDialog(context: Context) : Dialog(context) {

	init {
		setCancelable(false)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.dialog_scan_finger)
		setTitle("t")
	}

	fun checkIfFingerScanningIsPossible(context: Context){
		val isBiometricPromptEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
		val isSdkVersionSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
		val fingerprintManager = FingerprintManagerCompat.from(context)
		val isHardwareSupported = fingerprintManager.isHardwareDetected
		val atLeastOneFingerInDatabase = fingerprintManager.hasEnrolledFingerprints()
		val hasPerrmisions =  ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
	}


}