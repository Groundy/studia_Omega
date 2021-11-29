package com.example.omega
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		FirebaseApp.initializeApp(this)
		//var qrScanner : QrScanner = QrScanner(this)
		//BlikNumberEditText.requestFocus()
		//startNfcConnection()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_app_menu,menu)
		return super.onCreateOptionsMenu(menu)
	}




	private fun showMsg(stringToDisplay:String){
		val dialogBuilderVar=AlertDialog.Builder(this)
		val dialogInterfaceVar=object : DialogInterface.OnClickListener{
			override fun onClick(p0: DialogInterface, p1: Int) {
				p0.dismiss()
			}
		}
		dialogBuilderVar.setMessage(stringToDisplay).setPositiveButton("Ok",dialogInterfaceVar)
		val dialog:AlertDialog=dialogBuilderVar.create()
		dialog.show()
	}
	/*
	var nfcAdapter : NfcAdapter = NfcAdapter.getDefaultAdapter(this)

	private fun checkIfDeviceSupportNFC(): Boolean {
		var toRet = true
		 if(nfcAdapter==null){
			Toast.makeText(this, "That device doesn't support NFC ", Toast.LENGTH_LONG)
			toRet = false
		}
		return toRet
	}

	fun askForNFCPermissions(){
		val permissionArray= Array(1){ Manifest.permission.NFC}.toMutableList()
		val permissionListener=object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

			}

			override fun onPermissionRationaleShouldBeShown(
				permissions: MutableList<PermissionRequest>?,
				token: PermissionToken?
			) {
				Toast.makeText(this@MainActivity, "Accept permissions first!", Toast.LENGTH_LONG)
			}
		}
		Dexter.withActivity(this).withPermissions(permissionArray).withListener(permissionListener).check()

	}
	fun checkIfNfcIsEnabled():Boolean{
		return if(nfcAdapter.isEnabled)
			true
		else{
			Toast.makeText(this,"Turn on NFC", Toast.LENGTH_LONG)
			false
		}
	}
	fun startNfcConnection(){
		askForNFCPermissions()
		nfcAdapter = NfcAdapter.getDefaultAdapter(this)
		//checkIfDeviceSupportNFC()
	}
*/
}


