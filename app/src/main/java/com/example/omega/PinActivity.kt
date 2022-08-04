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
import androidx.core.view.isVisible
import com.example.omega.Utilities.Companion.TagProduction

class PinActivity : AppCompatActivity() {
	companion object{
		enum class Purpose(val text : String){
			Set("Set"), Change("Change"), Auth("Auth");
			companion object {
				fun fromString(text : String) : Purpose{
					return when(text){
						Set.text -> Set
						Change.text -> Change
						Auth.text -> Auth
						else -> {
							Log.e(TagProduction, "[fromString/Purpose/PinActivity]  failed to convert Purpose str to purose enum str=$text")
							Auth
						}
					}
				}
			}
		}
		private enum class ChangePinProcessPhases{OLD_PIN, NEW_PIN, NEW_PIN_AGAIN}
	}

	private lateinit var puprose : Purpose
	private var digits : MutableList<EditText> = arrayListOf()
	private var pinTriesLeft = 3
	private var tmpPIN : Int = 0
	private var phaseOfChangePinProcess = ChangePinProcessPhases.OLD_PIN
	private lateinit var descriptionField : TextView
	private lateinit var titleField : TextView
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pin)
		checkStartPurpose()
		setGuiElements()
		getProperTextsForGuiElements()
		requestFocusOnActivityStart()
	}
	private fun setGuiElements(){
		digits.add(findViewById(R.id.PIN_digit1_TextView))
		digits.add(findViewById(R.id.PIN_digit2_TextView))
		digits.add(findViewById(R.id.PIN_digit3_TextView))
		digits.add(findViewById(R.id.PIN_digit4_TextView))
		digits.add(findViewById(R.id.PIN_digit5_TextView))
		descriptionField = findViewById(R.id.PIN_Description_TextView)
		titleField = findViewById(R.id.PIN_Title_TextView)

		val onEnterKeyPressedListener = object : TextView.OnEditorActionListener {
			//zwracana wartosc oznacza czy zamknac klawiature
			override fun onEditorAction(field: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
				val pressedKeyIsEnter = actionId == EditorInfo.IME_ACTION_DONE
				if(!pressedKeyIsEnter)
					return false


				val everyDigitIsOk = checkIfAllFieldsHaveEnteredDigits()
				if(!everyDigitIsOk)
					return false

				processPIN()
				return true
			}
		}
		val deleteButtonPressedListener = object : View.OnKeyListener{
			override fun onKey(source: View?, keyCode: Int, event: KeyEvent): Boolean {
				if(keyCode != KeyEvent.KEYCODE_DEL || event.action != KeyEvent.ACTION_DOWN)
				return false

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
				}
				return true
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
	private fun getProperTextsForGuiElements(){
		when(puprose){
			Purpose.Auth -> {
				titleField.text = resources.getString(R.string.GUI_authTransactionTitle)
				descriptionField.text = intent.getStringExtra(getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME))
			}
			Purpose.Set -> {
				descriptionField.text = null
				titleField.text = resources.getString(R.string.PIN_GUI_setPinTitle)
			}
			Purpose.Change -> {
				descriptionField.text = resources.getString(R.string.PIN_GUI_changeDescription_old)
				titleField.text = resources.getString(R.string.PIN_GUI_changeTitle)
				setUpForgotPinNameTextView()
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
		if(pin == null){
			val msg = resources.getString(R.string.PIN_UserMsg_haveToUseAllTheDigits)
			Utilities.showToast(this,msg)
			return
		}

		when(puprose){
			Purpose.Auth -> processAuth(pin)
			Purpose.Set -> processSet(pin)
			Purpose.Change -> processChange(pin)
		}
	}
	private fun requestFocusOnActivityStart(){
		digits[0].requestFocus()
		val showKeyboardObj = Runnable {
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(digits[0], InputMethodManager.SHOW_FORCED)
		}
		digits[0].postDelayed(showKeyboardObj, 250)
	}
	private fun checkIfPinIsCorrect(pin : Int) : Boolean{
		val savedPinHash = PreferencesOperator.readPrefStr(this, R.string.PREF_hashPin)
		val inputPinHash = Utilities.hashMd5(pin.toString())
		return inputPinHash == savedPinHash
	}
	private fun processAuth(pin : Int){
		val authCorrect = checkIfPinIsCorrect(pin)
		if(authCorrect){
			setResult(RESULT_OK,Intent())
			finish()
		}
		else{
			pinTriesLeft--
			val allowUserToTryOtherPin = pinTriesLeft > 0
			if(!allowUserToTryOtherPin){
				finishActivity(false)
				return
			}

			val textToShow = when(pinTriesLeft){
				2 -> resources.getString(R.string.PIN_UserMsg_triesLeft2)
				1 -> resources.getString(R.string.PIN_UserMsg_triesLeft1)
				else -> resources.getString(R.string.UserMsg_UNKNOWN_ERROR)
			}
			Utilities.showToast(this, textToShow)
			clearDigitsFields()
		}
		return
	}
	private fun processChange(pin : Int){
		when(phaseOfChangePinProcess){
			ChangePinProcessPhases.OLD_PIN ->{
				descriptionField.text = resources.getString(R.string.PIN_GUI_changeDescription_old)
				clearDigitsFields()
				val properOldPin = checkIfPinIsCorrect(pin)
				if(properOldPin) {
					phaseOfChangePinProcess = ChangePinProcessPhases.NEW_PIN
					descriptionField.text =	resources.getString(R.string.PIN_GUI_changeDescription_new)
				}
				else{
					pinTriesLeft--
					when(pinTriesLeft){
						2 -> {
							val msg = resources.getString(R.string.PIN_UserMsg_triesLeft2)
							Utilities.showToast(this,msg)
							return
						}
						1 -> {
							val msg = resources.getString(R.string.PIN_UserMsg_triesLeft1)
							Utilities.showToast(this,msg)
							return
						}
						else -> {
							val msg = resources.getString(R.string.PIN_UserMsg_failedToSetNewPin)
							Utilities.showToast(this,msg)
							finishActivity(false)
						}
					}
				}
			}
			ChangePinProcessPhases.NEW_PIN ->{
				descriptionField.text = resources.getString(R.string.PIN_GUI_changeDescription_new)
				tmpPIN = pin
				phaseOfChangePinProcess = ChangePinProcessPhases.NEW_PIN_AGAIN
			}
			ChangePinProcessPhases.NEW_PIN_AGAIN ->{
				descriptionField.text = resources.getString(R.string.PIN_GUI_changeDescription_newAgain)
				val twoPinsAreSame = pin == tmpPIN
				if(twoPinsAreSame){
					Utilities.showToast(this, resources.getString(R.string.PIN_UserMsg_newPinSet))
					saveNewPinInMemory(pin)
					finishActivity(true)
				}
				else{
					val msg = resources.getString(R.string.PIN_UserMsg_failedToSetNewPin)
					Utilities.showToast(this,msg)
					finishActivity(false)
				}
			}
		}
	}
	private fun processSet(pin: Int) {
		val itsFirstAttemptToSetPin = tmpPIN == 0
		if(itsFirstAttemptToSetPin){
			descriptionField.text = resources.getString(R.string.PIN_GUI_setPinAgainTitle)
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
		val hashedPin = Utilities.hashMd5(pin.toString())
		PreferencesOperator.savePref(this, R.string.PREF_hashPin, hashedPin)
	}
	private fun checkStartPurpose(){
		val purposeFieldName = resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
		val activityStartReasonStr = intent.getStringExtra(purposeFieldName)
		if(activityStartReasonStr == null){
			Log.e(TagProduction, "[checkStartPurpose/${this.javaClass.name}] Error in obtaining purpose STR")
			return
		}
		this.puprose = Purpose.fromString(activityStartReasonStr)
	}
	private fun clearDigitsFields(){
		digits.forEach { it.text.clear() }
		digits[0].requestFocus()
	}

	private fun setUpForgotPinNameTextView(){
		val forogtPinField = findViewById<TextView>(R.id.PIN_forgetPin_TextView)
		forogtPinField.isVisible = true
		forogtPinField.setOnClickListener{
			ActivityStarter.openDialogWithDefinedPurpose(this, YesNoDialogActivity.Companion.DialogPurpose.ResetPin)
		}
	}
	private fun finishActivity(success: Boolean){
		val result = if(success) RESULT_OK else RESULT_CANCELED
		setResult(result,Intent())
		finish()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		val properCode = resources.getInteger(R.integer.ACT_RETCODE_DIALOG_ResetPin)
		if(requestCode != properCode)
			return

		val resetPin = resultCode == RESULT_OK
		if(!resetPin)
			return

		PreferencesOperator.clearAuthData(this)

		val field = resources.getString(R.string.ACT_COM_DIALOG_ResetPin_FIELDNAME)
		val intentRet = Intent()
			.putExtra(field, true)
		setResult(RESULT_CANCELED,intentRet)
		finish()
	}

}
