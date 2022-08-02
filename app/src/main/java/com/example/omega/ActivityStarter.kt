package com.example.omega

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult

class ActivityStarter {
	companion object{
		fun startQrScannerActivity(activity: Activity){
			val retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_QrScanner)

			val qrScannerActivityIntent = Intent(activity, QrScannerActivity::class.java)
			activity.startActivityForResult(qrScannerActivityIntent, retCode)
		}
		fun startResetPermissionsActivity(activity: Activity){
			val retCode = activity.resources.getInteger(R.integer.ACT_RETCODE_PERMISSION_LIST)

			val intent = Intent(activity, UserPermissionList::class.java)
			activity.startActivityForResult(intent,retCode)
		}
		fun startActToSetPinIfTheresNoSavedPin(activity: Activity){
			val pinAlreadySet = Utilities.checkIfAppHasAlreadySetPin(activity)
			if(pinAlreadySet)
				return

			val pinActPurpose = PinActivity.Companion.Purpose.Set.text
			val pinActPurposeFieldName = activity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN_SET)

			val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)
			pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
			activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)

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
			val textToDisplay = activity.resources.getString(textIdToDisplay)
			val textFieldName = activity.resources.getString(R.string.ACT_COM_RESULT_TEXT_FIELD_NAME)

			val resultIntent = Intent(activity, ResultActivity::class.java)
			resultIntent.putExtra(textFieldName,textToDisplay)
			activity.startActivity(resultIntent)
		}
		fun startTransferSummaryActivity(activity: Activity, serializedTransferDataObj: String){
			val serializedObjField = activity.getString(R.string.TransferSummary_COM_serializedData)

			val resultIntent = Intent(activity, TransferSummary::class.java)
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
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val pinActPurpose = PinActivity.Companion.Purpose.Auth.text
			val pinActPurposeFieldName = activity.resources.getString(R.string.ACT_COM_PIN_ACT_PURPOSE_FIELDNAME)
			val retCodeForActivity = activity.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)

			val pinActivityActivityIntent = Intent(activity, PinActivity::class.java)
			pinActivityActivityIntent.putExtra(descriptionFieldName,description)
			pinActivityActivityIntent.putExtra(pinActPurposeFieldName,pinActPurpose)
			activity.startActivityForResult(pinActivityActivityIntent, retCodeForActivity)
		}
		private fun authByFingerPrint(activity: Activity, description : String?){
			val descriptionFieldName = activity.resources.getString(R.string.ACT_COM_TRANSACTION_DETAILS_FIELD_NAME)
			val retCodeForActivity  = activity.resources.getInteger(R.integer.ACT_RETCODE_FINGER)

			val scanFingerActivityIntent = Intent(activity, ScanFingerActivity::class.java)
			scanFingerActivityIntent.putExtra(descriptionFieldName,description)
			activity.startActivityForResult(scanFingerActivityIntent, retCodeForActivity)
		}
		fun openBrowserForLogin(activity: Activity, redirect : BankLoginWebPageActivity.Companion.WebActivtyRedirect){
			val redirectField = activity.resources.getString(R.string.ACT_COM_WEBVIEW_REDIRECT_FIELD_NAME)
			val returnCode = activity.resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW)

			val intent = Intent(activity, BankLoginWebPageActivity::class.java)
			intent.putExtra(redirectField, redirect.text)
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
		fun openAccountTransfersHistoryActivity(callerActivity: Activity) {
			val intent = Intent(callerActivity, AccountHistroyActivity::class.java)
			callerActivity.startActivity(intent)
		}
	}
}