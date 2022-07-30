package com.example.omega

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.Utilities.Companion.TagProduction

class AccountHistroyActivity : AppCompatActivity() {
	private lateinit var spinner : Spinner
	private lateinit var list : ListView
	private lateinit var token: Token

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_histroy)
		if(!getToken()){
			finish()
			return
		}
		setGUI()
		fillListOfAccounts()
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
			Utilities.showToast(this, userMsg)
			false
		}
	}
	private fun fillListOfAccounts(){
		if(!token.getDetailsOfAccountsFromBank(this)){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token cant obtain accounts Details")
			val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
			Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return
		}

		val listOfAccountsFromToken = token.getListOfAccountsToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token return null or empty account list")
			val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
			Utilities.showToast(this, errorCodeTextToDisplay)
			finish()
			return
		}

		val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
		listOfAccountsFromToken.forEach{
			adapter.add(it)
		}
		spinner.adapter = adapter
		userChangedAccount()
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
	}
	private fun userChangedAccount(){
		val itemTxt = spinner.selectedItem.toString()
		val accNumber = token.getAccountNbrByDisplayStr(itemTxt)?: return
		val recordList = ApiGetTransactionsDone(this@AccountHistroyActivity, token).run(accNumber)
		if(recordList.isNullOrEmpty()){
			//todo
			return
		}

		val recordListTexts = arrayListOf<String>()
		recordList.forEach {
			recordListTexts.add(it.toString())
		}

		list.adapter = ArrayAdapter(
			this,
			android.R.layout.simple_spinner_item,
			recordListTexts
		)
	}
}