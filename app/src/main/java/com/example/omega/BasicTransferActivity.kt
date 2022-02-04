package com.example.omega

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.math.BigInteger
import kotlin.math.floor
import android.text.SpannableStringBuilder
import android.util.Log


class BasicTransferActivity : AppCompatActivity() {
	private lateinit var receiverNumberEditText : EditText
	private lateinit var amountEditText : EditText
	private lateinit var receiverNameEditText : EditText
	private lateinit var transferTitle : EditText
	private lateinit var goNextButton : Button
	private val polishBankAccountNumberLength = 26
	private lateinit var amountAfterTransferTextView : TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_basic_transfer)
		findGuiElements()
		setListenersToGuiElements()
	}
	private fun setListenersToGuiElements(){
		val amountEditTextListener = object :TextWatcher{
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0!!.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(p0 != null)
					stopUserFromPuttingMoreThan2DigitsAfterComma(previousValue, p0.toString())
				printAmountAfterTransfer()
			}
		}
		val receiverNumberEditTextListener = object :TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {}
		}
		val receiverNameEditTextListener = object :TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {}
		}
		val transferTitleEditTextListener = object :TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {}
		}
		val buttonOnClickList = View.OnClickListener {

		}

		receiverNumberEditText.addTextChangedListener(receiverNumberEditTextListener)
		amountEditText.addTextChangedListener(amountEditTextListener)
		receiverNameEditText.addTextChangedListener(receiverNameEditTextListener)
		transferTitle.addTextChangedListener(transferTitleEditTextListener)
		goNextButton.setOnClickListener(buttonOnClickList)
	}
	private fun findGuiElements(){
		receiverNumberEditText = findViewById(R.id.basicTransfer_receiverNumber_EditText)
		amountEditText = findViewById(R.id.basicTransfer_amount_editText)
		receiverNameEditText = findViewById(R.id.basicTransfer_reciverName_EditText)
		transferTitle = findViewById(R.id.basicTransfer_transferTitle_EditText)
		goNextButton = findViewById(R.id.basicTransfer_goNext_button)
		amountAfterTransferTextView = findViewById(R.id.basicTransfer_amountAfterTransfer_TextView)
	}
	private fun getSelectedAccountBalance() : Double{
		//TODO
		return 12345.86
	}
	private fun printAmountAfterTransfer(){
		val currentMoney = getSelectedAccountBalance()
		val amountToTransfer = amountEditText.text.toString().toDoubleOrNull()
		if(amountToTransfer == null ||amountToTransfer == 0.0)
			amountAfterTransferTextView.text = null
		else{
			var moneyAfterTransfer = getAccountBalanceAfterTransfer()
			if(moneyAfterTransfer >= 0){
				val textBase = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer)
				val textToSet = "$textBase $moneyAfterTransfer"
				amountAfterTransferTextView.text = textToSet
			}
			else{
				val textToSet = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer_less_than_zero)
				amountAfterTransferTextView.text = textToSet
			}
		}
	}
	private fun stopUserFromPuttingMoreThan2DigitsAfterComma(oldVal : String, newVal : String){
		val indexOfDecimal = newVal.indexOf('.')
		if(indexOfDecimal != -1){
			val digitsAfterComma = (newVal.length - 1) - indexOfDecimal
			if(digitsAfterComma > 2){
				amountEditText.text = SpannableStringBuilder(oldVal)
				amountEditText.setSelection(amountEditText.length())//Setting cursor to end
			}
		}
	}
	private fun checkIfInputIsCorrect() : Boolean{
		val properAmount = getAccountBalanceAfterTransfer() >= 0
		val receiverAccNumberCorrect = receiverNumberEditText.text.length == 26
		val receiverNameCorrect = receiverNameEditText.text.length in 3..50

		val InputIsCorrect = properAmount && receiverAccNumberCorrect && receiverNameCorrect
		return receiverNameCorrect
	}
	private fun getAccountBalanceAfterTransfer() : Double{
		val balance = getSelectedAccountBalance()
		val amount = amountEditText.text.toString().toDoubleOrNull()
		var toRet = if(amount == null)
			balance
		else
			balance - amount
		toRet = floor(toRet * 100) / 100 //trim decimal after 2 digits
		return toRet
	}
	private fun fillListOfAccs(){

	}
}