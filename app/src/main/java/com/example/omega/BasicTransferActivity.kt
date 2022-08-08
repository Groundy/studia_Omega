package com.example.omega

import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

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
	private var currentPaymentAccount: PaymentAccount? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_basic_transfer)
		findGuiElements()
		setListenersToGuiElements()
		val dialog = WaitingDialog(this)
		CoroutineScope(IO).launch {
			dialog.changeText(this@BasicTransferActivity, R.string.POPUP_getToken)
			val tokenObtained = getToken()
			if(!tokenObtained){
				withContext(Main){
					dialog.hide()	//todo info for user
				}
				return@launch//todo maybe finish act
			}

			dialog.changeText(this@BasicTransferActivity, R.string.POPUP_getAccountsDetails)
			val spinnerAdapter = getListOfAccountsForSpinner()
			if(spinnerAdapter == null){
				withContext(Main){
					dialog.hide()		//todo info
				}
				return@launch//todo maybe finish act
			}
			withContext(Main){
				dialog.hide()
				spinner.adapter = spinnerAdapter
				wookieTestFillTmpWidgets()
			}
		}

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

		if(currentPaymentAccount==null)
			return

		if(accBalanceAfterTransfer >= 0){
			val textBase = resources.getString(R.string.GUI_basicTransfer_amountAfterTransfer)
			val accountCurrency = currentPaymentAccount!!.getCurrencyOfAccount()
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

		val accountNumber = AccountNumber(receiverNumberEditText.text.toString())

		val recipientAccNumberLengthOk = accountNumber.lengthOK()
		if(!recipientAccNumberLengthOk)
			return resources.getString(R.string.UserMsg_basicTransfer_TOO_SHORT_RECEIVER_ACC_NUMBER)

		val formatOk = accountNumber.checkIfIsProperIbanFormar()
		if(!formatOk)
			return resources.getString(R.string.UserMsg_basicTransfer_NOT_IBAN_FORMAT)

		val receiverNameCorrect = receiverNameEditText.text.length in 3..50
		if(!receiverNameCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_wrong_receiver_name)

		val titleCorrect = transferTitle.text.length in 3..50
		if(!titleCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_wrong_title)

		return null
	}
	private fun getAccountBalanceAfterTransfer() : Double?{
		if(currentPaymentAccount == null)
			return null

		val balance = currentPaymentAccount!!.getBalanceOfAccount()
		if (balance==null){
			Log.e(TagProduction, "[getAccountBalanceAfterTransfer/${this.javaClass.name}], error obtained null as balance of account")
			finishThisActivityWithError(getString(R.string.UserMsg_UNKNOWN_ERROR))
			return null
		}

		val transferAmount = amountEditText.text.toString().toDoubleOrNull()?: 0.0
		val balanceAfterTransfer = balance - transferAmount
		return floor(balanceAfterTransfer * 100) / 100 //trim decimal after 2 digits
	}
	private suspend fun getListOfAccountsForSpinner() : SpinnerAdapter?{
		val errorBase = "[getListOfAccountsForSpinner/${this.javaClass.name}]"

		if(!tokenCpy.fillTokenAccountsWithBankDetails(this)){
			Log.e(TagProduction, "$errorBase token cant obtain accounts Details")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			finishThisActivityWithError(errorCodeTextToDisplay)
			return null
		}

		val listOfAccountsFromToken = tokenCpy.getListOfAccountsNumbersToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "$errorBase token return null or empty account list")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			finishThisActivityWithError(errorCodeTextToDisplay)
			return null
		}

		val listOfAccountsWithPlnCurrency = ArrayList<String>()
		listOfAccountsFromToken.forEach{
			if(it.contains("PLN"))
				listOfAccountsWithPlnCurrency.add(it)
		}


		if(listOfAccountsWithPlnCurrency.isEmpty()){
			Log.e(TagProduction, "$errorBase token returned ${listOfAccountsFromToken.size} acounts but any of them was in PLN")
			withContext(Main){
				Utilities.showToast(this@BasicTransferActivity, "Na liście nie ma konta złotówkowego.")
			}
		}

		return ArrayAdapter(this@BasicTransferActivity,android.R.layout.simple_spinner_item, listOfAccountsWithPlnCurrency.toList())
	}
	private fun finishThisActivityWithError(errorCode : String? = null){
		if(errorCode!=null)
			Utilities.showToast(this, errorCode)
		finish()
	}
	private fun goNextActivityButtonClicked(){
		val inputErrorText = checkInputData()
		if(inputErrorText != null){
			Utilities.showToast(this, inputErrorText)
			return
		}
		if(currentPaymentAccount==null)
			return

		val transferData = TransferData().also {
			it.receiverAccNumber = "PL".plus(receiverNumberEditText.text.toString()) //todo
			it.receiverName = receiverNameEditText.text.toString()
			it.senderAccNumber = currentPaymentAccount!!.getAccNumber()
			it.senderAccName = currentPaymentAccount!!.getOwnerName()
			it.amount = amountEditText.text.toString().toDouble()
			it.description = transferTitle.text.toString()
			it.currency = currentPaymentAccount!!.getCurrencyOfAccount()
			it.executionDate = OmegaTime.getDate()
		}

		val transferDataSerialized = transferData.toString()
		if(transferDataSerialized.isEmpty()){
			val errorStr = getString(R.string.UserMsg_basicTransfer_unkownError)
			finishThisActivityWithError(errorStr)
			return
		}

		ActivityStarter.startTransferSummaryActivity(this, transferDataSerialized)
	}
	private fun getToken() : Boolean{
		val tokenTmp = PreferencesOperator.getToken(this)
		val tokenOk = tokenTmp.isOk(this)
		if(!tokenOk)
			return false

		tokenCpy = tokenTmp
		return true
	}
	private fun userChangeAnotherAccOnSpiner(){
		val selectedItemText = spinner.selectedItem.toString()
		val accountNumber = tokenCpy.getAccountNbrByDisplayStr(selectedItemText) ?: return//todo give some msg
		val paymentAccountTmp = tokenCpy.getPaymentAccount(accountNumber)
		if(paymentAccountTmp == null){
			Log.e(TagProduction, "[userChangeAnotherAccOnSpiner/${this.javaClass.name}] Cant get payment info from token")
			val errorStr = getString(R.string.UserMsg_UNKNOWN_ERROR)
			finishThisActivityWithError(errorStr)
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

		val amountOfDigitsThatHaveToBePut = ApiFunctions.getLengthOfCountryBankNumberWitchCountryCode() - inputDigitsNumber - ApiConsts.countryCodeLength
		if(amountOfDigitsThatHaveToBePut == 0)
			receiverAccNbrDigitsHint.text = null
		else{
			val textToSet = "W numerze brakuje $amountOfDigitsThatHaveToBePut cyfr"
			receiverAccNbrDigitsHint.text = textToSet
		}
	}
	private fun wookieTestFillTmpWidgets(){
		//currentPaymentAccount = Utilities.wookieTestGetTestPaymentAccountForPaymentAct()
		receiverNumberEditText.text = Utilities.strToEditable("09124026981111001066212622")//mj
		amountEditText.text =  Utilities.strToEditable("1.23")
		receiverNameEditText.text = Utilities.strToEditable("Ciocia Zosia")
		transferTitle.text = Utilities.strToEditable("Zwrot za paczkę")

	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(resultCode != RESULT_OK)
			return

		val fieldName = resources.getString(R.string.ACT_COM_MANY_RetTransferData_FIELDNAME)
		val transferDataStr = try {
			data!!.extras!!.getString(fieldName)
		}catch (e : Exception){
			//todo give msg
			null
		}
		if(transferDataStr == null){
			setResult(RESULT_CANCELED)
			finish()
			return
		}

		val newIntent = Intent()
			.putExtra(fieldName, transferDataStr)

		setResult(RESULT_OK, newIntent)
		finish()

	}
}