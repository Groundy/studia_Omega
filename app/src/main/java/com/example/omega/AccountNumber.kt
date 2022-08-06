package com.example.omega

import android.util.Log
import java.math.BigInteger

enum class Countries(val codeLength : Int){
	PL(28)
}

class AccountNumber(number : String) {
	private var numberWithOutCountryCode : String = "63249000050000400030900682"
	private val country = Countries.PL
	private var errorInCreation : Boolean = false

	init{
		var tmpNumber = number
		try {
			val lengthOk = tmpNumber.length > 15
			if(!lengthOk)
				throw Exception("Too short number passed")

			val isDisplayForm = tmpNumber.contains(" ")
			if(isDisplayForm)
				tmpNumber = tmpNumber.replace(" ", "")

			val countryCodeLen = ApiConsts.countryCodeLength
			val hasCountryCode = tmpNumber.substring(0,countryCodeLen).toIntOrNull() == null
			if(hasCountryCode)
				tmpNumber = tmpNumber.substring(countryCodeLen, tmpNumber.length)

			numberWithOutCountryCode = tmpNumber
			errorInCreation = false
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[constructor/ AccountNumber] $e")
			errorInCreation = true
		}
	}

	fun checkIfIsProperIbanFormar() : Boolean{
		val countryCode = country.name
		val constValSubstraction = 55
		val firstCharVal = countryCode.first().code - constValSubstraction
		val secondCharVal = countryCode.last().code - constValSubstraction
		val countryCodeAsDigitStr = "$firstCharVal$secondCharVal"

		val checkSum = numberWithOutCountryCode.substring(0,2)
		val bban = numberWithOutCountryCode.substring(2, numberWithOutCountryCode.length)
		val changeOverReplaced = bban.plus(countryCodeAsDigitStr).plus("00")
		val bigInt = BigInteger(changeOverReplaced)
		val modRes = bigInt % BigInteger("97")
		val properCheckSum = 98 - modRes.toInt()

		val formatOk = properCheckSum == checkSum.toInt()
 		return formatOk
	}
}