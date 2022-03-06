package com.example.omega

import android.app.Activity
import android.content.Intent
import android.net.Uri

class ActivityStarter {
	companion object{
		fun startActToSetPinIfTheresNoSavedPin(activity: Activity){
			val pinAlreadySet = Utilites.checkIfAppHasAlreadySetPin(activity)
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
			val transferIntent = Intent(activity,BasicTransferActivity::class.java)
			activity.startActivity(transferIntent)
		}
		fun startResultActivity(activity: Activity, textIdToDisplay: Int){
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
			var preferredMethodeCode =
				Utilites.readPref_Int(context, R.integer.PREF_preferedAuthMethode)
			if(forcedMethodeCode != null && forcedMethodeCode in 0..2)
				preferredMethodeCode = forcedMethodeCode
			val preferredMethodeName = when(preferredMethodeCode){
				0->context.getString(R.string.GUI_selectAuthMethodeText_pin)
				1->context.getString(R.string.GUI_selectAuthMethodeText_pattern)
				2->context.getString(R.string.GUI_selectAuthMethodeText_finger)
				else ->context.getString(R.string.GUI_selectAuthMethodeText_pin)
			}
			when(preferredMethodeName){
				context.getString(R.string.GUI_selectAuthMethodeText_pin) -> authByPin(context,description)
				context.getString(R.string.GUI_selectAuthMethodeText_pattern) -> Utilites.authByPattern(
					context,
					description
				)
				context.getString(R.string.GUI_selectAuthMethodeText_finger) -> authByFingerPrint(context,description)
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
		fun openBrowserForLogin(activity: Activity, authUrl : String, expectedState: String){
			 val intent = Intent(activity, OAuth::class.java)
			 val uriField = activity.resources.getString(R.string.ACT_COM_WEBVIEW_URI_FIELDNAME)
			 val stateField = activity.resources.getString(R.string.ACT_COM_WEBVIEW_STATE_FIELDNAME)
			 val returnCode = activity.resources.getInteger(R.integer.ACT_RETCODE_WEBVIEW)

			 intent.putExtra(uriField,authUrl)
			 intent.putExtra(stateField,expectedState)

			 activity.startActivityForResult(intent,returnCode)
		}
	}
}