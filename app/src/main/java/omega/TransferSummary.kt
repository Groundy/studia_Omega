package omega

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
		val transferDataSerializedField = getString(R.string.TransferSummary_COM_serializedData)
		val transferDataSerialized = intent.getStringExtra(transferDataSerializedField)
		transferData = TransferData(transferDataSerialized!!)

		findViewById<TextView>(R.id.transferSummary_amount).text = "${transferData.amount} ${transferData.currency}"
		findViewById<TextView>(R.id.transferSummary_receAcc).text = "${transferData.receiverAccNumber}"
		findViewById<TextView>(R.id.transferSummary_receName).text = "${transferData.receiverName}"
		findViewById<TextView>(R.id.transferSummary_senderAcc).text = "${transferData.senderAccNumber}"
		findViewById<TextView>(R.id.transferSummary_title).text = "${transferData.title}"

		findViewById<Button>(R.id.TransferSummary_cancel_Button).setOnClickListener{cancelClicked()}
		findViewById<Button>(R.id.TransferSummary_auth_Button).setOnClickListener{authClicked()}
	}
	private fun cancelClicked(){
		this.finish()
	}
	private fun authClicked(){
		val description  = transferData.toString()
		ActivityStarter.startAuthActivity(this, description, null)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		val isAuthCode =
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_PIN_AUTH)) ||
			(requestCode == this.resources.getInteger(R.integer.ACT_RETCODE_FINGER))

		if(isAuthCode){
			if(resultCode == RESULT_OK)
				ActivityStarter.startResultActivity(this, R.string.Result_GUI_OK)
			else
				ActivityStarter.startResultActivity(this, R.string.Result_GUI_WRONG_AUTH)
		}
		this.finish()
	}

}