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
import androidx.core.content.ContextCompat
import com.example.omega.FilterDialog.OnMyDialogResult
import com.example.omega.Utilities.Companion.TagProduction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class AccountHistroyActivity : AppCompatActivity() {
	private lateinit var spinner : Spinner
	private lateinit var list : ListView
	private lateinit var token: Token
	private var fillterDataObj : HistoryFillters = HistoryFillters()

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
				return@launch
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
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
				Utilities.showToast(this@AccountHistroyActivity, errorCodeTextToDisplay)
			}
			finish()
			return null
		}

		val listOfAccountsFromToken = token.getListOfAccountsNumbersToDisplay()
		if(listOfAccountsFromToken.isNullOrEmpty()){
			Log.e(TagProduction, "[fillListOfAccounts/${this.javaClass.name}], token return null or empty account list")
			withContext(Main){
				val errorCodeTextToDisplay = getString(R.string.AccHistoryAct_UserMsg_ErrorInObtainingToken)
				Utilities.showToast(this@AccountHistroyActivity, errorCodeTextToDisplay)
			}
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
		spinner = findViewById(R.id.AccHistoryActivity_Spinner)
		with(spinner){
			val spinnerListener = object : AdapterView.OnItemSelectedListener{
				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					userChangedAccount()
				}
				override fun onNothingSelected(p0: AdapterView<*>?) {}
			}
			onItemSelectedListener = spinnerListener
		}

		list = findViewById(R.id.AccHistoryActivity_List)

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

			val recordsListFromRequest = token.getPaymentAccount(accNumber)?.accountHistory
			if(recordsListFromRequest.isNullOrEmpty()){
				Log.e(TagProduction,"[userChangedAccount/${this@AccountHistroyActivity.javaClass.name}], failed to get hist from payment account")
				withContext(Main){
					list.adapter = CustomAdapter(this@AccountHistroyActivity,accNumber, emptyList())
					dialog.hide()
				}
				return@launch
			}

			val fillteredRecordsToDisplay  = ArrayList<AccountHistoryRecord>()
			recordsListFromRequest.forEach{
				val isInRange = fillterDataObj.amountIsInFillterRange(it.amount)
				if(isInRange)
					fillteredRecordsToDisplay.add(it)
			}
			val recordsToDisplay = fillteredRecordsToDisplay.toList().sorted()
			val adapterToSet = CustomAdapter(this@AccountHistroyActivity,accNumber, recordsToDisplay)
			withContext(Main){
				list.adapter = null
				list.adapter = adapterToSet
				dialog.hide()
			}
		}

	}
	private fun showFilterDialog(){
		val dialog = FilterDialog(this, fillterDataObj)
		dialog.setDialogResult(object : OnMyDialogResult {
			override fun finish(result: HistoryFillters?) {
				if(result!=null){
					dialog.dismiss()
					userAppliedFillters(result)
				}
			}
		})
		dialog.show()
	}
	private fun userAppliedFillters(fillterDataObj: HistoryFillters){
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

class FilterDialog(context: Context, fillterDataObj: HistoryFillters) : Dialog(context) {
	private lateinit var minAmountField : EditText
	private lateinit var maxAmountField : EditText
	private lateinit var applyButton : Button
	private lateinit var backButton : Button
	private lateinit var startDateField : TextView
	private lateinit var endDateField : TextView

	private var mDialogResult: OnMyDialogResult? = null
	private var filltersInput = fillterDataObj

	interface OnMyDialogResult {
		fun finish(result: HistoryFillters?)
	}


	private fun setGui(){
		backButton = findViewById(R.id.FilterAct_cancel_Button)
		backButton.setOnClickListener {
			this.dismiss()
		}

		applyButton = findViewById(R.id.FilterAct_apply_Button)
		applyButton.setOnClickListener {
			applyButtonClicked()
		}

		minAmountField = findViewById(R.id.FilterAct_minAmount_editText)
		with(minAmountField){
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
			addTextChangedListener(minAmountListener)
		}

		maxAmountField= findViewById(R.id.FilterAct_maxAmount_editText)
		with(maxAmountField){
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
			addTextChangedListener(maxAmountListener)
		}

		startDateField = findViewById(R.id.FilterAct_dateFrom_textView)
		with(startDateField){
			val startDateFieldClickedListener = View.OnClickListener{
				val listener = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
					var dayOfMonthStr = dayOfMonth.toString()
					if(dayOfMonthStr.length == 1)
						dayOfMonthStr = "0$dayOfMonthStr"

					var monthOfYearStr = (monthOfYear+1).toString()
					if(monthOfYearStr.length == 1)
						monthOfYearStr = "0$monthOfYearStr"

					val str = "$dayOfMonthStr-$monthOfYearStr-$year"
					text = str
					checkDatesCorrectness()
				}
				val c = Calendar.getInstance()
				val year = c.get(Calendar.YEAR)
				val month = c.get(Calendar.MONTH)
				val day = c.get(Calendar.DAY_OF_MONTH)
				val dpd = DatePickerDialog(this.context, listener, year, month, day)
				dpd.show()

			}
			setOnClickListener(startDateFieldClickedListener)
		}

		endDateField = findViewById(R.id.FilterAct_dateTo_textView)
		with(endDateField){
			val endDateFieldClickedListener = View.OnClickListener{
				val listener = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
					var dayOfMonthStr = dayOfMonth.toString()
					if(dayOfMonthStr.length == 1)
						dayOfMonthStr = "0$dayOfMonthStr"

					var monthOfYearStr = (monthOfYear+1).toString()
					if(monthOfYearStr.length == 1)
						monthOfYearStr = "0$monthOfYearStr"

					val str = "$dayOfMonthStr-$monthOfYearStr-$year"
					text = str
					checkDatesCorrectness()
				}
				val c = Calendar.getInstance()
				val year = c.get(Calendar.YEAR)
				val month = c.get(Calendar.MONTH)
				val day = c.get(Calendar.DAY_OF_MONTH)
				val dpd = DatePickerDialog(this.context, listener, year, month, day)
				dpd.show()
			}
			setOnClickListener(endDateFieldClickedListener)
		}

		checkDatesCorrectness()
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.account_history_filters)
		setGui()
		setCancelable(false)
		applyFilltersFromInput()
	}
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
			ok = true

		val img = if(ok)
			ContextCompat.getDrawable(context, R.drawable.main_frame)
		else
			ContextCompat.getDrawable(context, R.drawable.error_frame)

		minAmountField.background = img
		maxAmountField.background = img
		return ok
	}
	private fun checkDatesCorrectness(): Boolean {
		val okImg = ContextCompat.getDrawable(context, R.drawable.main_frame)
		val errIng = ContextCompat.getDrawable(context, R.drawable.error_frame)

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
		if(!checkDatesCorrectness())
			return
		if(!checkIfMaxAmountIsBiggerThanMin())
			return

		val dataObj = getFillterDataObj()
		if(dataObj == null){
			val msg = context.resources.getString(R.string.UserMsg_FILLTER_NOT_APPLIED)
			Utilities.showToast(context as Activity, msg)
			mDialogResult!!.finish(null)
		}

		mDialogResult!!.finish(dataObj)
	}
	private fun getFillterDataObj() : HistoryFillters?{
		return try {
			val startDate = startDateField.text.toString()
			val endDate = endDateField.text.toString()

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

			val fillterDataObj = HistoryFillters(amountMin, amountMax, startDate, endDate)
			fillterDataObj
		}catch (e : Exception){
			Log.e(TagProduction, "applyButtonClicked/${this.javaClass.name} error in parsing data")
			null
		}
	}
	private fun applyFilltersFromInput(){
		val minAmountStr = filltersInput.getMinAmountForDisplay()
		val maxAmountStr = filltersInput.getMaxAmountForDisplay()
		val minDate = filltersInput.getStartDateForDisplay()
		val maxDate = filltersInput.getEndDateForDisplay()

		this.minAmountField.text = Utilities.strToEditable(minAmountStr)
		this.maxAmountField.text = Utilities.strToEditable(maxAmountStr)
		this.startDateField.text = Utilities.strToEditable(minDate)
		this.endDateField.text = Utilities.strToEditable(maxDate)
	}
	fun setDialogResult(dialogResult: OnMyDialogResult) {
		mDialogResult = dialogResult
	}
}
