package com.example.omega

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.lang.Thread.sleep


class NFCThread() : Service() {
	override fun onBind(p0: Intent?): IBinder? {return null}
	override fun onStart(intent: Intent?, startId: Int) {
		Log.i("WookieTag","NFCThread created")
		val intent1 = Intent()
		intent1.action = "NFCThread"
		val thread = Thread(){
			Log.i("WookieTag","NFC Thread is looking for tag")
			while(true)
				sleep(50)//TODO usunac to, tylko do symulacji dlugo dzialajacego procesu
			endService(3,intent1)
		}
		thread.start()
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
		Log.i("WookieTag","NFC Thread returned code " + code.toString())
		intent.putExtra("code", code.toString())
		sendBroadcast(intent)
		sleep(100)
	}
}
