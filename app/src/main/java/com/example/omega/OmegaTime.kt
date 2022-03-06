package com.example.omega

import android.util.Log
import java.lang.Exception
import java.util.*

class OmegaTime {
	companion object{

		fun  getCurrentTime(timeFromNow : Int = 0) : String{
			val c: Calendar = Calendar.getInstance()
			c.timeInMillis += 1000*timeFromNow

			var Y = (c.get(Calendar.YEAR)).toString()
			var M = (c.get(Calendar.MONTH) + 1).toString()//0-11 -> 1-12
			var D = (c.get(Calendar.DAY_OF_MONTH)).toString()
			var HH = (c.get(Calendar.HOUR_OF_DAY)).toString()
			var MM = (c.get(Calendar.MINUTE)).toString()
			var SS = (c.get(Calendar.SECOND)).toString()
			var ZZZ = (c.get(Calendar.MILLISECOND)).toString()

			if(M.length == 1)
				M = "0${M}"
			if(D.length == 1)
				D = "0${D}"
			if(HH.length == 1)
				HH = "0${HH}"
			if(MM.length == 1)
				MM = "0${MM}"
			if(SS.length == 1)
				SS = "0${SS}"
			if(ZZZ.length == 1)
				ZZZ = "00${ZZZ}"
			if(ZZZ.length == 2)
				ZZZ = "0${ZZZ}"

			return "${Y}-${M}-${D}T${HH}:${MM}:${SS}.${ZZZ}Z"
		}
		fun miliSecToLeft(endTime : String) : Long? {
			try{
				assert(endTime.length == 24)
				val c = Calendar.getInstance()
				//val Y = (c.get(Calendar.YEAR)).toString().toLong()
				//val M = (c.get(Calendar.MONTH) + 1).toString().toLong()//0-11 -> 1-12
				//val D = (c.get(Calendar.DAY_OF_MONTH)).toString().toLong()
				val hh = (c.get(Calendar.HOUR_OF_DAY)).toString().toLong()
				val mm = (c.get(Calendar.MINUTE)).toString().toLong()
				val ss = (c.get(Calendar.SECOND)).toString().toLong()
				val zzz = (c.get(Calendar.MILLISECOND)).toString().toLong()

				//val year = time.subSequence(0,4).toString().toLong()
				//val month = time.subSequence(5,7).toString().toLong()
				//val day = time.subSequence(8,10).toString().toLong()
				val hour = endTime.subSequence(11,13).toString().toLong()
				val minute = endTime.subSequence(14,16).toString().toLong()
				val sec = endTime.subSequence(17,19).toString().toLong()
				val miliSec = endTime.subSequence(20,23).toString().toLong()

				var milSecFromNow  = (hour - hh)*3600*1000 + (minute - mm)*60*1000 + (sec - ss)*1000 + (miliSec - zzz)
				if(milSecFromNow < 0)
					milSecFromNow += 24*3600*1000//next day

				return milSecFromNow
			}
			catch(e : Exception){
				Log.e("WookieTag", "Wrong format of time object string! [${e.toString()}]")
				return null
			}
		}

	}
}