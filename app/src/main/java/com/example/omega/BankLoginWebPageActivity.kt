package com.example.omega

import android.annotation.SuppressLint
import android.content.Intent
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
	private lateinit var redirect : WebActivtyRedirect
	companion object{
		enum class WebActivtyRedirect(val text : String){
			AccountHistory("AccountHistory"),
			PaymentCreation("Payment"),
			DomesticPaymentProcess("domesticPaymentProcess"),
			GenerateRBlikCode("GenerateRBlikCode"),
			None("None");

			companion object{
				fun fromStr(text : String) : WebActivtyRedirect{
					val webActivtyRedirect = when(text){
						AccountHistory.text -> AccountHistory
						PaymentCreation.text -> PaymentCreation
						GenerateRBlikCode.text -> GenerateRBlikCode
						DomesticPaymentProcess.text ->DomesticPaymentProcess
						else -> None
					}
					return webActivtyRedirect
				}
			}
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_oauth)
		Log.i(TagProduction, "Login To Bank activity started")
		getInfoFromIntent()
		val readValuesOk = readPrefValues()
		if(!readValuesOk){
			setResult(RESULT_CANCELED)
			Log.i(TagProduction, "Login To Bank activity ended, RESULT_CANCELD")
			finish()
		}
		setWebView()
		webView.loadUrl(url)
	}

	@SuppressLint("SetJavaScriptEnabled")
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
					reciveInfoFromBank(request)
				}
				return super.shouldOverrideUrlLoading(view, request)
			}
		}
	}
	private fun readPrefValues() : Boolean{
		expectedState = PreferencesOperator.readPrefStr(this, R.string.PREF_lastRandomValue)
		url = PreferencesOperator.readPrefStr(this, R.string.PREF_authURL)
		if(expectedState.isEmpty() || url.isEmpty())
			return false

		val urlValidityTime = PreferencesOperator.readPrefStr(this, R.string.PREF_authUrlValidityTimeEnd)
		return OmegaTime.timestampIsValid(urlValidityTime)
	}
	private fun reciveInfoFromBank(request: WebResourceRequest){
		val isProperRedirectUri = request.url.toString().startsWith(ApiConsts.REDIRECT_URI, true)
		if(!isProperRedirectUri){
			Log.e(TagProduction, "Failed to obtain code[request not started with app callBack], request [${request.url}]")
			finishActivity(false)
			return
		}

		val responseState = request.url.getQueryParameter("state")// To prevent CSRF attacks, check that we got the same state value we sent,
		val responseStateCorrect = responseState == expectedState
		if (!responseStateCorrect) {
			val msg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
			Utilities.showToast(this, msg)
			Log.e(TagProduction, "Failed to obtain code[wrong state], request [${request.url}]")
			finishActivity(false)
			return
		}

		val code : String? = request.url.getQueryParameter("code")
		if (code.isNullOrEmpty()) {
			val msg = getString(R.string.BankLogin_UserMsg_ErrorInBankTryAgian)
			Log.e(TagProduction, "Failed to obtain code[no code], request [${request.url}]")
			Utilities.showToast(this, msg)
			finishActivity(false)
			return
		}
		else {
			PreferencesOperator.savePref(this, R.string.PREF_authCode, code)
			PreferencesOperator.clearPreferences(this, R.string.PREF_authURL, R.string.PREF_lastRandomValue)
			finishActivity(true)
			Log.i(TagProduction, "Login To Bank activity ended, RESULT_OK")
			return
		}
	}
	private fun getInfoFromIntent(){
		val fieldName = getString(R.string.ACT_COM_WEBVIEW_REDIRECT_FIELD_NAME)
		val redirectStr = intent.extras!!.getString(fieldName)
		val webActivtyRedirect = WebActivtyRedirect.fromStr(redirectStr!!)
		redirect = webActivtyRedirect
	}
	private fun finishActivity(success: Boolean){
		if(!success){
			setResult(RESULT_CANCELED)
			finish()
		}

		val output = Intent()
		val fieldName = this.resources.getString(R.string.ACT_COM_WEBVIEW_REDIRECT_FIELD_NAME)
		output.putExtra(fieldName, redirect.text)
		setResult(RESULT_OK, output)
		finish()
	}
}
