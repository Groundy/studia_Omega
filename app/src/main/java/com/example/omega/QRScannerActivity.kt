package com.example.omega
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_qrscanner.*
import android.content.Intent

class QRScannerActivity() : AppCompatActivity() {
	val options = FirebaseVisionBarcodeDetectorOptions.Builder()
		.setBarcodeFormats(
			FirebaseVisionBarcode.FORMAT_QR_CODE)
		.build()
	var imgAnalyzer = ImageAnalysis.Builder().build()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_qrscanner)
		askForCameraPermissions()
		imgAnalyzer.setAnalyzer(mainExecutor,YourImageAnalyzer(this))
		startCamera()
	}
	private fun askForCameraPermissions(){
		val permissionListener = object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport) {
				if (report.areAllPermissionsGranted()) {

				}
				if (report.isAnyPermissionPermanentlyDenied or !report.areAllPermissionsGranted()) {
					val toastText = "skaner kodów QR musi mieć dostęp do używania aparatu i mikrofonu."
					Toast.makeText(this@QRScannerActivity,toastText,Toast.LENGTH_LONG).show()
					this@QRScannerActivity.finish()
				}
			}

			override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?,token: PermissionToken?) {
				token!!.continuePermissionRequest()
			}
		}

		Dexter.withActivity(this@QRScannerActivity).withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).withListener(permissionListener).onSameThread().check()
	}

	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance( this@QRScannerActivity)
		cameraProviderFuture.addListener(Runnable {
			// Used to bind the lifecycle of cameras to the lifecycle owner
			val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

			// Preview
			val preview = Preview.Builder().build().also {
				it.setSurfaceProvider(this.cameraPreview.surfaceProvider)
			}

			// Select back camera as a default
			val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

			try {
				// Unbind use cases before rebinding
				cameraProvider.unbindAll()

				// Bind use cases to camera
				cameraProvider.bindToLifecycle(
					this, cameraSelector,imgAnalyzer, preview)

			} catch(exc: Exception) {
				//TODO
				Utilites.showMsg(this,exc.toString())
			}

		}, ContextCompat.getMainExecutor(this))
	}


	private class YourImageAnalyzer(context : QRScannerActivity) : ImageAnalysis.Analyzer {
		val scanner = BarcodeScanning.getClient()
		val context = context
		@SuppressLint("UnsafeExperimentalUsageError")
		override fun analyze(imageProxy: ImageProxy) {
			val mediaImage = imageProxy.image
			if (mediaImage != null) {
				val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
				val result = scanner.process(image).addOnSuccessListener {barcodes ->
					for (barcode in barcodes) {
						val rawValue = barcode.rawValue
						if(rawValue.length == 6){
							var code : Int? = rawValue.toIntOrNull()
							if(code!=null) {
								context.setResult(code)
								context.finish()
							}
						}
					}
				}
			}
			imageProxy.close()
		}
	}
}
