package com.example.omega
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_qrscanner.*

class QrScannerActivity : AppCompatActivity() {

	private class YourImageAnalyzerClass(val context : QrScannerActivity) : ImageAnalysis.Analyzer {
		val options =  BarcodeScannerOptions.Builder()
			.setBarcodeFormats(Barcode.FORMAT_QR_CODE)
			.build()
		val scanner = BarcodeScanning.getClient(options)

		@SuppressLint("UnsafeExperimentalUsageError")
		override fun analyze(imageProxy: ImageProxy) {
			val mediaImage = imageProxy.image ?: return
			val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
			scanner.process(image).addOnSuccessListener{barcodes ->
				if(barcodes.size > 0)
					context.processCode(barcodes[0].rawValue)
				imageProxy.close()
			}
		}
	}

	private var imgAnalyzer = ImageAnalysis.Builder().build()

	private fun processCode(rawValue : String?){
		if(rawValue == null || rawValue.length != 6)
			return
		val codeValue = rawValue.toInt()
		val properValue = codeValue in 0..999999
		if(properValue)
			this.endActivity(this, true,rawValue.toInt())
		else{
			val userMsg = resources.getString(R.string.QrScanner_UserMsg_wrongQrFormat)
			Utilities.showToast(this, userMsg)
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_qrscanner)
		askForCameraPermissions()
		imgAnalyzer.setAnalyzer(mainExecutor, YourImageAnalyzerClass(this))
		startCamera()
	}
	private fun askForCameraPermissions(){
		val permissionListener = object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport) {
				if (report.isAnyPermissionPermanentlyDenied or !report.areAllPermissionsGranted()) {
					val toastText = getString(R.string.QrScanner_UserMsg_permissionNeeded)
					Toast.makeText(this@QrScannerActivity,toastText,Toast.LENGTH_LONG).show()
					endActivity(this@QrScannerActivity, false)
				}
			}
			override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?,token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}
		Dexter.withActivity(this@QrScannerActivity).withPermissions(Manifest.permission.CAMERA).withListener(permissionListener).onSameThread().check()
	}
	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance( this)
		val cameraListener = Runnable {
			val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()//bind the lifecycle of camera to the lifecycle owner
			val preview = Preview.Builder().build()
			preview.setSurfaceProvider(this.cameraPreview.surfaceProvider)

			try {
				cameraProvider.unbindAll()// Unbind use cases before rebinding
				val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA			// Select back camera as a default
				cameraProvider.bindToLifecycle(this, cameraSelector,imgAnalyzer, preview)// Bind use cases to camera
			}
			catch(exc: Exception) {
				Utilities.showMsg(this, exc.toString())
			}
		}
		cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(this))
	}
	private fun endActivity(activity: Activity, success : Boolean, code : Int = 0){
		if(success){
			val output = Intent()
			val fieldName = activity.resources.getString(R.string.ACT_COM_QrScanner_FIELD_NAME)
			output.putExtra(fieldName, code)
			activity.setResult(RESULT_OK, output)
		}
		else
			activity.setResult(RESULT_CANCELED)
		activity.finish()
	}
}
