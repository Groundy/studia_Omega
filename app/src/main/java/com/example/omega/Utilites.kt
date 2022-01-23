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
	val sharedPref = activity.getSharedPreferences(fileName,MODE_PRIVATE)
	return sharedPref
}

class Utilites {
	companion object{
		fun showMsg(activity: Activity, stringToDisplay:String) {
			val dialogBuilder = AlertDialog.Builder(activity)
			val dialogInterfaceVar = DialogInterface.OnClickListener { p0, p1 -> p0.dismiss() }
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
			return prefs.getBoolean(fieldName,false)
		}
		fun readPref_Int(activity: Activity, strResourceId : Int) : Int{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getInt(fieldName,0)
		}
		fun readPref_Str(activity: Activity, strResourceId : Int) : String {
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getString(fieldName, "")!!
		}
		fun readPref_Float(activity: Activity, strResourceId : Int) : Float{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getFloat(fieldName,0f)
		}

		fun authTransaction(context : Activity, description : String?, forcedMethodeCode : Int?){
			var preferredMethodeCode = readPref_Int(context, R.integer.PREF_preferedAuthMethode)
			if(forcedMethodeCode != null && forcedMethodeCode in 0..2)
				preferredMethodeCode = forcedMethodeCode
			val preferredMethodeName = when(preferredMethodeCode){
				0->context.getString(R.string.GUI_selectAuthMethodeText_pin)
				1->context.getString(R.string.GUI_selectAuthMethodeText_pattern)
				2->context.getString(R.string.GUI_selectAuthMethodeText_finger)
				else ->context.getString(R.string.GUI_selectAuthMethodeText_pin)
			}
			when(preferredMethodeName){
				context.getString(R.string.GUI_selectAuthMethodeText_pin) -> authByPin(context,description)
				context.getString(R.string.GUI_selectAuthMethodeText_pattern) -> authByPattern(context,description)
				context.getString(R.string.GUI_selectAuthMethodeText_finger) -> authByFingerPrint(context,description)
			}
		}
		private fun authByPin(activity: Activity, description : String?){
			val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val pinActPurpose = activity.resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[1]
			val pinActPurposeFieldName = activity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
			pinActivityActivityIntent.putExtra(descriptionFieldName,description)
			pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)
			activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
		}
		private fun authByFingerPrint(activity: Activity, description : String?){
			val scanFingerActivityIntent = Intent(activity, ScanFingerActivity::class.java)
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			scanFingerActivityIntent.putExtra(descriptionFieldName,description)
			val retCodeForActivity  = activity.resources.getInteger(R.integer.ACT_RETCODE_FINGER)
			activity.startActivityForResult(scanFingerActivityIntent, retCodeForActivity)
		}
		private fun authByPattern(activity: Activity, description : String?){
			//TODO
		}
		fun authSuccessed(context: Context){
			//TODO
			showToast(context as Activity, "Auth success!")
		}
		fun authFailed(context: Context){
			//TODO
			showToast(context as Activity, "Auth failed!")
		}
		fun checkIfAppHasAlreadySetPin(activity: Activity): Boolean {
			//TODO tymczasowo PIN jest zapisywany w pamięci telefonu w plain text, należy to koniecznie zmienić
			val pinRead = readPref_Int(activity, R.integer.PREF_pin)
			return pinRead in 1..99999
		}
		fun getMessageToDisplayToUserAfterBiometricAuthError(errCode : Int) : String{
			return when(errCode){
				//0 -> "Uzyskano autoryzację!"
				1 -> "Sensor jest chwilowo niedostępny, należy spróbować później."
				2 -> "Czujnik nie był w stanie przetworzyć odcisku palca."
				3 -> "Nie wykryto palca przez 30s."
				4 -> "Urządzenie nie ma wystarczającej ilości miejsca żeby wykonać operacje."
				5,10 -> "Użytkownik anulował uwierzytelnianie za pomocą biometrii."
				7 -> "Pięciorkotnie nierozpoznano odcisku palca, sensor będzie dostępny ponownie za 30s."
				9 -> "Sensor jest zablokowany, należy go odblokować wporwadzająć wzór/pin telefonu."
				11 -> "Nieznany błąd, upewnij się czy w twoim urządzeniu jest zapisany odcis palca."
				12 -> "Urządzenie nie posiada odpowiedniego sensora."
				14 -> "Urządzenie musi posiadać pin,wzór lub hasło."
				15 -> "Operacja nie może zostać wykonana bez aktualizacji systemu."
				else ->"Operacja zakończona niepowodzeniem z nieznanego powodu."
			}
		}
	}
}