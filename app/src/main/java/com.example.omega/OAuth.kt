package com.example.omega

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible

class OAuth : AppCompatActivity() {
	private lateinit var webView: WebView
	private var expectedRedirectState : String? = null
	private var uri : String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_oauth)
		setWebView()
		getDataFromIntent()
		webView.loadUrl(uri.toString())
	}

	private fun setWebView(){
		webView = this.findViewById(R.id.OAuth_webView)
		webView.settings.javaScriptEnabled = true
		webView.settings.allowContentAccess = true
		webView.settings.allowFileAccess = true
		webView.settings.databaseEnabled = true
		webView.settings.domStorageEnabled = true

		webView.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
				request?.let {
					view?.isVisible = false
					val isProperRedirectUri = it.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
					if(!isProperRedirectUri){
						Log.e(Utilites.TagProduction, "Failed to obtain code[request not started with app callBack], request [${request.url}]")
						finishThisActivity(false)
					}

					val responseState = it.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
					val responseStateCorrect = responseState == expectedRedirectState
					if (!responseStateCorrect) {
						Log.e(Utilites.TagProduction, "Failed to obtain code[wrong state], request [${request.url}]")
						finishThisActivity(false)
					}

					val code : String? = it.url.getQueryParameter("code")
					if (code == null) {
						Log.e(Utilites.TagProduction, "Failed to obtain code[no code], request [${request.url}]")
						finishThisActivity(false)
					}
					finishThisActivity(true, code)
				}
				return super.shouldOverrideUrlLoading(view, request)
			}

			override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
				super.onPageStarted(view, url, favicon)
				Log.i(Utilites.TagProduction, "login to bank webpage started loading")

			}
			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				Log.i(Utilites.TagProduction, "login to bank webpage ended loading")
			}
		}
	}
	private fun getDataFromIntent(){
		val uriField = resources.getString(R.string.ACT_COM_WEBVIEW_URI_FIELDNAME)
		val stateField = resources.getString(R.string.ACT_COM_WEBVIEW_STATE_FIELDNAME)
		uri = intent.getStringExtra(uriField).toString()
		if(uri == null){
			Log.e(Utilites.TagProduction, "Failed to authorize, wrong Uri passed to login activity")
			finishThisActivity(false)
		}

		expectedRedirectState = intent.getStringExtra(stateField).toString()
		if(expectedRedirectState == null){
			Log.e(Utilites.TagProduction, "Failed to authorize, Uri passed to login activity has no state parameter")
			finishThisActivity(false)
		}
	}
	private fun finishThisActivity(success : Boolean, code : String? = null){
		if(!success || code == null)
			finishActivity(RESULT_CANCELED)
		else{
			val codeReturnField = resources.getString(R.string.ACT_COM_WEBVIEW_AUTHCODE_FIELDNAME)
			val output = Intent()
				.putExtra(codeReturnField, code)
			setResult(RESULT_OK, output)
			finish()
		}
	}
}