package com.example.omega

class ApiConsts {
	enum class ScopeValues {AIS,AIS_ACC,PIS}
	enum class Privileges(val text : String){
		AccountsDetails("ACC_DETAILS"), AccountsHistory("ACC_HISTORY"), SinglePayment("SINGLE_PAYMENT")
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

		const val AuthUrlValidityTimeSeconds = 24 * 60 * 60
		const val requestTimeOut = 1000L * 60 * 8
	}
}