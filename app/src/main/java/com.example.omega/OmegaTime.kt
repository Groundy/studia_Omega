package com.example.omega

import android.util.Log
import java.lang.Exception
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OmegaTime {
	companion object{
		fun getCurrentTime(secondsFromNow : Int = 0) : String{
			var time = Instant.now().plusSeconds(secondsFromNow.toLong())
			val currentTimeStr = DateTimeFormatter.ISO_INSTANT.format(time)
			return currentTimeStr
		}
		fun timestampIsValid(timestamp : String, minTimeMarginMili : Long = 100) : Boolean{
			val timeToCheck = Instant.parse(timestamp)
			val timeCurrent =  Instant.parse(getCurrentTime()).plusMillis(minTimeMarginMili)
			val isValid = timeToCheck > timeCurrent
			return isValid
		}
	}
}