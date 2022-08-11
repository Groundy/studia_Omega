package com.example.omega

import android.annotation.SuppressLint
import android.util.Log
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.omega.Utilities.Companion.TagProduction
import java.text.SimpleDateFormat
import java.util.*

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
		fun getSecondsToStampExpiration(timeStampCreation: String, validityPeriod: Int) : Long{
			val expirationInstant = try{
				Instant.parse(timeStampCreation).plusSeconds(validityPeriod.toLong())
			}catch (e : Exception){
				Log.e(TagProduction, "[getSecondsToStampExpiration/${this::class.java.name}] passed time stamp in wrong format")
				return -1
			}

			val currentTimeInstant = Instant.parse(getCurrentTime())
			return ChronoUnit.SECONDS.between(currentTimeInstant, expirationInstant) //secondsToExpiration
		}
		fun getDate(daysBack : Int = 0, yearsFirst: Boolean = true) : String{
			val c: Calendar = Calendar.getInstance()
			c.timeInMillis -= daysBack * 24 * 60 * 60 * 1000

			val y = (c.get(Calendar.YEAR)).toString()
			var m = (c.get(Calendar.MONTH) + 1).toString()//0-11 -> 1-12
			var d = (c.get(Calendar.DAY_OF_MONTH)).toString()

			if(m.length == 1)
				m = "0${m}"
			if(d.length == 1)
				d = "0${d}"

			if(yearsFirst)
				return "$y-$m-$d"
			else
				return "$d-$m-$y"
		}
		fun converTimeStampToEpoch(timestamp: String?) : Long{
			return try{
				Instant.parse(timestamp).epochSecond
			}catch (e : Exception){
				Log.e(TagProduction, "[converTimeStampToEpoch/${this::class.java.name}] passed time stamp in wrong format")
				Instant.now().minusSeconds(60*24*3600).epochSecond
			}
		}
		fun convertTimeToDisplay(input: String) : String{
			return try {
				val str = input.substring(0,10)
				val parts = str.split("-")
				"${parts[2]}-${parts[1]}-${parts[0]}"
			}catch (e : Exception){
				"Date unkown"
			}
		}
		@SuppressLint("SimpleDateFormat")
		fun convertDateToLong(date: String): Long {
			return try {
				val df = SimpleDateFormat("dd-MM-yyyy")
				val time = df.parse(date).time
				time
			}catch (e : Exception){
				Log.e(TagProduction, "wrong date passed to [convertDateToLong/Omegatime] str = $date")
				0L
			}

		}
	}
}