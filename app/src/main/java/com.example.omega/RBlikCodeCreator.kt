package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

class RBLIKCodeCreator : AppCompatActivity() {
	private lateinit var amountField : EditText
	private lateinit var titleField : EditText
	private lateinit var receiverNameField : EditText
	private lateinit var accountListSpinner : Spinner
	private lateinit var goNextActivityButton : Button
	private var userAccountsAvailable = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rblik_code_creator)
		userAccountsAvailable = ApiGetPaymentAccDetails.run()
		setUpGui()
		fillListOfAccounts()
		DEVELOPER_fillWidgets()
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
			return//todo

		val data = serializeDataForServer()
		val codeAssociated = getCodeFromServer(data)
		if(codeAssociated == null)
			return//todo

		openDisplayActivityWithCode(codeAssociated)
	}
	private fun fillListOfAccounts(){
		if(!userAccountsAvailable){
			val errorCodeTextToDisplay = getString(R.string.UserMsg_basicTransfer_error_reciving_acc_balance)
			Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return
		}
		val listOfAccountFromToken = UserData.accessTokenStruct?.listOfAccounts!!
		val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item)
		listOfAccountFromToken.forEach{
			adapter.add(it.getDisplayString())
		}
		accountListSpinner.adapter = adapter
	}
	private fun validateDataToGenRBlikCode() : Boolean{
		val accountChosen = true//todo
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
		return Utilities.getRandomTestCode()      //todo
	}
	private fun serializeDataForServer() : TransferData {
		val amount = amountField.text.toString().toDouble()
		val title = titleField.text.toString()
		val currency = "PLN"//todo
		val receiverAccNumberAcc = "0123456789012345678901234567" //todo
		val receiverName = receiverNameField.text.toString()
		return TransferData(null, receiverAccNumberAcc, receiverName, title, amount, currency)
	}

	private fun openDisplayActivityWithCode(codeFromServer : Int){
		val codeDisplayIntent = Intent(this, RBlikCodeDisplayActivity::class.java)
		codeDisplayIntent.putExtra(getString(R.string.ACT_COM_CODEGENERATOR_CODE_FOR_DISPLAY_FIELDNAME), codeFromServer)
		this.startActivity(codeDisplayIntent)
	}
	private fun DEVELOPER_fillWidgets(){
		amountField.text = Editable.Factory.getInstance().newEditable("aaa")
		titleField.text = Editable.Factory.getInstance().newEditable("aaa")
		receiverNameField.text = Editable.Factory.getInstance().newEditable(12345.toString())
	}
}