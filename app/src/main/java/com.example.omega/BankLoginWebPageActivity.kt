package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.example.omega.Utilities.Companion.TagProduction

class BankLoginWebPageActivity : AppCompatActivity() {
	private lateinit var webView: WebView
	private var expectedState = String()
	private var url = String()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_oauth)
		Log.i(TagProduction, "Login To Bank activity started")
		val readValuesOk = readPrefValues()
		if(!readValuesOk){
			setResult(RESULT_CANCELED)
			Log.i(TagProduction, "Login To Bank activity ended, RESULT_CANCELD")
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
					reciveInfoFromBank(view, request)
				}
				return super.shouldOverrideUrlLoading(view, request)
			}
		}
	}
	private fun readPrefValues() : Boolean{
		expectedState = PreferencesOperator.readPrefStr(this, R.string.PREF_lastRandomValue)
		url = PreferencesOperator.readPrefStr(this, R.string.PREF_authURL)
		if(expectedState.isNullOrEmpty() || url.isNullOrEmpty())
			return false

		val urlValidityTime = PreferencesOperator.readPrefStr(this, R.string.PREF_authUrlValidityTimeEnd)
		val urlVaild = OmegaTime.timestampIsValid(urlValidityTime)
		return urlVaild
	}
	private fun reciveInfoFromBank(view: WebView?, request: WebResourceRequest){
		val isProperRedirectUri = request.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
		if(!isProperRedirectUri){
			Log.e(TagProduction, "Failed to obtain code[request not started with app callBack], request [${request.url}]")
			setResult(RESULT_CANCELED)
			finish()
		}

		val responseState = request.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
		val responseStateCorrect = responseState == expectedState
		if (!responseStateCorrect) {
			val msg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
			Utilities.showToast(this, msg)
			Log.e(TagProduction, "Failed to obtain code[wrong state], request [${request.url}]")
			setResult(RESULT_CANCELED)
			finish()
		}

		val code : String? = request.url.getQueryParameter("code")
		if (code.isNullOrEmpty()) {
			val msg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
			Log.e(TagProduction, "Failed to obtain code[no code], request [${request.url}]")
			Utilities.showToast(this, msg)
			setResult(RESULT_CANCELED)
			finish()
		}
		else {
			PreferencesOperator.savePref(this, R.string.PREF_authCode, code)
			setResult(RESULT_OK)
			finish()
			Log.i(TagProduction, "Login To Bank activity ended, RESULT_OK")
		}
	}
}
