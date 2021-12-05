package com.example.omega
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun getSharedProperties(activity: Activity) : SharedPreferences{
	val fileName = activity.getString(R.string.preference_file_key)
	val sharedPref = activity?.getSharedPreferences(fileName,MODE_PRIVATE)
	return sharedPref
}

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
		fun showToast(activity: Activity, stringToDisplay:String){
			Toast.makeText(activity,stringToDisplay, Toast.LENGTH_LONG).show()
		}

		fun savePref(activity: Activity, strResourceId : Int, value : Int){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putInt(fieldName,value)
			editor.commit()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : Boolean){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putBoolean(fieldName,value)
			editor.commit()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : Float){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putFloat(fieldName,value)
			editor.commit()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : String){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putString(fieldName,value)
			editor.commit()
		}

		fun readPref_Bool(activity: Activity, strResourceId : Int) : Boolean{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			var toRet = prefs.getBoolean(fieldName,false)
			return toRet
		}
		fun readPref_Int(activity: Activity, strResourceId : Int) : Int{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			var toRet = prefs.getInt(fieldName,0)
			return toRet
		}
		fun readPref_Str(activity: Activity, strResourceId : Int) : String {
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			val toRet =  prefs.getString(fieldName, "")!!
			return toRet
		}
		fun readPref_Float(activity: Activity, strResourceId : Int) : Float{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			var toRet = prefs.getFloat(fieldName,0f)
			return toRet
		}
	}
}