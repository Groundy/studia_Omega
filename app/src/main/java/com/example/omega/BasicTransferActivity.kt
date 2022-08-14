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
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class BasicTransferActivity : AppCompatActivity() {
	private lateinit var receiverNumberEditText : EditText
	private lateinit var amountEditText : EditText
	private lateinit var receiverNameEditText : EditText
	private lateinit var transDesEditText : EditText
	private lateinit var goNextButton : Button
	private lateinit var amountAfterTransferTextView : TextView
	private lateinit var spinner : Spinner
	private lateinit var receiverAccNbrDigitsHint : TextView
	private lateinit var addTransToBundleButton : Button
	private lateinit var numberOfTransInBundle : TextView

	private lateinit var tokenCpy : Token
	private var currentPaymentAccount: PaymentAccount? = null

	private var transferDataListToBundle = arrayListOf<TransferData>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_basic_transfer)
		setUpGui()
		val dialog = WaitingDialog(this)
		CoroutineScope(IO).launch {
			dialog.changeText(this@BasicTransferActivity, R.string.POPUP_getToken)
			val tokenObtained = getToken()
			if(!tokenObtained){
				withContext(Main){
					dialog.hide()
					val msg = this@BasicTransferActivity.getString(R.string.UserMsg_UNKNOWN_ERROR)
					Utilities.showToast(this@BasicTransferActivity, msg)
				}
				return@launch
			}

			dialog.changeText(this@BasicTransferActivity, R.string.POPUP_getAccountsDetails)
			val spinnerAdapter = getListOfAccountsForSpinner()

			if(spinnerAdapter == null){
				withContext(Main){
					dialog.hide()
					val msg = this@BasicTransferActivity.getString(R.string.UserMsg_UNKNOWN_ERROR)
					Utilities.showToast(this@BasicTransferActivity, msg)
				}
				return@launch
			}
			withContext(Main){
				dialog.hide()
				spinner.adapter = spinnerAdapter
				fillElementsFromIntentDataIfExists()
			}
		}
	}

	private fun setUpGui(){
		receiverNumberEditText = findViewById(R.id.basicTransfer_receiverNumber_EditText)
		with(receiverNumberEditText){
			val receiverNumberEditTextListener = object :TextWatcher{
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun afterTextChanged(p0: Editable?) {
					showDigitsLeftToHaveProperAmountOfDigits()
				}
			}
			addTextChangedListener(receiverNumberEditTextListener)
		}

		amountEditText = findViewById(R.id.basicTransfer_amount_editText)
		with(amountEditText){
			val amountEditTextListener = object :TextWatcher{
				var previousValue : String = ""
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
					if(p0 != null)
						previousValue = p0.toString()
				}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun afterTextChanged(p0: Editable?) {
					if(!p0.isNullOrEmpty())
						Utilities.stopUserFromPuttingMoreThan2DigitsAfterComma(
							amountEditText,
							previousValue,
							p0.toString()
						)
					showAmountAfterTransfer()
				}
			}
			addTextChangedListener(amountEditTextListener)
		}

		receiverNameEditText = findViewById(R.id.basicTransfer_reciverName_EditText)
		with(receiverNumberEditText){
			val receiverNameEditTextListener = object :TextWatcher{
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun afterTextChanged(p0: Editable?) {}
			}
			addTextChangedListener(receiverNameEditTextListener)
		}

		transDesEditText = findViewById(R.id.basicTransfer_transferTitle_EditText)
		with(transDesEditText){
			val transferTitleEditTextListener = object :TextWatcher{
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun afterTextChanged(p0: Editable?) {}
			}
			addTextChangedListener(transferTitleEditTextListener)
		}

		goNextButton = findViewById(R.id.basicTransfer_goNext_button)
		goNextButton.setOnClickListener {goNextActivityButtonClicked()}

		addTransToBundleButton = findViewById(R.id.basicTransfer_addNewTrans_button)
		addTransToBundleButton.setOnClickListener {
			userAddedTransferToBundle()
		}

		spinner = findViewById(R.id.basicTransfer_accountList_Spinner)
		with(spinner){
			val selectedItemChangedListener = object :  AdapterView.OnItemSelectedListener {
				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					userChangeAnotherAccOnSpiner()
				}
				override fun onNothingSelected(p0: AdapterView<*>?) {}
			}

			onItemSelectedListener = selectedItemChangedListener
		}

		amountAfterTransferTextView = findViewById(R.id.basicTransfer_amountAfterTransfer_TextView)
		receiverAccNbrDigitsHint = findViewById(R.id.basicTransfer_receiverNumberDigitsLeft_TextView)
		numberOfTransInBundle = findViewById(R.id.basicTransfer_numberOfTransactionsInBundle_textView)
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

		val recipientAccNumbOjb = AccountNumber(receiverNumberEditText.text.toString())
		val recipientAccNumberLengthOk = recipientAccNumbOjb.lengthOK()
		if(!recipientAccNumberLengthOk)
			return resources.getString(R.string.UserMsg_basicTransfer_TOO_SHORT_RECEIVER_ACC_NUMBER)

		val formatOk = recipientAccNumbOjb.checkIfIsProperIbanFormar()
		if(!formatOk)
			return resources.getString(R.string.UserMsg_basicTransfer_NOT_IBAN_FORMAT)

		val receiverNameCorrect = receiverNameEditText.text.length in 3..50
		if(!receiverNameCorrect)
			return resources.getString(R.string.UserMsg_basicTransfer_wrong_receiver_name)

		val titleCorrect = transDesEditText.text.length in 3..50
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
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
				finishThisActivityWithError(errorCodeTextToDisplay)
			}
			return null
		}

		val listOfAccountsFromToken = tokenCpy.getListOfAccountsNumbersToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "$errorBase token return null or empty account list")
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
				finishThisActivityWithError(errorCodeTextToDisplay)
			}
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
		if(transferDataListToBundle.size == 0){
			val inputErrorText = checkInputData()
			if(inputErrorText != null){
				Utilities.showToast(this, inputErrorText)
				return
			}
			if(currentPaymentAccount==null)
				return

			val transferData = getTransferDataFromFields()
			if(transferData == null){
				val errorStr = getString(R.string.UserMsg_basicTransfer_unkownError)
				finishThisActivityWithError(errorStr)
				return
			}

			val transferDataSerialized = transferData.toString()
			if(transferDataSerialized.isEmpty()){
				val errorStr = getString(R.string.UserMsg_basicTransfer_unkownError)
				finishThisActivityWithError(errorStr)
				return
			}

			val showSummary = !PreferencesOperator.readPrefBool(this, R.string.PREF_skipSummaryWindows)
			if(showSummary)
				ActivityStarter.startTransferSummaryActivity(this, transferData)
			else{
				val descripitionToShow = "${transferData.amount.toString()} ${transferData.currency}"
				ActivityStarter.startAuthActivity(this, descripitionToShow)
			}
		}
		else{
			var totalAmount = 0.0
			transferDataListToBundle.forEach {
				totalAmount += it.amount!!
			}
			val strToShow = Utilities.doubleToTwoDigitsAfterCommaString(totalAmount)

			val descripitionToShow = "$strToShow PLN"
			ActivityStarter.startAuthActivity(this, descripitionToShow)
		}

	}
	private suspend fun getToken() : Boolean{
		val tokenTmp = PreferencesOperator.getToken(this)
		val tokenOk = tokenTmp.isOk(this)
		if(!tokenOk)
			return false

		tokenCpy = tokenTmp
		return true
	}
	private fun userChangeAnotherAccOnSpiner(){
		val selectedItemText = spinner.selectedItem.toString()
		val accountNumber = tokenCpy.getAccountNbrByDisplayStr(selectedItemText)
		if(accountNumber == null){
			Log.e(TagProduction, "[userChangeAnotherAccOnSpiner/${this.javaClass.name}] Cant get payment info from token")
			val errorStr = getString(R.string.UserMsg_UNKNOWN_ERROR)
			finishThisActivityWithError(errorStr)
			return
		}

		val paymentAccountTmp = tokenCpy.getPaymentAccount(accountNumber)
		if(paymentAccountTmp == null){
			Log.e(TagProduction, "[userChangeAnotherAccOnSpiner/${this.javaClass.name}] Cant get payment info from token")
			val errorStr = getString(R.string.UserMsg_UNKNOWN_ERROR)
			finishThisActivityWithError(errorStr)
			return
		}

		val calledByUserAction = amountEditText.isFocusable
		if(calledByUserAction)
			amountEditText.text = null
		currentPaymentAccount = paymentAccountTmp
		showAmountAfterTransfer()
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
	private fun fillElementsFromIntentDataIfExists(){
		val fieldName = this.getString(R.string.ACT_COM_BASIC_TRANS_INTENT_FIELDNAME)
		val transferDataSerialized = try {
			intent.getStringExtra(fieldName)
		}catch (e : Exception){
			null
		}
		val startedFromCode = transferDataSerialized != null
		if(!startedFromCode){
			wookieTestFillWidgetsWithTestData()//todo tmp tymczasowe
			return
		}


		val transferData = try {
			TransferData.fromJsonSerialized(transferDataSerialized!!)
		}catch (e : Exception){
			Log.e(TagProduction, "[fillElementsFromIntentDataIfExists/${this.javaClass.name}] error in recreating TransferData Obj from str from intent")
			null
		} ?: return

		with(receiverNumberEditText){
			val recieverAccNumberStr = AccountNumber(transferData.receiverAccNumber!!).toStringWithoutCountry()
			text = Utilities.strToEditable(recieverAccNumberStr)
			isFocusable = false
			setTextColor(Color.GRAY)
		}
		with(amountEditText){
			text = Utilities.strToEditable(transferData.amount.toString())
			isFocusable = false
			setTextColor(Color.GRAY)
		}
		with(receiverNameEditText){
			text = Utilities.strToEditable(transferData.receiverName)
			isFocusable = false
			setTextColor(Color.GRAY)
		}
		with(transDesEditText){
			text = Utilities.strToEditable(transferData.description)
			isFocusable = false
			setTextColor(Color.GRAY)
		}
		addTransToBundleButton.visibility = View.GONE
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(resultCode != RESULT_OK)
			return

		var serializedTransferData : String? = null
		if(transferDataListToBundle.size == 0) {
			val transferData = getTransferDataFromFields()
			if (transferData != null){
				val arr = JSONArray().put(transferData)
				serializedTransferData = arr.toString()
			}
		}
		else{
			val arr = JSONArray()
				transferDataListToBundle.forEach {
				arr.put(it.toJsonObject())
			}
			serializedTransferData = arr.toString()
		}

		if(serializedTransferData.isNullOrEmpty()) {
			val errorStr = getString(R.string.UserMsg_basicTransfer_unkownError)
			finishThisActivityWithError(errorStr)
			return
		}

		val fieldName = resources.getString(R.string.ACT_COM_MANY_RetTransferData_FIELDNAME)
		val newIntent = Intent()
			.putExtra(fieldName, serializedTransferData)

		setResult(RESULT_OK, newIntent)
		finish()
	}
	private fun wookieTestFillWidgetsWithTestData(){
		//todo tmp tymczasowe
		//currentPaymentAccount = Utilities.wookieTestGetTestPaymentAccountForPaymentAct()
		receiverNumberEditText.text = Utilities.strToEditable("09124026981111001066212622")
		amountEditText.text =  Utilities.strToEditable("1.23")
		receiverNameEditText.text = Utilities.strToEditable("Ciocia Zosia")
		transDesEditText.text = Utilities.strToEditable("Zwrot za paczkę")
	}
	private fun getTransferDataFromFields() : TransferData?{
		return try {
			val transferData = TransferData()
			with(transferData){
				val recipientAccNumberObj = AccountNumber(receiverNumberEditText.text.toString())
				receiverAccNumber = recipientAccNumberObj.toStringWithCountry()
				receiverName = receiverNameEditText.text.toString()
				senderAccNumber = currentPaymentAccount!!.getAccNumber()
				senderAccName = currentPaymentAccount!!.getOwnerName()
				amount = amountEditText.text.toString().toDouble()
				description = transDesEditText.text.toString()
				currency = currentPaymentAccount!!.getCurrencyOfAccount()
				executionDate = OmegaTime.getDate()
			}
			transferData
		}catch (e : Exception){
			Log.e(TagProduction, "[getTransferDataFromFields/${this.javaClass.name}]")
			null
		}

	}
	private fun userAddedTransferToBundle(){
		val transferData = getTransferDataFromFields()
		if(transferData == null){
			//todo
			return
		}
		if(transferDataListToBundle.size == 10){
			Utilities.showToast(this, "Maksymalnie 10 przelewów w jednej paczce")
			return
		}

		transferDataListToBundle.add(transferData)
		receiverNumberEditText.text = null
		amountEditText.text = null
		receiverNameEditText.text = null
		transDesEditText.text = null
		amountAfterTransferTextView.text = null
		wookieTestFillWidgetsWithTestData()

		if(transferDataListToBundle.size == 0){
			val base = resources.getString(R.string.GUI_basicTransfer_numberOfTransfers)
			val textToSet = "$base 0"
			numberOfTransInBundle.text = Utilities.strToEditable(textToSet)
			numberOfTransInBundle.visibility = View.GONE
		}

		var amount = 0.0
		var desciption = "\n"
		val maxLength = 30
		transferDataListToBundle.forEach {
			amount += it.amount!!
			val endIndex = if(it.description!!.length > maxLength)
				maxLength
			else
				it.description!!.length

			val desToAdd = "${it.amount} -> ${it.description!!.subSequence(0, endIndex)}\n"
			desciption += desToAdd
		}
		val amountStr = "(${((amount*100).toInt()/100.0)})"

		numberOfTransInBundle.visibility = View.VISIBLE
		val base = resources.getString(R.string.GUI_basicTransfer_numberOfTransfers)
		val textToSet = "$base ${transferDataListToBundle.size} $amountStr".plus(desciption)
		numberOfTransInBundle.text = Utilities.strToEditable(textToSet)
	}

}