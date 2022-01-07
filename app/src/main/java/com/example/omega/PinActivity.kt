package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
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
		digit1 = findViewById<EditText>(R.id.pid_digit1)
		digit2 = findViewById<EditText>(R.id.pid_digit2)
		digit3 = findViewById<EditText>(R.id.pid_digit3)
		digit4 = findViewById<EditText>(R.id.pid_digit4)
		digit5 = findViewById<EditText>(R.id.pid_digit5)
		setUIElementsListeners()

		digit1.requestFocus()
		val showKeyBoardObj = Runnable {
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(digit1, InputMethodManager.SHOW_FORCED)
		}
		digit1.postDelayed(showKeyBoardObj, 100)
	}

	private fun setUIElementsListeners(){
		val onEnterKeyPressedListener = object : TextView.OnEditorActionListener {
			override fun onEditorAction(field: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
				//val keyPressed = keyEvent?.action == KeyEvent.ACTION_DOWN
				val pressedKeyIsEnter = actionId === EditorInfo.IME_ACTION_DONE
				if (pressedKeyIsEnter) {
					val everyDigitIsOk = checkIfAllFieldsHaveEnteredDigits()
					if(everyDigitIsOk)
						Log.i("WookieTag","Pressed enter in PIN activity, pin is in CORRECT format")
					else
						Log.e("WookieTag","Pressed enter in PIN activity, pin is in WRONG format")
				}
				else
					Log.i("WookieTag","Pressed not enter key in PIN activity")
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
}