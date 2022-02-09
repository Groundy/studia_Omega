package com.example.omega
import android.R.attr
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import android.R.attr.publicKey
import java.security.PrivateKey


class ApiFuncs {
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

		fun generate_X_REQUEST_ID() : UUID{
			val random = Random()
			val random63BitLong = random.nextLong() and 0x3FFFFFFFFFFFFFFFL
			val variant1BitFlag : Long = (-0x6000000000000000L).toLong()
			val least64SigBits = random63BitLong + variant1BitFlag

			val start: LocalDateTime = LocalDateTime.of(1582, 10, 15, 0, 0, 0)
			val duration: Duration = Duration.between(start, LocalDateTime.now())
			val timeForUuidIn100Nanos = duration.seconds * 10000000 + duration.nano * 100
			val least12SignificantBitOfTime = timeForUuidIn100Nanos and 0x000000000000FFFFL shr 4
			val version = (1 shl 12).toLong()
			val most64SigBits = (timeForUuidIn100Nanos and -0x10000L) + version + least12SignificantBitOfTime


			val uuid = UUID(most64SigBits, least64SigBits)
			return uuid
		}

	}

}