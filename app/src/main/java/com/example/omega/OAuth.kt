package com.example.omega

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URI
import java.net.URL

class OAuth : AppCompatActivity() {
	lateinit var webView: WebView
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
		webView = this.findViewById(R.id.webView)
		webView.settings.javaScriptEnabled = true
		webView.settings.allowContentAccess = true
		webView.settings.allowFileAccess = true
		webView.settings.databaseEnabled = true
		webView.settings.domStorageEnabled = true
		webView.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
				request?.let {
					val isProperRedirectUri = request.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
					if(!isProperRedirectUri){
						Log.e("WookieTag", "Failed to obtain code()[resuest not started with my app callBack], request [${request.url.toString()}]")
						finishThisActivity(false)
					}

					val responseState = request.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
					val responseStateCorrect = responseState == expectedRedirectState
					if (!responseStateCorrect) {
						Log.e("WookieTag", "Failed to obtain code[wrong state], request [${request.url.toString()}]")
						finishThisActivity(false)
					}

					val code : String? = request.url.getQueryParameter("code")
					if (code == null) {
						Log.e("WookieTag", "Failed to obtain code[no code], request [${request.url.toString()}]")
						finishThisActivity(false)
					}
					finishThisActivity(true, code)
				}
				return super.shouldOverrideUrlLoading(view, request)
			}
		}
	}
	private fun getDataFromIntent(){
		val uriField = resources.getString(R.string.ACT_COM_WEBVIEW_URI_FIELDNAME)
		val stateField = resources.getString(R.string.ACT_COM_WEBVIEW_STATE_FIELDNAME)
		uri = intent.getStringExtra(uriField).toString()
		if(uri == null){
			Log.e("WookieTag", "Failed to authorize, wrong Uri passed to activity")
			finishThisActivity(false)
		}

		expectedRedirectState = intent.getStringExtra(stateField).toString()
		if(expectedRedirectState == null){
			Log.e("WookieTag", "Failed to authorize, Uri passed to activity has no state parameter")
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