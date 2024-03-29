package com.example.omega

class ApiConsts {
	enum class ScopeValues(val text : String) {
		Ais("ais"),
		//AisAcc("ais-accounts"),
		Pis("pis");
		companion object{
			fun fromStr(text: String) : ScopeValues{
				return when(text){
					Ais.text -> Ais
					Pis.text -> Pis
					else -> Ais
				}
			}
		}
	}
	enum class Privileges(val text : String){
		AccountsDetails("ACC_DETAILS"),
		AccountsHistory("ACC_HISTORY"),
		SinglePayment("SINGLE_PAYMENT"),
		Bundle("Bundle"),
		GetPayment("GetPayment")
	}
	enum class BankUrls(val text : String){
		AuthUrl("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/authorize"),
		GetTokenUrl ("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/auth/v3_0.1/token"),
		GetPaymentAccount("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccount"),
		//GetPaymentAccounts("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getAccounts"),
		GetTransactionsDone("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/accounts/v3_0.1/getTransactionsDone"),
		SinglePayment("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/payments/v3_0.1/domestic"),
		Bundle("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/payments/v3_0.1/bundle"),
		getPaymentStstus("https://gateway.developer.aliorbank.pl/openapipl/sb/v3_0.1/payments/v3_0.1/getPayment")
	}
	enum class ApiReqFields(val text : String){
		RequestHeader("requestHeader"),
		RequestId("requestId"),
		UserAgent("userAgent"),
		IpAddress("ipAddress"),
		SendDate("sendDate"),
		TppId("tppId"),
		IsCompanyContext("isCompanyContext"),
		Code("Code"),
		GrantType("grant_type"),
		RedirectUri("redirect_uri"),
		ClientId("client_id"),
		ClientSecret("client_secret"),

		TokenField("token"),
		IsDirectPsu("isDirectPsu"),
		DirectPsu("directPsu"),
		AccountNumberField("accountNumber"),

		Authorization("authorization"),

		ResponseType("response_type"),
		Scope("scope"),
		ScopeDetails("scope_details"),
		State("state"),
		RefreshToken("refresh_token")
	}
	enum class GrantTypes(val text : String){
		AuthorizationCode("authorization_code"),
		RefreshToken("refresh_token")
	}


	enum class ResponseTypes(val text: String){
		Code("code")
	}
	enum class ScopeUsageLimit(val text : String) {
		Multiple("multiple"),
		Single("single")
	}
	enum class ScopeDetailsFields(val text: String){
		ScopeUsageLimit("scopeUsageLimit"),
		MaxAllowedHistoryLong("maxAllowedHistoryLong")
	}
	enum class ResponseCodes(val code : Int){
		OK(200),
		LimitExceeded(429)
	}

	companion object {
		const val PREFERED_CHARSET = "utf-8"
		const val PREFERED_ENCODING = "gzip"
		const val CONTENT_TYPE = "application/json"
		const val PREFERED_LAUNGAGE = "en"
		const val appSecret_ALIOR = "M4uV4nK6lY5tP0vO7vC0cF8iR3rD4sN2wV6yM1aX3rU6uG8nS7"
		const val userId_ALIOR = "6af374ae-480e-4631-b70c-4d8b2862e311"
		const val REDIRECT_URI = "https://Omega:8080/auth/oauth2/callback"
		const val TTP_ID = "requiredValueThatIsNotValidated"

		//not Checked values required by Bank
		const val ThrottlingPolicyVal = "psd2Regulatory"
		const val ConsentId = "123456789"


		const val AuthUrlValidityTimeSeconds = 24 * 60 * 60
		const val requestTimeOutMiliSeconds = 10 * 1000L
		//const val ThreadTimeOut = requestTimeOut*2
		const val ipTimeCheckPeriodSeconds = 120

		const val countryCodeLength = 2
	}

}