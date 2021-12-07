package com.example.omega

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.logging.Level.INFO


class NFCThread() : Service() {
	override fun onBind(p0: Intent?): IBinder? {return null}
	override fun onStart(intent: Intent?, startId: Int) {
		Log.i("Wookie","NFCThread created")

	}

	override fun onCreate() {
		Log.i("Wookie","NFCThread started")
		super.onCreate()
	}

	private fun askForNFCPermissions(activity: Activity){
		val permissionListener = object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport) {
				if (report.isAnyPermissionPermanentlyDenied or !report.areAllPermissionsGranted()) {
					val toastText = "Do płatności zbliżeniowych konieczne jest włączenie NFC"
					Utilites.showToast(activity,toastText)
				}
			}
			override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(activity).withPermissions(Manifest.permission.NFC).withListener(permissionListener).onSameThread().check()
	}
}

