package com.example.omega

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.omega.Utilities.Companion.TagProduction

class TransferSummary : AppCompatActivity() {
	private lateinit var transferData : TransferData

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_transfer_summary)
		getDataFromIntent()
		setUpGUI()
	}
	private fun setUpGUI() {
		with(findViewById<TextView>(R.id.transferSummary_amount)){
			val transferAmountText = "${transferData.amount} ${transferData.currency}"
			text = transferAmountText
			setTextColor(Color.GRAY)
		}
		with(findViewById<TextView>(R.id.transferSummary_receAcc)){
			text = "${transferData.receiverAccNumber}"
			setTextColor(Color.GRAY)
		}
		with(findViewById<TextView>(R.id.transferSummary_receName)){
			text = "${transferData.receiverName}"
			setTextColor(Color.GRAY)
		}
		with(findViewById<TextView>(R.id.transferSummary_senderAcc)){
			text = "${transferData.senderAccNumber}"
			setTextColor(Color.GRAY)
		}
		with(findViewById<TextView>(R.id.transferSummary_title)){
			text = "${transferData.description}"
			setTextColor(Color.GRAY)
		}

		findViewById<Button>(R.id.TransferSummary_cancel_Button).setOnClickListener{
			finish()
		}
		findViewById<Button>(R.id.TransferSummary_auth_Button).setOnClickListener{
			val description  = "${transferData.amount.toString()} ${transferData.currency}"
			ActivityStarter.startAuthActivity(this, description, null)
		}
	}
	private fun getDataFromIntent(){
		val transferDataSerializedField = getString(R.string.TransferSummary_COM_serializedData)
		val transferDataSerialized = intent.getStringExtra(transferDataSerializedField)
		val transferDataTmp = TransferData.fromJsonSerialized(transferDataSerialized!!)
		if(transferDataTmp == null){
			//todo
			return
		}
		transferData = transferDataTmp
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if(resultCode != RESULT_OK)
			return

		val retCodeisAuthCode =
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)) ||
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_FINGER))

		if(!retCodeisAuthCode)
			return


		val fieldName = resources.getString(R.string.ACT_COM_MANY_RetTransferData_FIELDNAME)
		val newIntent = Intent()
			.putExtra(fieldName, transferData.toString())
		setResult(RESULT_OK, newIntent)
		finish()
	}

}