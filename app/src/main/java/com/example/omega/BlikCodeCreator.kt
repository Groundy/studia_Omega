package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner


class BlikCodeCreator : AppCompatActivity() {
	private lateinit var amountField : EditText
	private lateinit var titleField : EditText
	private lateinit var receiverNameField : EditText
	private lateinit var accountSpinner : Spinner
	private lateinit var goNextActivityButton : Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_blik_code_creator)
		setUpGui()
	}
	private fun setUpGui(){
		amountField = findViewById(R.id.BlikCodeGenerator_amount_editText)
		titleField = findViewById(R.id.BlikCodeGenerator_transferTitle_EditText)
		receiverNameField = findViewById(R.id.BlikCodeGenerator_reciverName_EditText)
		accountSpinner = findViewById(R.id.BlikCodeGenerator_accountList_Spinner)
		goNextActivityButton = findViewById(R.id.BlikCodeGenerator_goNext_button)


		goNextActivityButton.setOnClickListener(){
			goNextButtonClicked()
		}


		val amountEditTextListener = object : TextWatcher {
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0!!.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(p0 != null)
					Utilites.stopUserFromPuttingMoreThan2DigitsAfterComma(amountField, previousValue, p0.toString())
			}
		}
		amountField.addTextChangedListener(amountEditTextListener)
	}
	private fun goNextButtonClicked(){
		val dataOk = validateDataToGenBlikCode()
		if(!dataOk)
			return

		val data = serializeDataForServer()
		val codeAssociated = getCodeFromServer(data)
	}

	private fun validateDataToGenBlikCode() : Boolean{
		val accountChosen = true//todo
		if(!accountChosen){
			val textToShow = getString(R.string.USER_MSG_BlikCodeGenerator_acc_not_chosen)
			Utilites.showToast(this,textToShow)
			return false
		}

		val receiverName = findViewById<EditText>(R.id.BlikCodeGenerator_reciverName_EditText).text.toString()
		val receiverNameOk = receiverName.length in 3..50
		if(!receiverNameOk){
			val textToShow = getString(R.string.USER_MSG_BlikCodeGenerator_wrong_receiver_name)
			Utilites.showToast(this,textToShow)
			return false
		}

		val amountText = findViewById<EditText>(R.id.BlikCodeGenerator_Amount_TextView).text.toString()
		val amountOk = amountText.toDouble() > 0.0
		if(!amountOk){
			val textToShow = getString(R.string.USER_MSG_BlikCodeGenerator_Amount_zero)
			Utilites.showToast(this,textToShow)
			return false
		}


		val titleText = findViewById<EditText>(R.id.BlikCodeGenerator_transferTitle_EditText).text.toString()
		val titleOk = titleText.length in 3..50
		if(!titleOk){
			val textToShow = getString(R.string.USER_MSG_BlikCodeGenerator_wrong_title)
			Utilites.showToast(this,textToShow)
			return false
		}

		return true
	}

	private fun getCodeFromServer(transferData: TransferData) : Int?{
		//todo
		return 123456
	}
	private fun serializeDataForServer() : TransferData {
		val amount = amountField.text.toString().toDouble()
		val title = titleField.text.toString()
		val currency = "PLN"//todo
		val receiverAccNumberAcc = "0123456789012345678901234567" //todo
		val receiverName = receiverNameField.text.toString()
		val data = TransferData(null, receiverAccNumberAcc, receiverName, title, amount, currency)
		return data
	}
}