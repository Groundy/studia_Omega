/*package com.example.omega

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

//NFC Part//



class NfcManager(context: Context){
    var nfcAdapter: NfcAdapter = NfcAdapter.getDefaultAdapter(context as Activity)
    var mainActivityContext: Context=context
    if(checkIfDeviceSupportNFC())
}

private fun checkIfDeviceSupportNFC(nfcAdapter: NfcAdapter,context: Context):Boolean{
    return if(nfcAdapter==null){
        Toast.makeText(context, "That device doesn't support NFC ", Toast.LENGTH_LONG)
        false
    } else true
}
private fun askForNFCPermissions(context:Context){
    val permissionArray= Array(1){ Manifest.permission.NFC}.toMutableList()
    val permissionListener=object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?){

        }

        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?){
            Toast.makeText(context,"Accept permissions first!", Toast.LENGTH_LONG)
        }

    }
    val activity = context as Activity
    Dexter.withActivity(activity).withPermissions(permissionArray).withListener(permissionListener).check()

}
private fun checkIfNfcIsEnabled(nfcAdapter: NfcAdapter,context: Context):Boolean{
    return if(nfcAdapter.isEnabled)
        true
    else{
        Toast.makeText(context,"Turn on NFC", Toast.LENGTH_LONG)
        false
    }
}
private fun startNfcConnection(nfcAdapter: NfcAdapter){
    var isNfcConnectionPossible=(checkIfDeviceSupportNFC(nfcAdapter) && checkIfNfcIsEnabled(nfcAdapter))
}
*/

