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
	private var expectedState = String()
	private var url = String()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_oauth)
		val readValuesOk = readPrefValues()
		if(!readValuesOk){
			setResult(RESULT_CANCELED)
			finish()
		}
		setWebView()
		webView.loadUrl(url)
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
				view?.isVisible = false
				request?.let {
					val isProperRedirectUri = it.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
					if(!isProperRedirectUri){
						Log.e(Utilities.TagProduction, "Failed to obtain code[request not started with app callBack], request [${request.url}]")
						setResult(RESULT_CANCELED)
						finish()
					}

					val responseState = it.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
					val responseStateCorrect = responseState == expectedState
					if (!responseStateCorrect) {
						Utilities.showToast(this@OAuth, "Błąd uwierzytelniania ze strony banku, spróbuj ponownie")//todo TOFILE
						Log.e(Utilities.TagProduction, "Failed to obtain code[wrong state], request [${request.url}]")
						setResult(RESULT_CANCELED)
						finish()
					}

					val code : String? = it.url.getQueryParameter("code")
					if (code.isNullOrEmpty()) {
						Log.e(Utilities.TagProduction, "Failed to obtain code[no code], request [${request.url}]")
						Utilities.showToast(this@OAuth, "Błąd uwierzytelniania ze strony banku, spróbuj ponownie")//todo TOFILE
						setResult(RESULT_CANCELED)
						finish()
					}
					else {
						PreferencesOperator.savePref(this@OAuth, R.string.PREF_authCode, code)
						setResult(RESULT_OK)
						finish()
					}
				}
				return super.shouldOverrideUrlLoading(view, request)
			}

			override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
				super.onPageStarted(view, url, favicon)
				Log.i(Utilities.TagProduction, "login to bank webpage started loading")

			}
			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				Log.i(Utilities.TagProduction, "login to bank webpage ended loading")
			}
		}
	}

	private fun readPrefValues() : Boolean{
		expectedState = PreferencesOperator.readPrefStr(this, R.string.PREF_lastRandomValue)
		url = PreferencesOperator.readPrefStr(this, R.string.PREF_authURL)
		val urlValidityTime = PreferencesOperator.readPrefStr(this, R.string.PREF_authUrlValidityTimeEnd)
		val urlVaild = OmegaTime.timestampIsValid(urlValidityTime)
		val valuesAreOk = urlVaild && url.isNotEmpty() && expectedState.isNotEmpty()
		return valuesAreOk
	}
	/*
	private fun reciveInfoFromBank(view: WebView?, request: WebResourceRequest){
		val isProperRedirectUri = request.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
		if(!isProperRedirectUri){
			Log.e(Utilities.TagProduction, "Failed to obtain code[request not started with app callBack], request [${request.url}]")
			setResult(RESULT_CANCELED)
			finish()
		}

		val responseState = request.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
		val responseStateCorrect = responseState == expectedState
		if (!responseStateCorrect) {
			Utilities.showToast(this, "Błąd uwierzytelniania ze strony banku, spróbuj ponownie")//todo TOFILE
			Log.e(Utilities.TagProduction, "Failed to obtain code[wrong state], request [${request.url}]")
			setResult(RESULT_CANCELED)
			finish()
		}

		val code : String? = request.url.getQueryParameter("code")
		if (code.isNullOrEmpty()) {
			Log.e(Utilities.TagProduction, "Failed to obtain code[no code], request [${request.url}]")
			Utilities.showToast(this, "Błąd uwierzytelniania ze strony banku, spróbuj ponownie")//todo TOFILE
			setResult(RESULT_CANCELED)
			finish()
		}
		else {
			PreferencesOperator.savePref(this, R.string.PREF_authCode, code)
			setResult(RESULT_OK)
			finish()
		}
	}
*/
}
