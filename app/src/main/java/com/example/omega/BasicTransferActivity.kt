package com.example.omega

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import kotlin.math.floor
import android.widget.*
import com.example.omega.Utilities.Companion.TagProduction


class BasicTransferActivity : AppCompatActivity() {
	private lateinit var receiverNumberEditText : EditText
	private lateinit var amountEditText : EditText
	private lateinit var receiverNameEditText : EditText
	private lateinit var transferTitle : EditText
	private lateinit var goNextButton : Button
	private lateinit var amountAfterTransferTextView : TextView
	private lateinit var spinner : Spinner
	private lateinit var receiverAccNbrDigitsHint : TextView

	private lateinit var tokenCpy : Token
	private lateinit var currentPaymentAccount: PaymentAccount

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_basic_transfer)
		getToken()
		findGuiElements()
		setListenersToGuiElements()
		fillListOfAccounts()
	}


	private fun setListenersToGuiElements(){
		val amountEditTextListener = object :TextWatcher{
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(p0 != null)
					Utilities.stopUserFromPuttingMoreThan2DigitsAfterComma(
						amountEditText,
						previousValue,
						p0.toString()
					)
				showAmountAfterTransfer()
			}
		}
		val receiverNumberEditTextListener = object :TextWatcher{
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				showDigitsLeftToHaveProperAmountOfDigits()
			}
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
				userChangeAnotherAccOnSpiner()
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
		goNextButton = findViewById(R.id.RBlikCodeGenerator_goNext_button)
		amountAfterTransferTextView = findViewById(R.id.basicTransfer_amountAfterTransfer_TextView)
		spinner = findViewById(R.id.basicTransfer_accountList_Spinner)
		receiverAccNbrDigitsHint = findViewById(R.id.basicTransfer_receiverNumberDigitsLeft_TextView)
	}
	private fun showAmountAfterTransfer(){
		val amountToTransfer = amountEditText.text.toString().toDoubleOrNull()
		if(amountToTransfer == null ||amountToTransfer == 0.0){
			amountAfterTransferTextView.text = null
			return
		}

		val accBalanceAfterTransfer = getAccountBalanceAfterTransfer()
		if(accBalanceAfterTransfer == null){
			amountAfterTransferTextView.text = getString(R.string.GUI_basicTransfer_amountAfterTransfer_less_than_zero)
			amountAfterTransferTextView.setTextColor(Color.RED)
			return
		}

		if(accBalanceAfterTransfer >= 0){
			val textBase = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer)
			val accountCurrency = currentPaymentAccount.getCurrencyOfAccount()
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
	private fun checkInputData() : String?{
		val amountAfterTransfer = getAccountBalanceAfterTransfer()?: return resources.getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)

		val properAmount = amountAfterTransfer >= 0
		if(!properAmount)
			return resources.getString(R.string.UserMsg_basicTransfer_Amount_too_hight)

		val amountInserted = amountEditText.text.toString().toDoubleOrNull()
		if(amountInserted == null || amountInserted == 0.0)
			return getString(R.string.UserMsg_basicTransfer_Amount_zero)

		val receiverAccNumberCorrect = receiverNumberEditText.text.length == ApiFunctions.getLengthOfCountryBankNumberDigitsOnly() - ApiConsts.countryCodeLength
		if(!receiverAccNumberCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_TOO_SHORT_RECEIVER_ACC_NUMBER)

		val receiverNameCorrect = receiverNameEditText.text.length in 3..50
		if(!receiverNameCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_wrong_receiver_name)

		val titleCorrect = transferTitle.text.length in 3..50
		if(!titleCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_wrong_title)

		return null
	}
	private fun getAccountBalanceAfterTransfer() : Double?{
		val balance = currentPaymentAccount.getBalanceOfAccount()
		if (balance==null){
			Log.e(TagProduction, "[getAccountBalanceAfterTransfer/${this.javaClass.name}], error obtained null as balance of account")
			finishThisActivity(false, getString(R.string.UserMsg_UNKNOWN_ERROR))
			return null
		}

		val transferAmount = amountEditText.text.toString().toDoubleOrNull()?: 0.0
		val balanceAfterTransfer = balance - transferAmount
		return floor(balanceAfterTransfer * 100) / 100 //trim decimal after 2 digits
	}
	private fun fillListOfAccounts(){
		if(!tokenCpy.getDetailsOfAccountsFromBank(this)){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token cant obtain accounts Details")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			finishThisActivity(false,errorCodeTextToDisplay)
			return
		}

		val listOfAccountsFromToken = tokenCpy.getListOfAccountsToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token return null or empty account list")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			finishThisActivity(false,errorCodeTextToDisplay)
			return
		}

		val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item)
		listOfAccountsFromToken.forEach{
			adapter.add(it)
		}
		spinner.adapter = adapter
	}
	private fun finishThisActivity(success : Boolean, errorCode : String? = null){
		if(errorCode!=null && !success)
			Utilities.showToast(this, errorCode)
		this.finish()
	}
	private fun goNextActivityButtonClicked(){
		val inputErrorText = checkInputData()
		if(inputErrorText != null){
			Utilities.showToast(this, inputErrorText)
			return
		}

		val receiverAccNumber = receiverNumberEditText.text.toString()
		val receiverName = receiverNameEditText.text.toString()
		val amount = amountEditText.text.toString().toDouble()
		val title = transferTitle.text.toString()
		val accountCurrency = currentPaymentAccount.getCurrencyOfAccount()
		val senderAccNumber = currentPaymentAccount.getAccNumber()
		val transferData = TransferData(senderAccNumber,receiverAccNumber,receiverName,title,amount,accountCurrency)

		val transferDataSerialized = transferData.toString()
		if(!transferDataSerialized.isNullOrEmpty()){
			ActivityStarter.startTransferSummaryActivity(this, transferDataSerialized)
			finishThisActivity(true)
		}
		else
			finishThisActivity(false, getString(R.string.UserMsg_basicTransfer_unkownError))
	}
	private fun getToken(){
		val tokenTmp = PreferencesOperator.getToken(this)
		if(tokenTmp.isOk(this))
			tokenCpy = tokenTmp
		else
			finish()
	}
	private fun userChangeAnotherAccOnSpiner(){
		val selectedItemText = spinner.selectedItem.toString()
		val accountNumber = tokenCpy.getAccountNbrByDisplayStr(selectedItemText) ?: return//todo give some msg
		val paymentAccountTmp = tokenCpy.getPaymentAccount(accountNumber)
		if(paymentAccountTmp == null){
			Log.e(TagProduction, "[userChangeAnotherAccOnSpiner/${this.javaClass.name}] Cant get payment info from token")
			finishThisActivity(false, getString(R.string.UserMsg_UNKNOWN_ERROR))
		}
		else{
			amountEditText.text = null
			currentPaymentAccount = paymentAccountTmp
		}
	}
	private fun showDigitsLeftToHaveProperAmountOfDigits(){
		val inputDigitsNumber = receiverNumberEditText.text.length
		if(inputDigitsNumber == 0){
			receiverAccNbrDigitsHint.text = null
			return
		}

		val amountOfDigitsThatHaveToBePut = ApiFunctions.getLengthOfCountryBankNumberDigitsOnly() - inputDigitsNumber - ApiConsts.countryCodeLength//countryCode letters
		if(amountOfDigitsThatHaveToBePut == 0)
			receiverAccNbrDigitsHint.text = null
		else{
			val textToSet = "W numerze brakuje $amountOfDigitsThatHaveToBePut cyfr"
			receiverAccNbrDigitsHint.text = textToSet
		}
	}
}