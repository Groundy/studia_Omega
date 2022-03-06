package com.example.omega

import android.content.Context

class ApiConsts {
	enum class scopeValues {AIS,AIS_ACC,PIS}
	companion object{
		val PREFERED_CHARSET = "utf-8"
		val PREFERED_ENCODING = "gzip"
		val CONTENT_TYPE = "application/json"
		val PREFERED_LAUNGAGE = "en"
		val appSecret_ALIOR = "M4uV4nK6lY5tP0vO7vC0cF8iR3rD4sN2wV6yM1aX3rU6uG8nS7"
		val userId_ALIOR = "6af374ae-480e-4631-b70c-4d8b2862e311"
		val REDIRECT_URI = "https://Omega:8080/auth/oauth2/callback"
		var pathToSaveFolder = ""
	}
}