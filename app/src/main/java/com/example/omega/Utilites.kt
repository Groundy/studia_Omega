package com.example.omega
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlin.random.Random

fun getSharedProperties(activity: Activity) : SharedPreferences{
	val fileName = activity.getString(R.string.preference_file_key)
	val sharedPref = activity.getSharedPreferences(fileName,MODE_PRIVATE)
	return sharedPref
}

class Utilites {
	companion object{
		val TagProduction = "WookieTag"
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
		fun authByPattern(activity: Activity, description : String?){
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
		fun checkBlikCode(code : Int) : TransferData?{
			//TODO implement
			val properCode = 111111
			val isProperCode = code == properCode
			if(isProperCode){
				val transferData = TransferData("0123456789012345678901234567","0001112223334445556667778889","Jan Kowalski","zwrot pożyczki" ,13.57 ,  "PLN")
				return transferData
			}
			else
				return null
		}
		fun stopUserFromPuttingMoreThan2DigitsAfterComma(editText : EditText, oldVal : String, newVal : String){
			val indexOfDecimal = newVal.indexOf('.')
			if(indexOfDecimal != -1){
				val digitsAfterComma = (newVal.length - 1) - indexOfDecimal
				if(digitsAfterComma > 2){
					editText.text = SpannableStringBuilder(oldVal)
					editText.setSelection(editText.length())//Setting cursor to end
				}
			}
		}
		fun getRandomTestCode() : Int{
			return Random.nextInt(999999)
		}
	}
}