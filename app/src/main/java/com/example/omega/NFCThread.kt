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
import java.lang.Thread.sleep


class NFCThread() : Service() {
	override fun onBind(p0: Intent?): IBinder? {return null}
	override fun onStart(intent: Intent?, startId: Int) {
		Log.i("Wookie","NFCThread created")
		val intent1 = Intent()
		intent1.action = "NFCThread"
	}
	override fun onCreate() {
		Log.i("Wookie","NFCThread started")
		super.onCreate()
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.i("Wookie","NFCThread destroyed")
	}
	fun endService(code : Int,intent: Intent){
		intent.putExtra("code", code.toString())
		sendBroadcast(intent)
		sleep(10)
		stopService(intent)
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


/*
var nfcAdapter : NfcAdapter = NfcAdapter.getDefaultAdapter(this)

private fun checkIfDeviceSupportNFC(): Boolean {
	var toRet = true
	 if(nfcAdapter==null){
		Toast.makeText(this, "That device doesn't support NFC ", Toast.LENGTH_LONG)
		toRet = false
	}
	return toRet
}

fun askForNFCPermissions(){
	val permissionArray= Array(1){ Manifest.permission.NFC}.toMutableList()
	val permissionListener=object : MultiplePermissionsListener {
		override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

		}

		override fun onPermissionRationaleShouldBeShown(
			permissions: MutableList<PermissionRequest>?,
			token: PermissionToken?
		) {
			Toast.makeText(this@MainActivity, "Accept permissions first!", Toast.LENGTH_LONG)
		}
	}
	Dexter.withActivity(this).withPermissions(permissionArray).withListener(permissionListener).check()

}
fun checkIfNfcIsEnabled():Boolean{
	return if(nfcAdapter.isEnabled)
		true
	else{
		Toast.makeText(this,"Turn on NFC", Toast.LENGTH_LONG)
		false
	}
}
fun startNfcConnection(){
	askForNFCPermissions()
	nfcAdapter = NfcAdapter.getDefaultAdapter(this)
	//checkIfDeviceSupportNFC()
}
*/




