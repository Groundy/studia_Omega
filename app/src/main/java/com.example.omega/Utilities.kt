package com.example.omega
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random


class PreferencesOperator{
	companion object{
		fun clearPreferences(activity: Activity, vararg fields : Int){
			val preferencesFields = arrayOf(
				R.string.PREF_authURL,
				R.string.PREF_authCode,
				R.string.PREF_lastRandomValue,
				R.string.PREF_lastUsedPermissionsForAuth,
				R.string.PREF_authUrlValidityTimeEnd,
				R.string.PREF_listOfAccNumbersAccociatedWithToken)

				fields.forEach {
					if (preferencesFields.contains(it))
						savePref(activity, it, String())
				}
		}
		fun clearAuthData(activity: Activity){
			clearPreferences(activity,
				R.string.PREF_authURL,
				R.string.PREF_lastRandomValue,
				R.string.PREF_authCode,
				R.string.PREF_authUrlValidityTimeEnd,
				R.string.PREF_listOfAccNumbersAccociatedWithToken,
				R.string.PREF_lastUsedPermissionsForAuth
			)
		}

		fun DEVELOPER_showPref(activity: Activity){
			val preferencesFields = arrayOf(
				R.string.PREF_authURL,
				R.string.PREF_authCode,
				R.string.PREF_lastRandomValue,
				R.string.PREF_lastUsedPermissionsForAuth,
				R.string.PREF_authUrlValidityTimeEnd,
				R.string.PREF_listOfAccNumbersAccociatedWithToken)

			preferencesFields.forEach {
				val str = readPrefStr(activity, it)
				val t = activity.getString(it)
				val hg = "$t ---> $str"
				Log.i(Utilities.TagProduction, hg)
			}
		}

		private fun getSharedProperties(activity: Activity) : SharedPreferences{
			val fileName = activity.getString(R.string.preference_file_key)
			val sharedPrefObj = activity.getSharedPreferences(fileName, MODE_PRIVATE)
			return sharedPrefObj
		}

		fun savePref(activity: Activity, strResourceId : Int, value : Int){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putInt(fieldName,value)
			editor.apply()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : Boolean){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putBoolean(fieldName,value)
			editor.apply()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : Float){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putFloat(fieldName,value)
			editor.apply()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : String){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putString(fieldName,value)
			editor.apply()
		}

		fun readPrefBool(activity: Activity, strResourceId : Int) : Boolean{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getBoolean(fieldName,false)
		}
		fun readPrefInt(activity: Activity, strResourceId : Int) : Int{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getInt(fieldName,0)
		}
		fun readPrefStr(activity: Activity, strResourceId : Int) : String {
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getString(fieldName, "")!!
		}
		fun readPrefFloat(activity: Activity, strResourceId : Int) : Float{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getFloat(fieldName,0f)
		}


		fun encrypt(text : String, key : String) : String{
			//TODO
			return ""
		}
		fun decrypt(encryptedText : String, key : String) : String{
			//TODO

			return ""
		}
	}
}

class Utilities {
	companion object{
		const val TagProduction = "WookieTag"
		fun showMsg(activity: Activity, stringToDisplay:String) {
			val dialogBuilder = AlertDialog.Builder(activity)
			val dialogInterfaceVar = DialogInterface.OnClickListener { p0, _ -> p0.dismiss() }
			dialogBuilder.setMessage(stringToDisplay).setPositiveButton("Ok", dialogInterfaceVar)
			val dialog: AlertDialog = dialogBuilder.create()
			dialog.show()
		}
		fun showToast(activity: Activity, stringToDisplay:String){
			Toast.makeText(activity,stringToDisplay, Toast.LENGTH_LONG).show()
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
			val savedPinHash = PreferencesOperator.readPrefStr(activity, R.string.PREF_hashPin)
			return savedPinHash.isNotEmpty()
		}

		fun checkBlikCode(code : Int) : TransferData?{
			//TODO implement
			val properCode = 111111
			val isProperCode = code == properCode
			return if(isProperCode){
				val transferData = TransferData("0123456789012345678901234567","0001112223334445556667778889","Jan Kowalski","zwrot pożyczki" ,13.57 ,  "PLN")
				transferData
			}
			else
				null
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

		fun hashMd5(inputStr : String) : String{
			val md = MessageDigest.getInstance("MD5")
			return BigInteger(1, md.digest(inputStr.toByteArray())).toString(16).padStart(32, '0')
		}
	}
}