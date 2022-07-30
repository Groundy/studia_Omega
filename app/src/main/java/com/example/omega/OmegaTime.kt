package com.example.omega

import android.util.Log
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
			val timeToCheck = try{
				Instant.parse(timestamp)
			}catch (e : Exception){
				Log.w(TagProduction, "[timestampIsValid/${this::class.java.name}] passed time stamp in wrong format, probably its first time pref pass")
				return false
			}
			val timeCurrent =  Instant.parse(getCurrentTime()).plusMillis(minTimeMarginMili)
			return timeToCheck > timeCurrent
		}
		fun getSecondsToStampExpiration(timestampStartValidityPeriod: String, validityPeriod: Int) : Long{
			val expirationInstant = try{
				Instant.parse(timestampStartValidityPeriod).plusSeconds(validityPeriod.toLong())
			}catch (e : Exception){
				Log.e(TagProduction, "[getSecondsToStampExpiration/${this::class.java.name}] passed time stamp in wrong format")
				return -1
			}

			val currentTimeInstant = Instant.parse(getCurrentTime())
			return ChronoUnit.SECONDS.between(currentTimeInstant, expirationInstant) //secondsToExpiration
		}
	}
}