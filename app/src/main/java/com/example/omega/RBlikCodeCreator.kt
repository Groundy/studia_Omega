package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RBLIKCodeCreator : AppCompatActivity() {
	private lateinit var amountField : EditText
	private lateinit var titleField : EditText
	private lateinit var receiverNameField : EditText
	private lateinit var accountListSpinner : Spinner
	private lateinit var goNextActivityButton : Button
	private lateinit var tokenCpy : Token

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rblik_code_creator)
		setUpGui()
		val dialog = WaitingDialog(this, R.string.POPUP_getToken)
		CoroutineScope(IO).launch{
			val success = getTokenCpy()
			if(!success){
				withContext(Main){
					dialog.hide()
					val msg = this@RBLIKCodeCreator.getString(R.string.UserMsg_UNKNOWN_ERROR)
					Utilities.showToast(this@RBLIKCodeCreator, msg)
				}
				return@launch
			}
			val spinnerAdapter = getListOfAccountsForSpinner()
			if(spinnerAdapter == null){
				withContext(Main){
					dialog.hide()
					val msg = this@RBLIKCodeCreator.getString(R.string.UserMsg_UNKNOWN_ERROR)
					Utilities.showToast(this@RBLIKCodeCreator, msg)
				}
				return@launch
			}

			withContext(Main){
				accountListSpinner.adapter = spinnerAdapter
				fillReceiverName()
				wookieTestFillWidgets()
				dialog.hide()
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode != resources.getInteger(R.integer.ACT_RETCODE_DISPLAY_ACTIVITY))
			return

		if(resultCode == RESULT_CANCELED)
			return

		setResult(RESULT_OK)
		finish()
	}

	private fun setUpGui(){
		amountField = findViewById(R.id.RBlikCodeGenerator_amount_editText)
		titleField = findViewById(R.id.RBlikCodeGenerator_transferTitle_EditText)
		receiverNameField = findViewById(R.id.RBlikCodeGenerator_reciverName_EditText)
		accountListSpinner = findViewById(R.id.RBlikCodeGenerator_accountList_Spinner)
		goNextActivityButton = findViewById(R.id.basicTransfer_goNext_button)
		goNextActivityButton.setOnClickListener{
			goNextButtonClicked()
		}

		val amountEditTextListener = object : TextWatcher {
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(p0 != null)
					Utilities.stopUserFromPuttingMoreThan2DigitsAfterComma(
						amountField,
						previousValue,
						p0.toString()
					)
			}
		}
		amountField.addTextChangedListener(amountEditTextListener)
	}
	private fun goNextButtonClicked(){
		val dataOk = validateDataToGenRBlikCode()
		if(!dataOk)
			return

		val transferData = getDataForServer()
		if(transferData == null){
			val userMsg = getString(R.string.UserMsg_UNKNOWN_ERROR)
			Utilities.showToast(this, userMsg)
			return
		}

		val dialog = WaitingDialog(this, R.string.POPUP_getCodeFromAzureService)
		CoroutineScope(IO).launch {
			val responseData : ServerSetCodeResponse? = CodeServerApi.setCode(this@RBLIKCodeCreator, transferData)
			withContext(Main){
				dialog.hide()
				if(responseData != null)
					ActivityStarter.startDisplayActivity(this@RBLIKCodeCreator, responseData)
			}
		}
	}
	private suspend fun getListOfAccountsForSpinner() : SpinnerAdapter?{
		val errorBase = "[fillListOfAccounts/${this.javaClass.name}]"

		if(!tokenCpy.fillTokenAccountsWithBankDetails(this)){
			Log.e(TagProduction, "$errorBase token cant obtain accounts Details")
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
				Utilities.showToast(this@RBLIKCodeCreator, errorCodeTextToDisplay)
			}
			return null
		}

		val listOfAccountFromToken = tokenCpy.getListOfAccountsNumbersToDisplay()
		if(listOfAccountFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "$errorBase token returned nullOrEmpty accountList")
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
				Utilities.showToast(this@RBLIKCodeCreator, errorCodeTextToDisplay)
			}
			return null
		}

		val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item)
		listOfAccountFromToken.forEach{
			if(it.contains("PLN"))
				adapter.add(it)
		}

		return adapter
	}
	private fun validateDataToGenRBlikCode() : Boolean{
		val accountChosen = accountListSpinner.selectedItemPosition != Spinner.INVALID_POSITION
		if(!accountChosen){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_acc_not_chosen)
			Utilities.showToast(this, textToShow)
			return false
		}

		val receiverName = findViewById<EditText>(R.id.RBlikCodeGenerator_reciverName_EditText).text.toString()
		val receiverNameOk = receiverName.length in 3..50
		if(!receiverNameOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_wrong_receiver_name)
			Utilities.showToast(this, textToShow)
			return false
		}

		val amountText = findViewById<EditText>(R.id.RBlikCodeGenerator_amount_editText).text.toString()
		if(amountText.isEmpty()){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_Amount_zero)
			Utilities.showToast(this, textToShow)
			return false
		}

		val amountOk = amountText.toDouble() > 0.0
		if(!amountOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_Amount_zero)
			Utilities.showToast(this, textToShow)
			return false
		}


		val titleText = findViewById<EditText>(R.id.RBlikCodeGenerator_transferTitle_EditText).text.toString()
		val titleOk = titleText.replace(" ","").length in 3..50
		if(!titleOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_wrong_title)
			Utilities.showToast(this, textToShow)
			return false
		}

		return true
	}
	private fun getDataForServer() : TransferData? {
		val paymentAccount = getPaymentAccountInfoOfSelectedOneByUser()
		if(paymentAccount == null){
			val msg = this.resources.getString(R.string.UserMsg_UNKNOWN_ERROR)
			Utilities.showToast(this, msg)
			return null
		}

		val transferData = TransferData()
		with(transferData){
			amount = amountField.text.toString().toDouble()
			description = titleField.text.toString()
			currency = paymentAccount.getCurrencyOfAccount()
			receiverAccNumber = paymentAccount.getAccNumber()
			receiverName = receiverNameField.text.toString()
			senderAccName = null
			senderAccNumber = null
		}
		return transferData
	}
	private fun wookieTestFillWidgets(){
		//amountField.text = Editable.Factory.getInstance().newEditable("10.0")
		//titleField.text = Editable.Factory.getInstance().newEditable("xyz")
	}
	private suspend fun getTokenCpy() : Boolean{
		val tokenTmp = PreferencesOperator.getToken(this)
		val tokenOk = tokenTmp.isOk(this)
		if(!tokenOk)
			return false

		tokenCpy = tokenTmp
		return true
	}
	private fun getPaymentAccountInfoOfSelectedOneByUser() : PaymentAccount?{
		val currentlySelectedSpinnerItem =  accountListSpinner.selectedItem.toString()
		val accountNumber = tokenCpy.getAccountNbrByDisplayStr(currentlySelectedSpinnerItem)?: return null
		val paymentAccount = tokenCpy.getPaymentAccount(accountNumber)
		return if(paymentAccount != null)
			paymentAccount
		else{
			Log.e(TagProduction, "[getPaymentAccountInfoOfSelectedOneByUser/${this.javaClass.name}] Recived payment account is null")
			return null
		}
	}
	private fun fillReceiverName(){
		val paymentAccount = getPaymentAccountInfoOfSelectedOneByUser()
		if(paymentAccount == null){
			Log.e(TagProduction, "[fillReceiverName/${this.receiverNameField.javaClass.name}] Failed to obtain reciever name, paymentAcc is null")
			return
		}
		val ownerName = paymentAccount.getOwnerName()
		val ownerNameAsEditable = Utilities.strToEditable(ownerName)
		receiverNameField.text = ownerNameAsEditable
	}
}