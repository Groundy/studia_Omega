package com.example.omega

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
	private enum class PURPOSE{SET, CHANGE, AUTH}
	private enum class CHANGE_PIN_PROCESS_PHASES{OLD_PIN, NEW_PIN, NEW_PIN_AGAIN}

	private var puprose : PURPOSE = PURPOSE.AUTH//tmp
	private var digits : MutableList<EditText> = arrayListOf()
	private var pinTriesLeft = 3
	private var tmpPIN : Int = 0
	private var phaseOfChangePinProcess = CHANGE_PIN_PROCESS_PHASES.OLD_PIN
	private lateinit var descriptionField : TextView
	private lateinit var titleField : TextView
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pin)
		findElements()
		checkStartPurpose()
		getProperTextsForGUIElements()
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
						Log.i(Utilites.TagProduction,"Pressed enter in PIN activity, pin is in CORRECT format")
						processPIN()
						return false
					}
					else{
						Log.e(Utilites.TagProduction,"Pressed enter in PIN activity, pin is in WRONG format")
						return true
					}
				}
				else
					Log.i(Utilites.TagProduction,"Pressed not enter key in PIN activity")
				return true
			}
		}
		val deleteButtonPressedListener = object : View.OnKeyListener{
			override fun onKey(source: View?, keyCode: Int, event: KeyEvent): Boolean {
				if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
					when(source?.id){
						R.id.PIN_digit1_TextView ->{
							digits[0].text.clear()
						}
						R.id.PIN_digit2_TextView ->{
							digits[0].requestFocus()
							digits[0].text.clear()
							digits[1].text.clear()
						}
						R.id.PIN_digit3_TextView ->{
							digits[1].requestFocus()
							digits[1].text.clear()
							digits[2].text.clear()
						}
						R.id.PIN_digit4_TextView ->{
							digits[2].requestFocus()
							digits[2].text.clear()
							digits[3].text.clear()
						}
						R.id.PIN_digit5_TextView ->{
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
	private fun getProperTextsForGUIElements(){
		when(puprose){
			PURPOSE.AUTH -> {
				titleField.text = resources.getString(R.string.GUI_authTransactionTitle)
				descriptionField.text = intent.getStringExtra(getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME))
			}
			PURPOSE.SET -> {
				descriptionField.text = null
				titleField.text = resources.getString(R.string.GUI_PIN_setPinTitle)
			}
			PURPOSE.CHANGE -> {
				descriptionField.text = resources.getString(R.string.GUI_PIN_changeDescription_old)
				titleField.text = resources.getString(R.string.GUI_PIN_changeTitle)
			}
		}
		descriptionField.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
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
		if(pin != null){
			when(puprose){
				PURPOSE.AUTH -> processAuth(pin)
				PURPOSE.SET -> processSet(pin)
				PURPOSE.CHANGE -> processChange(pin)
			}
		}
		else
			Utilites.showToast(this,resources.getString(R.string.USER_MSG_PIN_HAVE_TO_USE_ALL_DIGITS))
	}
	private fun requestFocusOnActivityStart(){
		digits[0].requestFocus()
		val showKeyboardObj = Runnable {
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(digits[0], InputMethodManager.SHOW_FORCED)
		}
		digits[0].postDelayed(showKeyboardObj, 250)
	}
	private fun findElements(){
		digits.add(findViewById<EditText>(R.id.PIN_digit1_TextView))
		digits.add(findViewById<EditText>(R.id.PIN_digit2_TextView))
		digits.add(findViewById<EditText>(R.id.PIN_digit3_TextView))
		digits.add(findViewById<EditText>(R.id.PIN_digit4_TextView))
		digits.add(findViewById<EditText>(R.id.PIN_digit5_TextView))
		descriptionField = findViewById<TextView>(R.id.PIN_Description_TextView)
		titleField = findViewById<TextView>(R.id.PIN_Title_TextView)
	}
	private fun checkIfPinIsCorrect(pin : Int) : Boolean{
		//TODO Dodać sprawdzanie czy pin jest OK
		val pinSavedInPrefs = Utilites.readPref_Int(this, R.integer.PREF_pin)
		return pin == pinSavedInPrefs
	}
	private fun processAuth(pin : Int){
		val authCorrect = checkIfPinIsCorrect(pin)
		if(authCorrect)
			finishActivity(true)
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
				clearDigitsFields()
			}
			else
				finishActivity(false)
		}
	}
	private fun processChange(pin : Int){
		when(phaseOfChangePinProcess){
			CHANGE_PIN_PROCESS_PHASES.OLD_PIN ->{
				descriptionField.text = resources.getString(R.string.GUI_PIN_changeDescription_old)
				clearDigitsFields()
				val properOldPin = checkIfPinIsCorrect(pin)
				if(properOldPin) {
					phaseOfChangePinProcess = CHANGE_PIN_PROCESS_PHASES.NEW_PIN
					descriptionField.text =	resources.getString(R.string.GUI_PIN_changeDescription_new)
				}
				else{
					pinTriesLeft--
					when(pinTriesLeft){
						2 -> {
							Utilites.showToast(this, resources.getString(R.string.USER_MSG_2_TRIES_LEFT))
							return
						}
						1 -> {
							Utilites.showToast(this, resources.getString(R.string.USER_MSG_LAST_TRY_LEFT))
							return
						}
						else -> {
							Utilites.showToast(this, resources.getString(R.string.USER_MSG_FAILED_TO_SET_NEW_PIN))
							finishActivity(false)
						}
					}
				}
			}
			CHANGE_PIN_PROCESS_PHASES.NEW_PIN ->{
				descriptionField.text = resources.getString(R.string.GUI_PIN_changeDescription_new)
				tmpPIN = pin
				phaseOfChangePinProcess = CHANGE_PIN_PROCESS_PHASES.NEW_PIN_AGAIN
			}
			CHANGE_PIN_PROCESS_PHASES.NEW_PIN_AGAIN->{
				descriptionField.text = resources.getString(R.string.GUI_PIN_changeDescription_newAgain)
				val twoPinsAreSame = pin == tmpPIN
				if(twoPinsAreSame){
					Utilites.showToast(this, resources.getString(R.string.USER_MSG_SUCESS_IN_SETTING_NEW_PIN))
					saveNewPinInMemory(pin)
					finishActivity(true)
				}
				else{
					Utilites.showToast(this, resources.getString(R.string.USER_MSG_FAILED_TO_SET_NEW_PIN))
					finishActivity(false)
				}
			}
		}
	}
	private fun processSet(pin: Int) {
		val itsFirstAttemptToSetPin = tmpPIN == 0
		if(itsFirstAttemptToSetPin){
			descriptionField.text = resources.getString(R.string.GUI_PIN_setPinAgainTitle)
			clearDigitsFields()
			tmpPIN = pin
		}
		else{
			val bothPinsAreSame = pin == tmpPIN
			if(bothPinsAreSame)
				saveNewPinInMemory(pin)
			finishActivity(bothPinsAreSame)
		}
	}
	private fun saveNewPinInMemory(pin : Int){
		//TODO
		Utilites.savePref(this,R.integer.PREF_pin,pin)
		//dodać funkcję która ustawi ten PIN w pamięci apki i na severze
	}
	private fun checkStartPurpose(){
		val purposeFieldName = resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
		val activityStartReasonStr = intent.getStringExtra(purposeFieldName)
		when(activityStartReasonStr){
			resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[0] -> this.puprose = PURPOSE.SET
			resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[1] -> this.puprose = PURPOSE.AUTH
			resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[2] -> this.puprose = PURPOSE.CHANGE
			else -> this.puprose = PURPOSE.AUTH
		}
	}
	private fun clearDigitsFields(){
		digits.forEach { it.text.clear() }
		digits[0].requestFocus()
	}
	private fun finishActivity(success: Boolean){
		val retCode = if(success) RESULT_OK else RESULT_CANCELED
		setResult(retCode)
		finish()
	}
}
