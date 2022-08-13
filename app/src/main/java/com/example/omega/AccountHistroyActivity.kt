package com.example.omega

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.FilterDialog.OnMyDialogResult
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class AccountHistroyActivity : AppCompatActivity() {
	private lateinit var spinner : Spinner
	private lateinit var list : ListView
	private lateinit var token: Token
	private var fillterDataObj : TransactionsDoneAdditionalInfos = TransactionsDoneAdditionalInfos()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_histroy)
		setGUI()

		val dialog = WaitingDialog(this, R.string.POPUP_getAccountsDetails)
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

	private suspend fun getToken() : Boolean{
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
	private suspend fun fillListOfAccounts() : SpinnerAdapter?{
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
			//todo its suspended
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
		findViewById<Button>(R.id.AccHistoryActivity_filterButton).setOnClickListener{
			showFilterDialog()
		}
	}
	private fun userChangedAccount(){
		val itemTxt = spinner.selectedItem.toString()
		val accNumber = token.getAccountNbrByDisplayStr(itemTxt)?: return
		val dialog = WaitingDialog(this, R.string.POPUP_getSingleAccountHistory)
		CoroutineScope(IO).launch{
			val accountHaveHistory = token.fillHistoryToPaymentAccount(this@AccountHistroyActivity, accNumber, fillterDataObj)
			if(!accountHaveHistory){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to fill PaymentAccount with details")
				withContext(Main){
					list.adapter = CustomAdapter(this@AccountHistroyActivity,accNumber, emptyList())
					dialog.hide()
				}
				return@launch
			}

			val recordsList = token.getPaymentAccount(accNumber)?.accountHistory
			if(recordsList.isNullOrEmpty()){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to get hist from payment account")
				withContext(Main){
					list.adapter = CustomAdapter(this@AccountHistroyActivity,accNumber, emptyList())
					dialog.hide()
				}
				return@launch
			}

			val adapterToSet = CustomAdapter(this@AccountHistroyActivity,accNumber, recordsList.sorted())
			withContext(Main){
				list.adapter = null
				list.adapter = adapterToSet
				dialog.hide()
			}
		}

	}
	private fun showFilterDialog(){
		val dialog = FilterDialog(this)
		dialog.setDialogResult(object : OnMyDialogResult {
			override fun finish(result: TransactionsDoneAdditionalInfos?) {
				if(result!=null){
					dialog.dismiss()
					userAppliedFillters(result)
				}
			}
		})
		dialog.show()
	}
	private fun userAppliedFillters(fillterDataObj: TransactionsDoneAdditionalInfos){
		token.clearPaymentAccsHistory()
		this.fillterDataObj = fillterDataObj
		userChangedAccount()
	}
}


class CustomAdapter(private val callerActivity : Activity, private val accountNumber: String, private val list : List<AccountHistoryRecord>) : BaseAdapter() {
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

class FilterDialog(context: Context) : Dialog(context) {
	private lateinit var minAmountField : EditText
	private lateinit var maxAmountField : EditText
	private lateinit var applyButton : Button
	private lateinit var backButton : Button
	private lateinit var startDateField : TextView
	private lateinit var endDateField : TextView
	private var mDialogResult: OnMyDialogResult? = null

	fun setDialogResult(dialogResult: OnMyDialogResult) {
		mDialogResult = dialogResult
	}

	interface OnMyDialogResult {
		fun finish(result: TransactionsDoneAdditionalInfos?)
	}


	private fun setGui(){
		minAmountField = findViewById(R.id.FilterAct_minAmount_editText)
		maxAmountField= findViewById(R.id.FilterAct_maxAmount_editText)
		applyButton = findViewById(R.id.FilterAct_apply_Button)
		backButton = findViewById(R.id.FilterAct_cancel_Button)
		startDateField = findViewById(R.id.FilterAct_dateFrom_textView)
		endDateField = findViewById(R.id.FilterAct_dateTo_textView)

		startDateField.text = OmegaTime.getDate(7, false)
		endDateField.text = OmegaTime.getDate(0, false)
		checkDatesCorrectness()
	}
	private fun setListeners(){
		backButton.setOnClickListener {
			this.dismiss()
		}
		applyButton.setOnClickListener {
			applyButtonClicked()
		}
		val minAmountListener = object : TextWatcher {
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(!p0.isNullOrEmpty())
					Utilities.stopUserFromPuttingMoreThan2DigitsAfterComma(
						minAmountField,
						previousValue,
						p0.toString()
					)
				checkIfMaxAmountIsBiggerThanMin()
			}
		}
		val maxAmountListener = object : TextWatcher {
			var previousValue : String = ""
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				if(p0 != null)
					previousValue = p0.toString()
			}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(p0: Editable?) {
				if(!p0.isNullOrEmpty())
					Utilities.stopUserFromPuttingMoreThan2DigitsAfterComma(
						maxAmountField,
						previousValue,
						p0.toString()
					)
				checkIfMaxAmountIsBiggerThanMin()
			}
		}
		minAmountField.addTextChangedListener(minAmountListener)
		maxAmountField.addTextChangedListener(maxAmountListener)


