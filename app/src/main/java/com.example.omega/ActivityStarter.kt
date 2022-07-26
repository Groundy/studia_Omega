package com.example.omega

import android.app.Activity
import android.content.Intent

class ActivityStarter {
	companion object{
		fun startResetPermissionsActivity(activity: Activity){
			val intent = Intent(activity, UserPermissionList::class.java)
			val retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_PERMISSION_LIST)
			activity.startActivityForResult(intent,retCode)
		}
		fun startActToSetPinIfTheresNoSavedPin(activity: Activity){
			val pinAlreadySet = Utilities.checkIfAppHasAlreadySetPin(activity)
			if(!pinAlreadySet){
				val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)

				val pinActPurpose = activity.resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[0]
				val pinActPurposeFieldName = activity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
				pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)

				val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN_SET)
				activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
			}
		}
		fun startConfigurationActivity(activity: Activity){
			val settingsActivityIntent = Intent(activity, SettingsActivity::class.java)
			activity.startActivity(settingsActivityIntent)
		}
		fun startTransferActivityFromMenu(activity: Activity){
			val transferIntent = Intent(activity, BasicTransferActivity::class.java)
			activity.startActivity(transferIntent)
		}
		fun startRBlikCodeCreatorActivity(activity: Activity){
			val rblikCodeCreatorIntent = Intent(activity, RBLIKCodeCreator::class.java)
			activity.startActivity(rblikCodeCreatorIntent)
		}
		fun startOperationResultActivity(activity: Activity, textIdToDisplay: Int){
			val resultIntent = Intent(activity, ResultActivity::class.java)
			val textToDisplay = activity.resources.getString(textIdToDisplay)
			val textFieldName = activity.resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME)
			resultIntent.putExtra(textFieldName,textToDisplay)
			activity.startActivity(resultIntent)
		}
		fun startTransferSummaryActivity(activity: Activity, serializedTransferDataObj: String){
			val resultIntent = Intent(activity, TransferSummary::class.java)
			val serializedObjField = activity.getString(R.string.TransferSummary_COM_serializedData)
			resultIntent.putExtra(serializedObjField,serializedTransferDataObj)
			activity.startActivity(resultIntent)
		}
		fun startAuthActivity(context : Activity, description : String?, forcedMethodeCode : Int?){
			val fingerCode = 1

			var preferredMethodeCode =
				PreferencesOperator.readPrefInt(context, R.integer.PREF_preferedAuthMethode)
			
			if(forcedMethodeCode != null && forcedMethodeCode in 0..1)
				preferredMethodeCode = forcedMethodeCode

			when(preferredMethodeCode){
				fingerCode -> authByFingerPrint(context,description)
				else -> authByPin(context,description)
			}
		}
		private fun authByPin(activity: Activity, description : String?){
			val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val pinActPurpose = activity.resources.getStringArray(R.array.ACT_COM_PIN_ACT_PURPOSE)[1]
			val pinActPurposeFieldName = activity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
			pinActivityActivityIntent.putExtra(descriptionFieldName,description)
			pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)
			activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
		}
		private fun authByFingerPrint(activity: Activity, description : String?){
			val scanFingerActivityIntent = Intent(activity, ScanFingerActivity::class.java)
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			scanFingerActivityIntent.putExtra(descriptionFieldName,description)
			val retCodeForActivity  = activity.resources.getInteger(R.integer.ACT_RETCODE_FINGER)
			activity.startActivityForResult(scanFingerActivityIntent, retCodeForActivity)
		}
		fun openBrowserForLogin(activity: Activity){
			 val intent = Intent(activity, BankLoginWebPageActivity::class.java)
			 val returnCode = activity.resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW)
			 activity.startActivityForResult(intent,returnCode)
		}
		fun openDialogWithDefinedPurpose(activity: Activity, purpose : YesNoDialogActivity.Companion.DialogPurpose){
			val yesNoDialog = Intent(activity, YesNoDialogActivity::class.java)
			var msgToDisplay = String()
			var retCode = 0
			when(purpose){
				YesNoDialogActivity.Companion.DialogPurpose.CancelBioAuth ->{
					retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_CancelBioAuth)
					msgToDisplay = activity.getString(R.string.DIALOG_GUI_CancelBioAuthMsgText)
				}
				YesNoDialogActivity.Companion.DialogPurpose.ResetAuthUrl ->{
					retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_ChangeAccountOnBankWebPage)
					msgToDisplay = activity.getString(R.string.DIALOG_GUI_AskForChangeOptionsForAccountsInBankWebPage_TEXT)

				}
				YesNoDialogActivity.Companion.DialogPurpose.LoginToBankAccount ->{
					retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_DIALOG_userWantToLoginToBank)
					msgToDisplay = activity.getString(R.string.DIALOG_GUI_AskUserIfHeWantToLoginToBank_TEXT)
				}
			}
			val field = activity.getString(R.string.ACT_COM_DIALOG_TextToDisplay_FIELDNAME)
			yesNoDialog.putExtra(field,msgToDisplay)
			activity.startActivityForResult(yesNoDialog, retCode)
		}
	}
}