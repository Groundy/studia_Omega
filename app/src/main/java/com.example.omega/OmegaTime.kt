package com.example.omega

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.omega.Utilities.Companion.TagProduction

class OmegaTime {
	companion object{
		fun getCurrentTime(secondsFromNow : Int = 0) : String{
			val time = Instant.now().plusSeconds(secondsFromNow.toLong())
			return DateTimeFormatter.ISO_INSTANT.format(time)
		}
		fun timestampIsValid(timestamp : String, minTimeMarginMili : Long = 100) : Boolean{
			val timeToCheck = Instant.parse(timestamp)
			val timeCurrent =  Instant.parse(getCurrentTime()).plusMillis(minTimeMarginMili)
			return timeToCheck > timeCurrent
		}
		fun getSecondsToStampExpiration(timestampStartValidityPeriod: String, validityPeriod: Int) : Long{
			val expirationInstant = Instant.parse(timestampStartValidityPeriod).plusSeconds(validityPeriod.toLong())
			val currentTimeInstant = Instant.parse(getCurrentTime())
			val secondsToExpiration = ChronoUnit.SECONDS.between(currentTimeInstant, expirationInstant)
			return secondsToExpiration
		}
	}
}