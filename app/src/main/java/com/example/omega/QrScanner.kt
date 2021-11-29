package com.example.omega
import android.Manifest
import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.android.synthetic.main.activity_main.*

import com.example.omega.Utilites

public class QrScanner(activity : Activity) {
	private var activity: Activity = activity
	lateinit var cameraView: CameraView


	var isCodeDetected:Boolean=false
	lateinit var options: FirebaseVisionBarcodeDetectorOptions
	lateinit var QrDetector: FirebaseVisionBarcodeDetector

	private fun askForCameraPermissions(){
		val permissionArray = Array(2){Manifest.permission.CAMERA; Manifest.permission.RECORD_AUDIO}.toMutableList()
		val permissionListener = object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport?){
				startCamera()
			}

			override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?){
				Toast.makeText(activity,"Accept permissions first!", Toast.LENGTH_LONG)
			}
		}
		Dexter.withActivity(this.activity).withPermissions(permissionArray).withListener(permissionListener).check()

	}

	private fun getFireBaseVisionImage(frame: Frame): FirebaseVisionImage {
		//Convert frame to FB image
		val dataFromImg=frame.data
		val metadata: FirebaseVisionImageMetadata =
			FirebaseVisionImageMetadata.Builder().setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21).setHeight(
				frame.size.height).setWidth(frame.size.width).setRotation(frame.rotation).build()
		return FirebaseVisionImage.fromByteArray(dataFromImg,metadata)
	}
	private fun processImage(FB_Frame: FirebaseVisionImage){
		val onSuccessListenerVar=object: OnSuccessListener<List<FirebaseVisionBarcode>> {
			override fun onSuccess(p0: List<FirebaseVisionBarcode>) {
				if(!isCodeDetected)
					processResult(p0)
			}
		}
		val onFailureListenerVar= object : OnFailureListener {
			override fun onFailure(p0: java.lang.Exception) {
				Toast.makeText(activity,"process Image Expectation",Toast.LENGTH_LONG)
			}
		}
		if(!isCodeDetected){
			QrDetector.detectInImage(FB_Frame).addOnSuccessListener(onSuccessListenerVar).addOnFailureListener(onFailureListenerVar)
		}
	}
	private fun processResult(FB_bareCodes:List<FirebaseVisionBarcode>){
		if(FB_bareCodes.isNotEmpty()) {
			for (signCode in FB_bareCodes) {
				when (signCode.valueType) {
					FirebaseVisionBarcode.TYPE_TEXT -> {
						Utilites.showMsg(activity!!, signCode.displayValue!!)
						turnOffScanning()
					}
					else -> {
						Utilites.showMsg(activity!!,"That is QR code but it's not vaild!")
						turnOffScanning()
					}
				}
			}
		}
	}
	private fun turnOffScanning(){
		activity!!.scanButton.isEnabled=true
		activity!!.scanButton.text="Scan Again"
		isCodeDetected=true
	}
	private fun startCamera() {
		val scanButton: Button = activity!!.findViewById(R.id.scanButton)

		lateinit var frameProcessor: FrameProcessor
		cameraView = activity!!.findViewById(R.id.CameraView2)

		scanButton.isEnabled = false
		scanButton.setOnClickListener {
			scanButton.isEnabled=false
			scanButton.text="Scanning"
			isCodeDetected=false
		}

		//cameraView.setLifecycleOwner(activity)

		frameProcessor = object : FrameProcessor {
			override fun process(frame: Frame) {
				processImage(getFireBaseVisionImage(frame))
			}
		}

		cameraView.addFrameProcessor(frameProcessor)
		options = FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
			.build()
		QrDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

	}
}