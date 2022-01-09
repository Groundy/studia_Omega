package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager


class PinActivity : AppCompatActivity() {
	private var digits : MutableList<EditText> = arrayListOf()
	private var pinTriesLeft = 3

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pin)
		findViewById<TextView>(R.id.descripitonOnPinAct).text = getAdditionalDescription()
		findElements()
		setUIElementsListeners()
		requestFocusOnActivityStart()
	}

	private fun setUIElementsListeners(){
		val onEnterKeyPressedListener = object : TextView.OnEditorActionListener {
			//zwracana wartosc oznacza czy zamknac klawiature
			override fun onEditorAction(field: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
				//val keyPressed = keyEvent?.action == KeyEvent.ACTION_DOWN
				val pressedKeyIsEnter = actionId === EditorInfo.IME_ACTION_DONE
				if (pressedKeyIsEnter) {
					val everyDigitIsOk = checkIfAllFieldsHaveEnteredDigits()
					if(everyDigitIsOk){
						Log.i("WookieTag","Pressed enter in PIN activity, pin is in CORRECT format")
						processPIN()
						return false
					}
					else{
						Log.e("WookieTag","Pressed enter in PIN activity, pin is in WRONG format")
						return true
					}
				}
				else
					Log.i("WookieTag","Pressed not enter key in PIN activity")
				return true
			}
		}
		val deleteButtonPressedListener = object : View.OnKeyListener{
			override fun onKey(source: View?, keyCode: Int, event: KeyEvent): Boolean {
				if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN) {
					when(source?.id){
						R.id.pid_digit1 ->{
							digits[0].text.clear()
						}
						R.id.pid_digit2 ->{
							digits[0].requestFocus()
							digits[0].text.clear()
							digits[1].text.clear()
						}
						R.id.pid_digit3 ->{
							digits[1].requestFocus()
							digits[1].text.clear()
							digits[2].text.clear()
						}
						R.id.pid_digit4 ->{
							digits[2].requestFocus()
							digits[2].text.clear()
							digits[3].text.clear()
						}
						R.id.pid_digit5 ->{
							val userAlreadyInsertedLastDigit = digits[4].text.length == 1
							if(!userAlreadyInsertedLastDigit){
								digits[3].requestFocus()
								digits[3].text.clear()
								digits[4].text.clear()
							}
							else
								digits[4].text.clear()
						}
						else ->{

						}
					}
					return true
				}
				return false
			}
		}
		val listener1 = object : TextWatcher {
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digits[1].requestFocus()
				}
			}
		}
		val listener2 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digits[2].requestFocus()
				}
			}
		}
		val listener3 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digits[3].requestFocus()
				}
			}
		}
		val listener4 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digits[4].requestFocus()
				}
			}
		}

		digits[0].addTextChangedListener(listener1)
		digits[1].addTextChangedListener(listener2)
		digits[2].addTextChangedListener(listener3)
		digits[3].addTextChangedListener(listener4)

		digits.forEach {
			it.setOnEditorActionListener(onEnterKeyPressedListener)
			it.setOnKeyListener(deleteButtonPressedListener)
		}
	}
	private fun checkIfAllFieldsHaveEnteredDigits(): Boolean {
		val value1 = digits[0].text.toString().toIntOrNull()
		val value2 = digits[1].text.toString().toIntOrNull()
		val value3 = digits[2].text.toString().toIntOrNull()
		val value4 = digits[3].text.toString().toIntOrNull()
		val value5 = digits[4].text.toString().toIntOrNull()
		val ok1 = value1 != null && value1 in 0..9
		val ok2 = value2 != null && value2 in 0..9
		val ok3 = value3 != null && value3 in 0..9
		val ok4 = value4 != null && value4 in 0..9
		val ok5 = value5 != null && value5 in 0..9
		return ok1 && ok2 && ok3 && ok4 && ok5
	}
	private fun getPinFromFields(): Int? {
		val allFieldsAreFilled = checkIfAllFieldsHaveEnteredDigits()
		if (!allFieldsAreFilled)
			return null
		val value1 = digits[0].text.toString().toInt() * 10000
		val value2 = digits[1].text.toString().toInt() * 1000
		val value3 = digits[2].text.toString().toInt() * 100
		val value4 = digits[3].text.toString().toInt() * 10
		val value5 = digits[4].text.toString().toInt() * 1
		return value1 + value2 + value3 + value4 + value5
	}
	private fun processPIN(){
		val pin = getPinFromFields()
		if (pin != null) {
			val authCorrect = compareInsertedPin(pin)
			if(authCorrect){
				val output = Intent()
				val fieldName = resources.getString(R.string.ACT_COM_PIN_FIELD_NAME)
				output.putExtra(fieldName, authCorrect)
				setResult(RESULT_OK, output)
				finish()
			}
			else{
				pinTriesLeft--
				val allowUserToTryOtherPin = pinTriesLeft > 0
				if(allowUserToTryOtherPin){
					val textToShow = when(pinTriesLeft){
						2 -> resources.getString(R.string.USER_MSG_2_TRIES_LEFT)
						1 -> resources.getString(R.string.USER_MSG_LAST_TRY_LEFT)
						else -> resources.getString(R.string.USER_MSG_UNKNOWN_ERROR)
					}
					Utilites.showToast(this,textToShow)
					digits.forEach { it.text.clear() }
					digits[0].requestFocus()
				}
				else{
					val output = Intent()
					val fieldName = resources.getString(R.string.ACT_COM_PIN_FIELD_NAME)
					output.putExtra(fieldName, false)
					setResult(RESULT_CANCELED, output)
					finish()
				}
			}
		}
	}
	private fun getAdditionalDescription(): String? {
		return intent.getStringExtra(getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME))
	}
	private fun requestFocusOnActivityStart(){
		digits[0].requestFocus()
		val showKeyboardObj = Runnable {
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(digits[0], InputMethodManager.SHOW_FORCED)
		}
		digits[0].postDelayed(showKeyboardObj, 150)
	}
	private fun findElements(){
		digits.add(findViewById<EditText>(R.id.pid_digit1))
		digits.add(findViewById<EditText>(R.id.pid_digit2))
		digits.add(findViewById<EditText>(R.id.pid_digit3))
		digits.add(findViewById<EditText>(R.id.pid_digit4))
		digits.add(findViewById<EditText>(R.id.pid_digit5))
	}
	private fun compareInsertedPin(pin : Int) : Boolean{
		//TODO Dodać sprawdzanie czy pin jest OK
		return pin == 12345
	}
}