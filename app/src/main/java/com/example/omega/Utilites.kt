package com.example.omega
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
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

		fun getAuthMethodeText(context: Context,methodeCode : Int) : String{
			val methodeName = when(methodeCode){
				0->context.getString(R.string.GUI_selectAuthMethodeText_pin)
				1->context.getString(R.string.GUI_selectAuthMethodeText_pattern)
				2->context.getString(R.string.GUI_selectAuthMethodeText_finger)
				else ->context.getString(R.string.GUI_selectAuthMethodeText_pin)
			}
			return methodeName
		}
		fun getCodeForAuthMethode(context: Context,methodeName : String) : Int{
			val methodeCode = when(methodeName){
				context.getString(R.string.GUI_selectAuthMethodeText_pin) -> 0
				context.getString(R.string.GUI_selectAuthMethodeText_pattern) -> 1
				context.getString(R.string.GUI_selectAuthMethodeText_finger) -> 2
				else -> 0
			}
			return methodeCode
		}

		fun authTransaction(context : Activity, description : String?, forcedMethodeCode : Int?){
			var preferredMethodeCode = readPref_Int(context, R.integer.preferedAuthMethode)
			if(forcedMethodeCode != null && forcedMethodeCode in 0..2)
				preferredMethodeCode = forcedMethodeCode
			val preferredMethodeName = getAuthMethodeText(context,preferredMethodeCode)
			when(preferredMethodeName){
				context.getString(R.string.GUI_selectAuthMethodeText_pin) ->
					authByPin(context,description)
				context.getString(R.string.GUI_selectAuthMethodeText_pattern) ->
					authByPattern(context,description)
				context.getString(R.string.GUI_selectAuthMethodeText_finger) ->
					authByFingerPrint(context,description)
			}
		}
		private fun authByPin(activity: Activity, description : String?){
			val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)
			val desFieldName = activity.resources.getString(R.string.ACT_COM_additionalDescriptionToAuthActivity)
			pinActivityActivityIntent.putExtra(desFieldName,description)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN)
			activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
		}
		private fun authByFingerPrint(activity: Activity, description : String?){
			val scanFingerActivityIntent = Intent(activity, ScanFingerActivity::class.java)
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_additionalDescriptionToAuthActivity)
			scanFingerActivityIntent.putExtra(descriptionFieldName,description)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_FINGER)
			activity.startActivityForResult(scanFingerActivityIntent, retCodeForActivity)
		}
		private fun authByPattern(activity: Activity, description : String?){
			//TODO
		}
	}
}