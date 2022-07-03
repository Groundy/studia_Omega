package com.example.omega

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlin.math.floor
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.*


class BasicTransferActivity : AppCompatActivity() {
	private lateinit var receiverNumberEditText : EditText
	private lateinit var amountEditText : EditText
	private lateinit var receiverNameEditText : EditText
	private lateinit var transferTitle : EditText
	private lateinit var goNextButton : Button
	private val polishBankAccountNumberLength = 26
	private lateinit var amountAfterTransferTextView : TextView
	private lateinit var spinner : Spinner

	private var availableBalance : Double? = null
	private var accountCurrency : String? = null
	private var senderAccNumber : String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_basic_transfer)
		findGuiElements()
		setListenersToGuiElements()
		fillListOfAccounts()
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
		val selectedItemChangedListener = object :  AdapterView.OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				val success = getInfoAboutChosenPaymentAccount()
				if(!success)
					finishThisActivity(false,getString(R.string.USER_MSG_basicTransfer_error_reciving_acc_balance))
				amountEditText.text = null
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {}
		}

		receiverNumberEditText.addTextChangedListener(receiverNumberEditTextListener)
		amountEditText.addTextChangedListener(amountEditTextListener)
		receiverNameEditText.addTextChangedListener(receiverNameEditTextListener)
		transferTitle.addTextChangedListener(transferTitleEditTextListener)
		goNextButton.setOnClickListener { goNextActivityButtonClicked() }
		spinner.onItemSelectedListener = selectedItemChangedListener
	}
	private fun findGuiElements(){
		receiverNumberEditText = findViewById(R.id.basicTransfer_receiverNumber_EditText)
		amountEditText = findViewById(R.id.basicTransfer_amount_editText)
		receiverNameEditText = findViewById(R.id.basicTransfer_reciverName_EditText)
		transferTitle = findViewById(R.id.basicTransfer_transferTitle_EditText)
		goNextButton = findViewById(R.id.basicTransfer_goNext_button)
		amountAfterTransferTextView = findViewById(R.id.basicTransfer_amountAfterTransfer_TextView)
		spinner = findViewById<Spinner>(R.id.basicTransfer_accountList_Spinner)

	}
	private fun printAmountAfterTransfer(){
		val amountToTransfer = amountEditText.text.toString().toDoubleOrNull()
		if(amountToTransfer == null ||amountToTransfer == 0.0){
			amountAfterTransferTextView.text = null
			return
		}

		val accBalanceAfterTransfer = getAccountBalanceAfterTransfer()
		if(accBalanceAfterTransfer == null){
			amountAfterTransferTextView.text = "Błąd"
			amountAfterTransferTextView.setTextColor(Color.RED)
			return
		}

		if(accBalanceAfterTransfer >= 0){
			val textBase = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer)
			val textToSet = "$textBase $accBalanceAfterTransfer $accountCurrency"
			amountAfterTransferTextView.text = textToSet
			amountAfterTransferTextView.setTextColor(Color.BLACK)
		}
		else{
			val textToSet = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer_less_than_zero)
			amountAfterTransferTextView.text = textToSet
			amountAfterTransferTextView.setTextColor(Color.RED)
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
		val amountAfterTransfer = getAccountBalanceAfterTransfer()
		if(amountAfterTransfer == null)
			return resources.getString(R.string.USER_MSG_basicTransfer_error_reciving_acc_balance)

		val properAmount = amountAfterTransfer >= 0
		if(!properAmount)
			return resources.getString(R.string.USER_MSG_basicTransfer_Amount_too_hight)

		val amountInserted = amountEditText.text.toString().toDoubleOrNull()
		if(amountInserted == null || amountInserted == 0.0)
			return getString(R.string.USER_MSG_basicTransfer_Amount_zero)

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
	private fun getAccountBalanceAfterTransfer() : Double?{
		val balance = availableBalance
		if(balance == null)
			return null
		var transferAmount : Double? = amountEditText.text.toString().toDoubleOrNull()
		if(transferAmount == null)
			transferAmount = 0.0

		var balanceAfterTransfer = balance - transferAmount
		return floor(balanceAfterTransfer * 100) / 100 //trim decimal after 2 digits
	}
	private fun fillListOfAccounts(){
		val obtainedAccountData = API_getPaymentAccDetails.run(this)
		if(!obtainedAccountData){
			val errorCodeTextToDisplay = getString(R.string.USER_MSG_basicTransfer_error_reciving_acc_balance)
			finishThisActivity(false,errorCodeTextToDisplay)
			return
		}
		val listOfAccountFromToken = UserData.accessTokenStruct?.listOfAccounts!!
		val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item)
		listOfAccountFromToken.forEach{
			adapter.add(it.getDisplayString())
		}
		spinner.adapter = adapter
	}
	private fun finishThisActivity(success : Boolean, errorCode : String? = null){
		if(errorCode!=null && success == false)
			Utilites.showToast(this, errorCode)
		this.finish()
	}
	private fun getInfoAboutChosenPaymentAccount() : Boolean{
		val currentChosenItem = spinner.selectedItem.toString()
		val separator = "]  "
		val indexOfStartAccNumber = currentChosenItem.indexOf(separator)
		if(indexOfStartAccNumber == -1)
			return false//todo

		val accountNumber = currentChosenItem.substring(indexOfStartAccNumber + separator.length)
		val accessToken = UserData.accessTokenStruct
		if(accessToken == null)
			return false //todo

		val accountBalance = accessToken.getBalanceOfAccount(accountNumber)
		val accountCurrency = accessToken.getCurrencyOfAccount(accountNumber)
		if(accountBalance == null || accountCurrency.isNullOrEmpty())
			return false //todo

		this.availableBalance = accountBalance
		this.accountCurrency = accountCurrency
		this.senderAccNumber = accountNumber
		return true
	}
	private fun goNextActivityButtonClicked(){
		val inputErrorText : String? = getErrorInputText()
		if(inputErrorText != null){
			Utilites.showToast(this, inputErrorText!!)
			return
		}

		val receiverAccNumber = receiverNumberEditText.text.toString()
		val receiverName = receiverNameEditText.text.toString()
		val amount = amountEditText.text.toString().toDouble()
		val title = transferTitle.text.toString()
		val transferData = TransferData(senderAccNumber,receiverAccNumber,receiverName,title,amount,accountCurrency)

		val transferDataSerialized = transferData.serialize()
		if(!transferDataSerialized.isNullOrEmpty()){
			ActivityStarter.startTransferSummaryActivity(this, transferDataSerialized!!)
			finishThisActivity(true)
		}
		else
			finishThisActivity(false, getString(R.string.USER_MSG_basicTransfer_unkownError))
	}
}