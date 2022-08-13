package com.example.omega

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class WaitingDialog {
	private var dialog : AlertDialog

	constructor(callerActivity: Activity, resId : Int = R.string.POPUP_empty){
		val infalter = callerActivity.layoutInflater
		val dialogView = infalter.inflate(R.layout.loading_dialog,null)
		val bulider = AlertDialog.Builder(callerActivity)
		bulider.setView(dialogView)
		bulider.setCancelable(false)
		dialog = bulider.create()
		val str = callerActivity.resources.getString(resId)
		CoroutineScope(Main).launch {
			val textView = dialog.findViewById<TextView>(R.id.WaitingDial_textView)
			textView.text = str
		}
		dialog.show()
	}
	constructor(callerActivity: Activity, str : String){
		val infalter = callerActivity.layoutInflater
		val dialogView = infalter.inflate(R.layout.loading_dialog,null)
		val bulider = AlertDialog.Builder(callerActivity)
		bulider.setView(dialogView)
		bulider.setCancelable(false)
		dialog = bulider.create()
		CoroutineScope(Main).launch {
			val textView = dialog.findViewById<TextView>(R.id.WaitingDial_textView)
			textView.text = str
		}
		dialog.show()
	}
	fun changeText(callerActivity: Activity, resId : Int){
		val str = callerActivity.resources.getString(resId)
		CoroutineScope(Main).launch {
			val textView = dialog.findViewById<TextView>(R.id.WaitingDial_textView)
			textView.text = str
		}
	}
	fun hide(){
		dialog.dismiss()
	}
}