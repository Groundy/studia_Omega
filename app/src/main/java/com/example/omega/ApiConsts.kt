package com.example.omega

class ApiConsts {
	companion object{
		val PREFERED_CHARSET = "utf-8"
		val PREFERED_ENCODING = "deflate"
		val CONTENT_TYPE = "application/json"
		val PREFERED_LAUNGAGE = "en"
		val appSecret_ALIOR = "M4uV4nK6lY5tP0vO7vC0cF8iR3rD4sN2wV6yM1aX3rU6uG8nS7"
		val userId_ALIOR = "6af374ae-480e-4631-b70c-4d8b2862e311"
		val appSecret_PEKAO = "P6tV0rO0mS1fE2tO6pG0oA1gI6gP6rO1cJ6bR1kG5pM8lJ1gQ8"
		val userId_PEKAO = "6c316ebb-c3b1-4de9-95de-2143432b8411"
		enum class scopeValues(val printableName: String) {
			AIS("ais"),
			AIS_ACC("ais-accounts"),
			PIS("pis")
		}
		val TOKEN_TYPE = "Bearer"
		val REDIRECT_URI = "http://azazada.pm/pi"/////////////////////////////////////////////////////

	}
}