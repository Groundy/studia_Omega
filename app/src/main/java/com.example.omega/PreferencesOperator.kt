package com.example.omega

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import com.example.omega.Utilities.Companion.TagProduction

class PreferencesOperator : Application() {
	companion object{
		private fun clearPreferences(activity: Activity, vararg fields : Int){
			val preferencesFieldsStr = arrayOf(
				R.string.PREF_authURL,
				R.string.PREF_authCode,
				R.string.PREF_lastRandomValue,
				R.string.PREF_lastUsedPermissionsForAuth,
				R.string.PREF_authUrlValidityTimeEnd,
				R.string.PREF_Token
				)

			val preferencesFieldsBool = arrayOf(
				R.bool.PREF_authUrlAlreadyUSed)

			fields.forEach {
				if (preferencesFieldsStr.contains(it))
					savePref(activity, it, String())
			}
			preferencesFieldsBool.forEach {
				if (preferencesFieldsStr.contains(it))
					savePref(activity, it, false)
			}
		}
		fun clearAuthData(activity: Activity){
			clearPreferences(activity,
				R.string.PREF_authURL,
				R.string.PREF_lastRandomValue,
				R.string.PREF_authCode,
				R.string.PREF_authUrlValidityTimeEnd,
				R.string.PREF_lastUsedPermissionsForAuth,
				R.bool.PREF_authUrlAlreadyUSed,
				R.string.PREF_Token
			)
		}
		fun developerShowPref(activity: Activity){
			val preferencesFields = arrayOf(
				R.string.PREF_authURL,
				R.string.PREF_authCode,
				R.string.PREF_lastRandomValue,
				R.string.PREF_lastUsedPermissionsForAuth,
				R.string.PREF_authUrlValidityTimeEnd
			)

			preferencesFields.forEach {
				val str = readPrefStr(activity, it)
				val t = activity.getString(it)
				val hg = "$t ---> $str"
				Log.i(TagProduction, hg)
			}
		}
		fun getToken(callActivity: Activity) : Token{
			val tokenCpy = try {
				val tokenStr = readPrefStr(callActivity, R.string.PREF_Token)
				val tokenJsonObj = JSONObject(tokenStr)
				Token(tokenJsonObj)
			}catch (e : Exception){
				Token()
			}
			return tokenCpy
		}
		private fun getSharedProperties(activity: Activity) : SharedPreferences {
			val fileName = activity.getString(R.string.preference_file_key)
			val sharedPrefObj = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE)
			return sharedPrefObj
		}

		fun savePref(activity: Activity, strResourceId : Int, value : Int){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putInt(fieldName,value)
			editor.apply()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : Boolean){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putBoolean(fieldName,value)
			editor.apply()
		}
		fun savePref(activity: Activity, strResourceId : Int, value : String){
			val fieldName = activity.getString(strResourceId)
			val editor = getSharedProperties(activity).edit()
			editor.putString(fieldName,value)
			editor.apply()
		}

		fun readPrefBool(activity: Activity, strResourceId : Int) : Boolean{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getBoolean(fieldName,false)
		}
		fun readPrefInt(activity: Activity, strResourceId : Int) : Int{
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getInt(fieldName,0)
		}
		fun readPrefStr(activity: Activity, strResourceId : Int) : String {
			val fieldName = activity.getString(strResourceId)
			val prefs = getSharedProperties(activity)
			return prefs.getString(fieldName, "")!!
		}
	}
}
