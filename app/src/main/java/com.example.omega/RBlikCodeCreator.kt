package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.omega.Utilities.Companion.TagProduction

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
		getTokenCpy()
		setUpGui()
		fillListOfAccounts()
		fillReceiverName()
		if(Utilities.developerMode)
			developerFillWidgets()
	}
	private fun setUpGui(){
		amountField = findViewById(R.id.RBlikCodeGenerator_amount_editText)
		titleField = findViewById(R.id.RBlikCodeGenerator_transferTitle_EditText)
		receiverNameField = findViewById(R.id.RBlikCodeGenerator_reciverName_EditText)
		accountListSpinner = findViewById(R.id.RBlikCodeGenerator_accountList_Spinner)
		goNextActivityButton = findViewById(R.id.RBlikCodeGenerator_goNext_button)
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

		val data = serializeDataForServer()
		if(data == null){
			val userMsg = getString(R.string.UserMsg_UNKNOWN_ERROR)
			Utilities.showToast(this, userMsg)
			return
		}

		val codeAssociated = getCodeFromServer(data)
		if(codeAssociated == null){
			Log.e(TagProduction,"[getCodeFromServer/${this.javaClass.name}] server returned null or wrong fromat code for QR generator")
			return
		}

		openDisplayActivityWithCode(codeAssociated)
	}
	private fun fillListOfAccounts(){
		if(!tokenCpy.getDetailsOfAccountsFromBank()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token cant obtain accounts Details")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return
		}

		val listOfAccountFromToken = tokenCpy.getListOfAccountsToDisplay()
		if(listOfAccountFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}] token returned nullOrEmpty accountList")
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return //maybe
		}

		val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item)
		listOfAccountFromToken.forEach{
			adapter.add(it)
		}
		accountListSpinner.adapter = adapter
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
		val amountOk = amountText.toDouble() > 0.0
		if(!amountOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_Amount_zero)
			Utilities.showToast(this, textToShow)
			return false
		}


		val titleText = findViewById<EditText>(R.id.RBlikCodeGenerator_transferTitle_EditText).text.toString()
		val titleOk = titleText.length in 3..50
		if(!titleOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_wrong_title)
			Utilities.showToast(this, textToShow)
			return false
		}

		return true
	}
	private fun getCodeFromServer(transferData: TransferData) : Int?{
		//todo implement
		return Utilities.getRandomTestCode()
	}
	private fun serializeDataForServer() : TransferData? {
		val paymentAccount = getPaymentAccountInfoOfSelectedOneByUser() ?: return null
		val amount = amountField.text.toString().toDouble()
		val title = titleField.text.toString()
		val currency = paymentAccount.getCurrencyOfAccount()
		val receiverAccNumberAcc = paymentAccount.getAccNumber()
		val receiverName = receiverNameField.text.toString()
		return TransferData(null, receiverAccNumberAcc, receiverName, title, amount, currency)
	}

	private fun openDisplayActivityWithCode(codeFromServer : Int){
		val codeDisplayIntent = Intent(this, RBlikCodeDisplayActivity::class.java)
		codeDisplayIntent.putExtra(getString(R.string.ACT_COM_CODEGENERATOR_CODE_FOR_DISPLAY_FIELDNAME), codeFromServer)
		this.startActivity(codeDisplayIntent)
	}
	private fun developerFillWidgets(){
		amountField.text = Editable.Factory.getInstance().newEditable("10.0")
		titleField.text = Editable.Factory.getInstance().newEditable("xyz")
	}
	private fun getTokenCpy(){
		val tokenTmp = PreferencesOperator.getToken(this)
		if(!tokenTmp.isOk())
			finish()
		else
			tokenCpy = tokenTmp
	}
	private fun getPaymentAccountInfoOfSelectedOneByUser() : PaymentAccount?{
		val currentlySelectedSpinnerItem =  accountListSpinner.selectedItem.toString()
		val pattern = "]  "
		if(!currentlySelectedSpinnerItem.contains(pattern)){
			Log.e(TagProduction, "[getPaymentAccountInfoOfSelectedOneByUser/${this.javaClass.name}] Could not get acc number from spinnerr selected item text")
			return null
		}

		val parts = currentlySelectedSpinnerItem.split(pattern)
		if(parts.size != 2){
			Log.e(TagProduction, "[getPaymentAccountInfoOfSelectedOneByUser/${this.javaClass.name}] Text from selected item is in wrong format")
			return null
		}

		val accountNumber = parts[1]
		val paymentAccount = tokenCpy.getPaymentAccount(accountNumber)
		return if(paymentAccount != null)
			paymentAccount
		else{
			Log.e(TagProduction, "[getPaymentAccountInfoOfSelectedOneByUser/${this.javaClass.name}] Recived payment account is null")
			return null
		}
	}
	private fun fillReceiverName(){
		val paymentAccount = getPaymentAccountInfoOfSelectedOneByUser() ?: return
		if(paymentAccount == null){
			Log.e(TagProduction, "[fillReceiverName/${this.receiverNameField.javaClass.name}] Failed to obtain reciever name, paymentAcc is null")
			return
		}
		val ownerName = paymentAccount.getOwnerName()
		val ownerNameAsEditable =  Editable.Factory.getInstance().newEditable(ownerName)
		receiverNameField.text = ownerNameAsEditable
	}
}