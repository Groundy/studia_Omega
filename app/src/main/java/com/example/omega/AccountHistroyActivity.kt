package com.example.omega

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountHistroyActivity : AppCompatActivity() {
	private lateinit var spinner : Spinner
	private lateinit var list : ListView
	private lateinit var token: Token

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_histroy)
		setGUI()

		val dialog = WaitingDialog(this, R.string.POPUP_getAccountsHistory)
		CoroutineScope(IO).launch{
			val success = getToken()
			if(!success){
				withContext(Main){
					dialog.hide()
					finish()
				}
				return@launch
			}
			val spinnerAdapterTmp = fillListOfAccounts()
			if(spinnerAdapterTmp==null){
				withContext(Main){
					dialog.hide()
				}
				return@launch//todo
			}
			withContext(Main){
				dialog.hide()
				spinner.adapter = spinnerAdapterTmp
			}
		}
	}

	private fun getToken() : Boolean{
		val tokenTmp = PreferencesOperator.getToken(this)
		return if(tokenTmp.isOk(this)) {
			token = tokenTmp
			true
		}
		else{
			Log.e(TagProduction, "[getToken/${this.javaClass.name}] error in obtaing token")
			val userMsg = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
			Utilities.showToast(this@AccountHistroyActivity, userMsg)
			false
		}
	}
	private fun fillListOfAccounts() : SpinnerAdapter?{
		if(!token.fillTokenAccountsWithBankDetails(this)){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token cant obtain accounts Details")
			//val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
			//Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return null
		}

		val listOfAccountsFromToken = token.getListOfAccountsNumbersToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token return null or empty account list")
			//val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
			//Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return null
		}

		val spinneradapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
		listOfAccountsFromToken.forEach{
			spinneradapter.add(it)
		}
		return spinneradapter
	}
	private fun setGUI(){
		val spinnerListener = object : AdapterView.OnItemSelectedListener{
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				userChangedAccount()
			}
			override fun onNothingSelected(p0: AdapterView<*>?) {}
		}

		spinner = findViewById(R.id.AccHistoryActivity_Spinner)
		list = findViewById(R.id.AccHistoryActivity_List)
		spinner.onItemSelectedListener = spinnerListener
		val listAdapter = ArrayAdapter(this@AccountHistroyActivity, android.R.layout.simple_spinner_item, emptyList<String>())
		list.adapter = listAdapter
	}
	private fun userChangedAccount(){
		val itemTxt = spinner.selectedItem.toString()
		val accNumber = token.getAccountNbrByDisplayStr(itemTxt)?: return
		val dialog = WaitingDialog(this, R.string.POPUP_getSingleAccountHistory)
		CoroutineScope(IO).launch{
			val accountHaveHistory = token.fillHistoryToPaymentAccount(this@AccountHistroyActivity, accNumber)
			if(!accountHaveHistory){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to fill PaymentAccount with details")
				dialog.hide()
				return@launch
			}

			val recordListTexts = token.getPaymentAccount(accNumber)?.getListOfAccountHistoryStrings()
			if(recordListTexts==null){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to get hist from payment account")
				dialog.hide()
				return@launch
			}
			val adapterToSet = ArrayAdapter(
				this@AccountHistroyActivity,
				android.R.layout.simple_spinner_item,
				recordListTexts
			)
			withContext(Main){
				list.adapter = adapterToSet
				dialog.hide()
			}
		}

	}
}