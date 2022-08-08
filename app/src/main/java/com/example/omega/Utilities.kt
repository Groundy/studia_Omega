package com.example.omega
import android.app.Activity
import android.content.DialogInterface
import android.text.Editable
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
		fun checkIfAppHasAlreadySetPin(activity: Activity): Boolean {
			val savedPinHash = PreferencesOperator.readPrefStr(activity, R.string.PREF_hashPin)
			return savedPinHash.isNotEmpty()
		}

		fun checkBlikCode(code : Int) : TransferData?{
			//TODO implement
			val properCode = 111111
			val isProperCode = code == properCode
			if(!isProperCode)
				return null

			return wookieTestGetTestObjWithFilledData()
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
		fun wookieTestGetRandomTestCode() : Int{
			return Random.nextInt(999999)
		}
		fun hashMd5(inputStr : String) : String{
			val md = MessageDigest.getInstance("MD5")
			return BigInteger(1, md.digest(inputStr.toByteArray())).toString(16).padStart(32, '0')
		}
		fun strToEditable(text: String): Editable {
			return Editable.Factory.getInstance().newEditable(text)
		}
		fun wookieTestGetTestObjWithFilledData() : TransferData{
			val testTransferData = TransferData().also {
				it.receiverAccNumber = "PL63249000050000400030900682"
				it.receiverName = "Regina Aff"
				it.senderAccNumber = "PL50249000050000400076134538"
				it.senderAccName = "Rwfaw Dawfawf"
				it.amount = 12.34
				it.description = "zwrot pozyczki"
				it.currency = "EUR"
				it.executionDate = OmegaTime.getDate()
			}
			return testTransferData
		}
		fun wookieTestGetTestPaymentAccountForPaymentAct(): PaymentAccount {
			return PaymentAccount().also {
				it.accountNumber = "0123456789012345678901234567"
				it.ownerName = "Ania Kowalska"
				it.accountDescription = "Bank milion procent"
				it.currency = "PLN"
				it.availableBalance = 1234567.83

			}
		}
		fun doubleToTwoDigitsAfterCommaString(value : Double?) : String{
			if(value == null){
				Log.e(TagProduction, "[doubleToTwoDigitsAfterCommaString/${this.javaClass.name}] Wrong struct passed for payment request, amount is null")
				return String()
			}
			var amountStr = value.toString()
			if(!amountStr.contains('.')){
				Log.e(TagProduction, "[doubleToTwoDigitsAfterCommaString/${this.javaClass.name}] Wrong struct passed for payment request, amount doesnt have dot sign")
				return amountStr
			}

			val theresOnlyOneDigitAfterDot = amountStr.indexOf('.') == amountStr.length -2
			if(theresOnlyOneDigitAfterDot)
				amountStr = amountStr.plus("0")
			return amountStr
		}
	}
}