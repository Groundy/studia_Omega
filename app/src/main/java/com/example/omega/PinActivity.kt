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
	lateinit var digit1 : EditText
	lateinit var digit2 : EditText
	lateinit var digit3 : EditText
	lateinit var digit4 : EditText
	lateinit var digit5 : EditText

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
							digit1.text.clear()
						}
						R.id.pid_digit2 ->{
							digit1.requestFocus()
							digit1.text.clear()
							digit2.text.clear()
						}
						R.id.pid_digit3 ->{
							digit2.requestFocus()
							digit2.text.clear()
							digit3.text.clear()
						}
						R.id.pid_digit4 ->{
							digit3.requestFocus()
							digit3.text.clear()
							digit4.text.clear()
						}
						R.id.pid_digit5 ->{
							val userAlreadyInsertedLastDigit = digit5.text.length == 1
							if(!userAlreadyInsertedLastDigit){
								digit4.requestFocus()
								digit4.text.clear()
								digit5.text.clear()
							}
							else
								digit5.text.clear()
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
					digit2.requestFocus()
				}
			}
		}
		val listener2 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digit3.requestFocus()
				}
			}
		}
		val listener3 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digit4.requestFocus()
				}
			}
		}
		val listener4 = object : TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(str: Editable?) {
				val enteredDigit = str.toString().toIntOrNull()
				if(enteredDigit != null){
					digit5.requestFocus()
				}
			}
		}

		digit1.addTextChangedListener(listener1)
		digit2.addTextChangedListener(listener2)
		digit3.addTextChangedListener(listener3)
		digit4.addTextChangedListener(listener4)
		digit1.setOnEditorActionListener(onEnterKeyPressedListener)
		digit2.setOnEditorActionListener(onEnterKeyPressedListener)
		digit3.setOnEditorActionListener(onEnterKeyPressedListener)
		digit4.setOnEditorActionListener(onEnterKeyPressedListener)
		digit5.setOnEditorActionListener(onEnterKeyPressedListener)
		digit1.setOnKeyListener(deleteButtonPressedListener)
		digit2.setOnKeyListener(deleteButtonPressedListener)
		digit3.setOnKeyListener(deleteButtonPressedListener)
		digit4.setOnKeyListener(deleteButtonPressedListener)
		digit5.setOnKeyListener(deleteButtonPressedListener)

	}
	private fun checkIfAllFieldsHaveEnteredDigits(): Boolean {
		val value1 = digit1.text.toString().toIntOrNull()
		val value2 = digit2.text.toString().toIntOrNull()
		val value3 = digit3.text.toString().toIntOrNull()
		val value4 = digit4.text.toString().toIntOrNull()
		val value5 = digit5.text.toString().toIntOrNull()
		val ok1 = value1 != null && value1 in 0..9
		val ok2 = value2 != null && value2 in 0..9
		val ok3 = value3 != null && value3 in 0..9
		val ok4 = value4 != null && value4 in 0..9
		val ok5 = value5 != null && value5 in 0..9
		return ok1 && ok2 && ok3 && ok4 && ok5
	}
	private fun getPinFromFields() : Int?{
		val allFieldsAreFilled = checkIfAllFieldsHaveEnteredDigits()
		if(!allFieldsAreFilled)
			return null
		val value1 = digit1.text.toString().toInt() * 10000
		val value2 = digit2.text.toString().toInt() * 1000
		val value3 = digit3.text.toString().toInt() * 100
		val value4 = digit4.text.toString().toInt() * 10
		val value5 = digit5.text.toString().toInt() * 1
		val pin = value1 + value2 + value3 + value4 + value5
		return pin
	}
	private fun processPIN(){
		val pin = getPinFromFields()
		if (pin != null) {
			val authCorrect = compareInsertedPin(pin)
			if(authCorrect){
				val output = Intent()
				val fieldName = resources.getString(R.string.PIN_RESULT_FIELD)
				output.putExtra(fieldName, authCorrect)
				setResult(RESULT_OK, output)
				finish()
			}
			else{
				//TODO obsługa sytuacji w ktorej do pinu wprowadzono wszystkie cyfry ale nie jest on poprawny
			}
		}
		Utilites.showToast(this,pin.toString())
	}
	private fun getAdditionalDescription() : String?{
		val description = this.intent.getStringExtra(getString(R.string.additionalDescriptionToAuthActivity))
		return description
	}
	private fun requestFocusOnActivityStart(){
		digit1.requestFocus()
		val showKeyboardObj = Runnable {
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(digit1, InputMethodManager.SHOW_FORCED)
		}
		digit1.postDelayed(showKeyboardObj, 150)
	}
	private fun findElements(){
		digit1 = findViewById<EditText>(R.id.pid_digit1)
		digit2 = findViewById<EditText>(R.id.pid_digit2)
		digit3 = findViewById<EditText>(R.id.pid_digit3)
		digit4 = findViewById<EditText>(R.id.pid_digit4)
		digit5 = findViewById<EditText>(R.id.pid_digit5)
	}
	private fun compareInsertedPin(pin : Int) : Boolean{
		//TODO Dodać sprawdzanie czy pin jest OK
		return true
	}
}