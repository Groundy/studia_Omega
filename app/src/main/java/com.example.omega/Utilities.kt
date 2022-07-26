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
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random

class Utilities {
	companion object{
		const val developerMode = true
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
		fun authSuccessed(context: Context){
			//TODO implement
			showToast(context as Activity, "Auth success!")
		}
		fun authFailed(context: Context){
			//TODO implement
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