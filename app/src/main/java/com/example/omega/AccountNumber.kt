package com.example.omega

import android.util.Log
import java.math.BigInteger

enum class Countries(val codeLength : Int){
	PL(28)
}

class AccountNumber(number : String) {
	companion object{
		fun checkIfIsProperIbanFormar(
			numberOnlyDigits: String,
			country: Countries = Countries.PL
		): Boolean {
			val countryCode = country.name
			val constValSubstraction = 55
			val firstCharVal = countryCode.first().code - constValSubstraction
			val secondCharVal = countryCode.last().code - constValSubstraction
			val countryCodeAsDigitStr = "$firstCharVal$secondCharVal"

			val checkSum = numberOnlyDigits.substring(0, 2)
			val bban = numberOnlyDigits.substring(2, numberOnlyDigits.length)
			val changeOverReplaced = bban.plus(countryCodeAsDigitStr).plus("00")
			val bigInt = BigInteger(changeOverReplaced)
			val modRes = bigInt % BigInteger("97")
			val properCheckSum = 98 - modRes.toInt()

			return properCheckSum == checkSum.toInt()
		}
	}
	private var numberOnlyDigits : String = String()
	private val country = Countries.PL
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

			numberOnlyDigits = tmpNumber
			if(!checkIfIsProperIbanFormar()){
				throw Exception("Not Iban Format")
			}
		}catch (e : Exception){
			Log.e(Utilities.TagProduction, "[constructor/ AccountNumber] $e")
		}
	}


	fun checkIfIsProperIbanFormar(): Boolean {
		val countryCode = country.name
		val constValSubstraction = 55
		val firstCharVal = countryCode.first().code - constValSubstraction
		val secondCharVal = countryCode.last().code - constValSubstraction
		val countryCodeAsDigitStr = "$firstCharVal$secondCharVal"

		val checkSum = numberOnlyDigits.substring(0, 2)
		val bban = numberOnlyDigits.substring(2, numberOnlyDigits.length)
		val changeOverReplaced = bban.plus(countryCodeAsDigitStr).plus("00")
		val bigInt = BigInteger(changeOverReplaced)
		val modRes = bigInt % BigInteger("97")
		val properCheckSum = 98 - modRes.toInt()

		return properCheckSum == checkSum.toInt()
	}
	fun toStringWithCountry(): String {
		return "${country.name}$numberOnlyDigits"
	}
	fun toStringWithoutCountry(): String {
		return numberOnlyDigits
	}

	fun toDisplay() : String{
		//var toRet = "${country.name}${numberWithOutCountryCode.subSequence(0,2)}"
		var toRet = String()
		val parts = numberOnlyDigits.subSequence(2, numberOnlyDigits.length).chunked(4)
		parts.forEach{
			toRet = toRet.plus(" ").plus(it)
		}
		return toRet
	}
	fun lengthOK(): Boolean {
		return ApiFunctions.getLengthOfCountryBankNumberWitchCountryCode() == numberOnlyDigits.length + ApiConsts.countryCodeLength
	}
	fun isOk() : Boolean{
		val ibanOk = checkIfIsProperIbanFormar()
		return ibanOk && lengthOK()
	}
}