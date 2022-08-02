package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView

class YesNoDialogActivity : AppCompatActivity() {
	companion object{
		enum class DialogPurpose(val text : String){
			CancelBioAuth("CancelBioAuth"), ResetAuthUrl("ResetAuthUrl"), LoginToBankAccount("LoginToBankAccount"), ResetPin("ResetPin"), None("None");
			companion object{
				fun fromStr(str : String) : DialogPurpose{
					return when(str){
						"CancelBioAuth" -> CancelBioAuth
						"ResetAuthUrl" -> ResetAuthUrl
						"LoginToBankAccount" ->LoginToBankAccount
						"ResetPin" -> ResetPin
						else ->None
					}
				}
			}

		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_yes_no_dialog)
		setFinishOnTouchOutside(false)
		setListenersToButtons()
		setText()
	}

	private fun setListenersToButtons(){
		findViewById<TextView>(R.id.DialogAct_OK_Button).setOnClickListener {
			setResult(RESULT_OK, Intent())
			finish()
		}
		findViewById<TextView>(R.id.DialogAct_No_Button).setOnClickListener {
			setResult(RESULT_CANCELED, Intent())
			finish()
		}
	}
	private fun setText(){
		val purposeFieldName = getString(R.string.ACT_COM_DIALOG_PURPOSE_FIELDNAME)
		val purposeStr = intent.getStringExtra(purposeFieldName)
		if(purposeStr == null){
			Log.e(Utilities.TagProduction, "[setText/${this.javaClass.name}] Error in parsing dialog purpose str")
			return
		}

		val purpose = DialogPurpose.fromStr(purposeStr)
		val mainTextToDisplay = when(purpose){
			DialogPurpose.CancelBioAuth ->
				getString(R.string.DIALOG_GUI_CancelBioAuthMsgText)
			DialogPurpose.ResetAuthUrl ->
				getString(R.string.DIALOG_GUI_AskForChangeOptionsForAccountsInBankWebPage_TEXT)
			DialogPurpose.LoginToBankAccount ->
				getString(R.string.DIALOG_GUI_AskUserIfHeWantToLoginToBank_TEXT)
			DialogPurpose.ResetPin ->
				getString(R.string.DIALOG_GUI_ResetPin_Text)
			else->{
				Log.e(Utilities.TagProduction, "[setText/${this.javaClass.name}] Error in parsing dialog purpose str, its not null but it doesnt fit")
				"Błąd"
			}
		}
		findViewById<TextView>(R.id.DialogAct_TextView).text = mainTextToDisplay
	}

}