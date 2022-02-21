package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class TransferSummary : AppCompatActivity() {
	private lateinit var transferData : TransferData

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_transfer_summary)
		setUpGUI()
	}

	private fun setUpGUI() {
		val senderAccNumberField = getString(R.string.TransferSummary_COM_senderAccNumberField)
		val receiverAccNumberField = getString(R.string.TransferSummary_COM_receiverAccNumberField)
		val receiverNameField = getString(R.string.TransferSummary_COM_receiverNameField)
		val titleField = getString(R.string.TransferSummary_COM_TitleField)
		val amountField = getString(R.string.TransferSummary_COM_AmountField)

		val senderAccNumber = intent.getStringExtra(senderAccNumberField)
		val receiverAccNumber = intent.getStringExtra(receiverAccNumberField)
		val receiverName = intent.getStringExtra(receiverNameField)
		val title = intent.getStringExtra(titleField)
		val amount = intent.getStringExtra(amountField)
		transferData = TransferData(senderAccNumber,receiverAccNumber,receiverName,title,amount?.toDouble())

		findViewById<TextView>(R.id.transferSummary_amount).text = "$amount PLN"
		findViewById<TextView>(R.id.transferSummary_receAcc).text = receiverAccNumber
		findViewById<TextView>(R.id.transferSummary_receName).text = receiverName
		findViewById<TextView>(R.id.transferSummary_senderAcc).text = senderAccNumber
		findViewById<TextView>(R.id.transferSummary_title).text = title

		findViewById<Button>(R.id.TransferSummary_cancel_Button).setOnClickListener{cancelClicked()}
		findViewById<Button>(R.id.TransferSummary_auth_Button).setOnClickListener{authClicked()}
	}
	private fun cancelClicked(){
		this.finish()
	}
	private fun authClicked(){
		val des  = transferData.toString()
		ActivityStarter.startAuthActivity(this,des,null)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		val isAuthCode =
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)) ||
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_FINGER))

		if(isAuthCode){
			if(resultCode == RESULT_OK)
				ActivityStarter.startResultActivity(this,R.string.GUI_result_OK)
			else
				ActivityStarter.startResultActivity(this,R.string.GUI_result_WRONG_AUTH)
		}
		this.finish()
	}

}