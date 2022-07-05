package omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner


class RBLIKCodeCreator : AppCompatActivity() {
	private lateinit var amountField : EditText
	private lateinit var titleField : EditText
	private lateinit var receiverNameField : EditText
	private lateinit var accountSpinner : Spinner
	private lateinit var goNextActivityButton : Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rblik_code_creator)
		setUpGui()
		DEVELOPER_fillWidgets()
	}
	private fun setUpGui(){
		amountField = findViewById(R.id.RBlikCodeGenerator_amount_editText)
		titleField = findViewById(R.id.RBlikCodeGenerator_transferTitle_EditText)
		receiverNameField = findViewById(R.id.RBlikCodeGenerator_reciverName_EditText)
		accountSpinner = findViewById(R.id.RBlikCodeGenerator_accountList_Spinner)
		goNextActivityButton = findViewById(R.id.RBlikCodeGenerator_goNext_button)


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
					Utilites.stopUserFromPuttingMoreThan2DigitsAfterComma(
						amountField,
						previousValue,
						p0.toString()
					)
			}
		}
		amountField.addTextChangedListener(amountEditTextListener)
	}
	private fun goNextButtonClicked(){
		val dataOk = validateDataToGenBlikCode()
		if(!dataOk)
			return//todo

		val data = serializeDataForServer()
		val codeAssociated = getCodeFromServer(data)
		if(codeAssociated == null){
			return//todo
		}

		openDisplayActivityWithCode(codeAssociated)
	}

	private fun validateDataToGenBlikCode() : Boolean{
		val accountChosen = true//todo
		if(!accountChosen){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_acc_not_chosen)
			Utilites.showToast(this, textToShow)
			return false
		}

		val receiverName = findViewById<EditText>(R.id.RBlikCodeGenerator_reciverName_EditText).text.toString()
		val receiverNameOk = receiverName.length in 3..50
		if(!receiverNameOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_wrong_receiver_name)
			Utilites.showToast(this, textToShow)
			return false
		}

		val amountText = findViewById<EditText>(R.id.RBlikCodeGenerator_amount_editText).text.toString()
		val amountOk = amountText.toDouble() > 0.0
		if(!amountOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_Amount_zero)
			Utilites.showToast(this, textToShow)
			return false
		}


		val titleText = findViewById<EditText>(R.id.RBlikCodeGenerator_transferTitle_EditText).text.toString()
		val titleOk = titleText.length in 3..50
		if(!titleOk){
			val textToShow = getString(R.string.UserMsg_RBlikCodeGenerator_wrong_title)
			Utilites.showToast(this, textToShow)
			return false
		}

		return true
	}
	private fun getCodeFromServer(transferData: TransferData) : Int?{
		return Utilites.getRandomTestCode()        //todo
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

	private fun openDisplayActivityWithCode(codeFromServer : Int){
		val codeDisplayIntent = Intent(this, RBlikCodeDisplayActivity::class.java)
		codeDisplayIntent.putExtra(getString(R.string.ACT_COM_CODEGENERATOR_CODE_FOR_DISPLAY), codeFromServer)
		this.startActivity(codeDisplayIntent)
	}
	private fun DEVELOPER_fillWidgets(){
		amountField.setText("5")
		titleField.setText("rrrr")
		receiverNameField.setText("444")
	}
}