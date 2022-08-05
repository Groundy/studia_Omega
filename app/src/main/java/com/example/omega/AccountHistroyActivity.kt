package com.example.omega

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
		//val listAdapter = ArrayAdapter(this@AccountHistroyActivity, android.R.layout.simple_spinner_item, emptyList<String>())
		//list.adapter = listAdapter
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

			val recordsList = token.getPaymentAccount(accNumber)?.accountHistory
			if(recordsList.isNullOrEmpty()){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to get hist from payment account")
				dialog.hide()
				return@launch
			}

			val adapterToSet = customAdapter(this@AccountHistroyActivity,accNumber, recordsList)
			withContext(Main){
				list.adapter = adapterToSet
				dialog.hide()
			}
		}

	}
}



class customAdapter(private val callerActivity : Activity, private val accountNumber: String, private val list : List<AccountHistoryRecord>) : BaseAdapter() {
	override fun getCount(): Int {
		return list.size
	}

	override fun getItem(p0: Int): Any {
		return  "test"
	}

	override fun getItemId(p0: Int): Long {
		return p0.toLong()
	}

	@SuppressLint("ViewHolder", "SetTextI18n")
	override fun getView(position: Int, convertView: View?, viewGroup : ViewGroup?): View {
		val layoutInflater = LayoutInflater.from(callerActivity)
		val rowView = layoutInflater.inflate(R.layout.history_record, viewGroup, false)

		val rec = list[position]

		val senderName = rowView.findViewById<TextView>(R.id.hisRec_fromName)
		val sendeNumber = rowView.findViewById<TextView>(R.id.hisRec_fromAccNumber)
		val amount = rowView.findViewById<TextView>(R.id.hisRec_amount)
		val recipientName = rowView.findViewById<TextView>(R.id.hisRec_toAccName)
		val recipientNumber = rowView.findViewById<TextView>(R.id.hisRec_toAccNumber)
		val date = rowView.findViewById<TextView>(R.id.hisRec_date)
		val description = rowView.findViewById<TextView>(R.id.hisRec_description)

		val isTransferFromUser = rec.senderAccNumber == accountNumber
		if(isTransferFromUser)
			amount.setTextColor(Color.RED)
		else
			amount.setTextColor(Color.GREEN)

		recipientName.text = "Odbiorca: ${rec.recipientName}"
		recipientNumber.text = "${rec.recipientAccNumber}"
		senderName.text = "Nadawca: ${rec.senderName}"
		sendeNumber.text = "${rec.senderAccNumber}"
		amount.text = "Kwota: ${rec.amount} ${rec.currency}"
		date.text = OmegaTime.convertTimeToDisplay(rec.tradeDate!!)
		description.text = "Opis: ${rec.description} "


		return rowView
	}

}