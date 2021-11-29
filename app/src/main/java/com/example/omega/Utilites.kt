package com.example.omega

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class Utilites {
	companion object{
		fun showMsg(activity: Activity, stringToDisplay:String) {
			val dialogBuilder = AlertDialog.Builder(activity)
			val dialogInterfaceVar = object : DialogInterface.OnClickListener {
				override fun onClick(p0: DialogInterface, p1: Int) {
					p0.dismiss()
				}
			}
			dialogBuilder.setMessage(stringToDisplay).setPositiveButton("Ok", dialogInterfaceVar)
			val dialog: AlertDialog = dialogBuilder.create()
			dialog.show()
		}
	}
}