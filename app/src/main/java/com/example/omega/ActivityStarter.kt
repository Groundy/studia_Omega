package com.example.omega

import android.app.Activity
import android.content.Intent

class ActivityStarter {
	companion object{
		fun startPinActivity(callerActivity: Activity, purpose : PinActivity.Companion.Purpose, descriptionForAuth: String = String()){
			when(purpose){
				PinActivity.Companion.Purpose.Set->{
					val pinAlreadySet = Utilities.checkIfAppHasAlreadySetPin(callerActivity)
					if(pinAlreadySet)
						return

					val pinActPurpose = PinActivity.Companion.Purpose.Set.text
					val pinActPurposeFieldName = callerActivity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
					val retCodeForActivity = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_PIN_SET)

					val pinActivityActivityIntent = Intent(callerActivity, PinActivity::class.java)
					pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
					callerActivity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
				}
				PinActivity.Companion.Purpose.Auth->{
					val descriptionFieldName = callerActivity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
					val pinActPurpose = PinActivity.Companion.Purpose.Auth.text
					val pinActPurposeFieldName = callerActivity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
					val retCodeForActivity = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)

					val pinActivityActivityIntent = Intent(callerActivity, PinActivity::class.java)
					pinActivityActivityIntent.putExtra(descriptionFieldName,descriptionForAuth)
					pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
					callerActivity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
				}
				PinActivity.Companion.Purpose.Change->{
					val activityReason = PinActivity.Companion.Purpose.Change.text
					val activityReasonFieldName = callerActivity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
					val retCodeForActivity = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_PIN_CHANGE)

					val changePinActivityIntent = Intent(callerActivity, PinActivity::class.java)
					changePinActivityIntent.putExtra(activityReasonFieldName,activityReason)
					callerActivity.startActivityForResult(changePinActivityIntent, retCodeForActivity)
				}
			}
		}
		fun startQrScannerActivity(callerActivity: Activity){
			val retCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_QrScanner)

			val qrScannerActivityIntent = Intent(callerActivity, QrScannerActivity::class.java)
			callerActivity.startActivityForResult(qrScannerActivityIntent, retCode)
		}
		fun startResetPermissionsActivity(callerActivity: Activity){
			val retCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_PERMISSION_LIST)

			val intent = Intent(callerActivity, UserPermissionList::class.java)
			callerActivity.startActivityForResult(intent,retCode)
		}
		fun startConfigurationActivity(callerActivity: Activity){
			val settingsActivityIntent = Intent(callerActivity, SettingsActivity::class.java)
			callerActivity.startActivity(settingsActivityIntent)
		}
		fun startTransferActivity(callerActivity: Activity, transferData: TransferData? = null){
			val retCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_BASIC_TRANSFER_ACT)
			val transferIntent = Intent(callerActivity, BasicTransferActivity::class.java)
			if(transferData!=null){
				val serializedTransferData = transferData.toString()
				val fieldName = callerActivity.resources.getString(R.string.ACT_COM_BASIC_TRANS_INTENT_FIELDNAME)
				transferIntent.putExtra(fieldName, serializedTransferData)
			}

			callerActivity.startActivityForResult(transferIntent, retCode)
		}
		fun startRBlikCodeCreatorActivity(callerActivity: Activity){
			val rblikCodeCreatorIntent = Intent(callerActivity, RBLIKCodeCreator::class.java)
			val retCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_RBLIK_CREATOR)

			callerActivity.startActivityForResult(rblikCodeCreatorIntent, retCode)
		}
		fun startOperationResultActivity(callerActivity: Activity, textToDisplayId: Int){
			val textToDisplay = callerActivity.resources.getString(textToDisplayId)
			val textFieldName = callerActivity.resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME)

			val resultIntent = Intent(callerActivity, ResultActivity::class.java)
			resultIntent.putExtra(textFieldName,textToDisplay)
			callerActivity.startActivity(resultIntent)
		}
		fun startOperationResultActivity(callerActivity: Activity, textIdToDisplay: String){
			val textFieldName = callerActivity.resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME)

			val resultIntent = Intent(callerActivity, ResultActivity::class.java)
			resultIntent.putExtra(textFieldName,textIdToDisplay)
			callerActivity.startActivity(resultIntent)
		}
		fun startTransferSummaryActivity(callerActivity: Activity, transferData: TransferData){
			val serializedObjField = callerActivity.getString(R.string.TransferSummary_COM_serializedData)
			val resultCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_TRANSFER_SUMMARY)

			val resultIntent = Intent(callerActivity, TransferSummary::class.java)
			resultIntent.putExtra(serializedObjField,transferData.toString())
			callerActivity.startActivityForResult(resultIntent, resultCode)
		}
		fun startAuthActivity(context : Activity, description : String?, forcedMethodeCode : Int? = null){
			val fingerCode = 1

			var preferredMethodeCode =
				PreferencesOperator.readPrefInt(context, R.integer.PREF_preferedAuthMethode)
			
			if(forcedMethodeCode != null && forcedMethodeCode in 0..1)
				preferredMethodeCode = forcedMethodeCode

			when(preferredMethodeCode){
				fingerCode -> authByFingerPrint(context,description)
				else -> startPinActivity(context, PinActivity.Companion.Purpose.Auth ,description!!)
			}
		}
		private fun authByFingerPrint(callerActivity: Activity, description : String?){
			val descriptionFieldName = callerActivity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val retCodeForActivity  = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_FINGER)

			val scanFingerActivityIntent = Intent(callerActivity, ScanFingerActivity::class.java)
			scanFingerActivityIntent.putExtra(descriptionFieldName,description)
			callerActivity.startActivityForResult(scanFingerActivityIntent, retCodeForActivity)
		}
		fun openBrowserForLogin(callerActivity: Activity, redirect : BankLoginWebPageActivity.Companion.WebActivtyRedirect){
			val redirectField = callerActivity.resources.getString(R.string.ACT_COM_WEBVIEW_REDIRECT_FIELD_NAME)
			val returnCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW)

			val intent = Intent(callerActivity, BankLoginWebPageActivity::class.java)
			intent.putExtra(redirectField, redirect.text)
			callerActivity.startActivityForResult(intent,returnCode)
		}
		fun openDialogWithDefinedPurpose(callerActivity: Activity, purpose : YesNoDialogActivity.Companion.DialogPurpose){
			val yesNoDialog = Intent(callerActivity, YesNoDialogActivity::class.java)
			val retCode = when(purpose){
				YesNoDialogActivity.Companion.DialogPurpose.CancelBioAuth ->
					callerActivity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_CancelBioAuth)
				YesNoDialogActivity.Companion.DialogPurpose.ResetAuthUrl ->
					callerActivity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_ChangeAccountOnBankWebPage)
				YesNoDialogActivity.Companion.DialogPurpose.LoginToBankAccount ->
					callerActivity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_userWantToLoginToBank)
				YesNoDialogActivity.Companion.DialogPurpose.ResetPin ->
					callerActivity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_ResetPin)
				else->
					0
			}

			val purposeField = callerActivity.getString(R.string.ACT_COM_DIALOG_PURPOSE_FIELDNAME)
			yesNoDialog.putExtra(purposeField,purpose.text)
			callerActivity.startActivityForResult(yesNoDialog, retCode)
		}
		fun openAccountTransfersHistoryActivity(callerActivity: Activity) {
			val intent = Intent(callerActivity, AccountHistroyActivity::class.java)
			callerActivity.startActivity(intent)
		}
		fun startDisplayActivity(callerActivity: Activity, data : ServerSetCodeResponse, multipleUseCode : Boolean){
			val servResField = callerActivity.getString(R.string.ACT_COM_CODEGENERATOR_SERIALIZED_SERVER_RES_FIELD)
			val multiUseCodeField = callerActivity.getString(R.string.ACT_COM_CODEGENERATOR_multiUseCode_FIELD)
			val retCode = callerActivity.resources.getInteger(R.integer.ACT_RETCODE_DISPLAY_ACTIVITY)
			val dataStr = data.toString()
			val codeDisplayIntent = Intent(callerActivity, RBlikCodeDisplayActivity::class.java)

			codeDisplayIntent.putExtra(servResField, dataStr)
			codeDisplayIntent.putExtra(multiUseCodeField, multipleUseCode)
			callerActivity.startActivityForResult(codeDisplayIntent, retCode)
		}
	}
}