		val startDateFieldClickedListener = View.OnClickListener{
			val listener = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
				var dayOfMonthStr = dayOfMonth.toString()
				if(dayOfMonthStr.length == 1)
					dayOfMonthStr = "0$dayOfMonthStr"

				var monthOfYearStr = (monthOfYear+1).toString()
				if(monthOfYearStr.length == 1)
					monthOfYearStr = "0$monthOfYearStr"

				val str = "$dayOfMonthStr-$monthOfYearStr-$year"
				startDateField.text = str
				checkDatesCorrectness()
			}
			val c = Calendar.getInstance()
			val year = c.get(Calendar.YEAR)
			val month = c.get(Calendar.MONTH)
			val day = c.get(Calendar.DAY_OF_MONTH)
			val dpd = DatePickerDialog(this.context, listener, year, month, day)
			dpd.show()

		}

		val endDateFieldClickedListener = View.OnClickListener{
			val listener = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
				var dayOfMonthStr = dayOfMonth.toString()
				if(dayOfMonthStr.length == 1)
					dayOfMonthStr = "0$dayOfMonthStr"

				var monthOfYearStr = (monthOfYear+1).toString()
				if(monthOfYearStr.length == 1)
					monthOfYearStr = "0$monthOfYearStr"

				val str = "$dayOfMonthStr-$monthOfYearStr-$year"
				endDateField.text = str
				checkDatesCorrectness()
			}
			val c = Calendar.getInstance()
			val year = c.get(Calendar.YEAR)
			val month = c.get(Calendar.MONTH)
			val day = c.get(Calendar.DAY_OF_MONTH)
			val dpd = DatePickerDialog(this.context, listener, year, month, day)
			dpd.show()
		}

		startDateField.setOnClickListener(startDateFieldClickedListener)
		endDateField.setOnClickListener(endDateFieldClickedListener)
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.account_history_filters)
		setGui()
		setListeners()
		setCancelable(false)
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private fun checkIfMaxAmountIsBiggerThanMin() : Boolean{
		val minText = minAmountField.text.toString()
		val minAmount = if(minText.isEmpty())
			0.0
		else
			minText.toDouble()


		val maxText = maxAmountField.text.toString()
		val maxAmount = if(maxText.isEmpty())
			0.0
		else
			maxText.toDouble()

		var ok = maxAmount > minAmount
		if(maxAmount == 0.0 && minAmount == 0.0)
			ok =true

		val img = if(ok)
			this.context.resources.getDrawable(R.drawable.main_frame)
		else
			this.context.resources.getDrawable(R.drawable.error_frame)

		minAmountField.background = img
		maxAmountField.background = img
		return ok
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private fun checkDatesCorrectness(): Boolean {
		val okImg = this.context.resources.getDrawable(R.drawable.main_frame)
		val errIng = this.context.resources.getDrawable(R.drawable.error_frame)

		val todayTimeVal = OmegaTime.convertDateToLong(OmegaTime.getDate(0,false))
		val startDayTimeVal = OmegaTime.convertDateToLong(startDateField.text.toString())
		val endDayTimeVal = OmegaTime.convertDateToLong(endDateField.text.toString())

		if(endDayTimeVal <= todayTimeVal)
			endDateField.background = okImg
		else{
			endDateField.background = errIng
			return false
		}

		return if(startDayTimeVal <= endDayTimeVal){
			endDateField.background = okImg
			startDateField.background = okImg
			true
		}
		else{
			endDateField.background = errIng
			startDateField.background = errIng
			false
		}
	}

	private fun applyButtonClicked(){
		val ok = checkDatesCorrectness() && checkIfMaxAmountIsBiggerThanMin()
		if(!ok)
			return

		val dataObj = getFillterDataObj()
		if(dataObj == null)//todo
			return

		mDialogResult!!.finish(dataObj)
	}
	private fun getFillterDataObj() : TransactionsDoneAdditionalInfos?{
		return try {
			val startParts = startDateField.text.split("-")
			val dateFrom = "${startParts[2]}-${startParts[1]}-${startParts[0]}"

			val endParts = endDateField.text.split("-")
			val dateTo = "${endParts[2]}-${endParts[1]}-${endParts[0]}"

			val amountMinText =  minAmountField.text.toString()
			val amountMin = if(amountMinText.isEmpty())
				0.0
			else
				amountMinText.toDouble()

			val amountMaxText =  maxAmountField.text.toString()
			val amountMax = if(amountMaxText.isEmpty())
				0.0
			else
				amountMaxText.toDouble()

			val fillterDataObj = TransactionsDoneAdditionalInfos(amountMin, amountMax, dateFrom, dateTo)
			fillterDataObj
		}catch (e : Exception){
			Log.e(TagProduction, "applyButtonClicked/${this.javaClass.name} error in parsing data")
			null
		}
	}

}
