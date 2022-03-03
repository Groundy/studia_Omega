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
			val inputErrorText : String? = getErrorInputText()
			val inputCorrect = inputErrorText == null
			if(inputCorrect){
				val senderAccNumber = "12312312312312312312312312"				//TODO
				val receiverAccNumber = receiverNumberEditText.text.toString()
				val receiverName = receiverNameEditText.text.toString()
				val amount = amountEditText.text.toString().toDoubleOrNull()
				val title = transferTitle.text.toString()
				val transferData = TransferData(senderAccNumber,receiverAccNumber,receiverName,title,amount)
				ActivityStarter.startTransferSummaryActivity(this, transferData)
				this.finish()
			}
			else{
				Utilites.showToast(this, inputErrorText!!)
			}
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
	private fun getErrorInputText() : String?{
		val properAmount = getAccountBalanceAfterTransfer() >= 0
		if(!properAmount)
			return resources.getString(R.string.USER_MSG_basicTransfer_Amount_too_hight)

		val receiverAccNumberCorrect = receiverNumberEditText.text.length == polishBankAccountNumberLength
		if(!receiverAccNumberCorrect)
			return resources.getString(R.string.USER_MSG_basicTransfer_TOO_SHORT_RECEIVER_ACC_NUMBER)

		val receiverNameCorrect = receiverNameEditText.text.length in 3..50
		if(!receiverNameCorrect)
			return resources.getString(R.string.USER_MSG_basicTransfer_wrong_receiver_name)

		val titleCorrect = transferTitle.text.length in 3..50
		if(!titleCorrect)
			return resources.getString(R.string.USER_MSG_basicTransfer_wrong_title)

		return null
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
	private fun finishThisActivity(success : Boolean, errorCode : String?){

	}
}