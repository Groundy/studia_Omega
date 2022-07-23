package com.example.omega

import android.util.Log
import org.json.JSONObject

class UserData  {
	class AccessTokenStruct{
		var tokenType : String? = null
		var tokenContent : String? = null
		var scope : ApiConsts.ScopeValues? = null
		var expirationTime : String? = null
		var refreshToken : String? = null
		var listOfAccounts : ArrayList<PaymentAccount>? = null

		fun setTokenType(tokenTypeToSet : String?) : AccessTokenStruct {
			this.tokenType = tokenTypeToSet
			return this
		}
		fun setTokenContent(tokenContToSet : String?) : AccessTokenStruct {
			this.tokenContent = tokenContToSet
			return this
		}
		fun setTokenScope(scope : ApiConsts.ScopeValues?) : AccessTokenStruct {
			this.scope = scope
			return this
		}
		fun setTokenExpirationTime(time: String) : AccessTokenStruct {
			this.expirationTime = time
			return this
		}
		fun setRefreshToken(refreshToken: String) : AccessTokenStruct {
			this.refreshToken = refreshToken
			return this
		}
		fun hasNotExpired() : Boolean{
			//todo
			return true
		}
		fun refresh(){
			//todo
		}
		fun addAccounts(list : ArrayList<PaymentAccount>?) : AccessTokenStruct {
			this.listOfAccounts = list
			return this
		}
		fun swapPaymentAccountToFilledOne(tmpPaymentAccount : PaymentAccount) : Boolean{
			val accList = this.listOfAccounts!!
			for (i in 0..accList.size){
				val accountMaths = accList[i].accNumber == tmpPaymentAccount.accNumber
				if(accountMaths){
					accList[i] = tmpPaymentAccount
					return true
				}
			}
			return false
		}
		fun getBalanceOfAccount(accountNumber : String) : Double?{
			listOfAccounts?.forEach{
				if(it.accNumber == accountNumber)
					return it.availableBalance
			}
			return null
		}
		fun getCurrencyOfAccount(accountNumber : String) : String?{
			listOfAccounts?.forEach{
				if(it.accNumber == accountNumber)
					return it.currency
			}
			return null
		}
	}
	class PaymentAccount{
		var accNumber : String = String()
		var accType : String? = null
		var currency :String? = null
		var availableBalance : Double? = null
		var bookingBalance : Double? = null
		var accountHolderType : String? = null
		var bankName  : String? = null
		var bankAddress : String? = null
		var ownerName : String? = null

		fun isValid() : Boolean{
			val isWrong =
				accNumber.isNullOrEmpty() ||
				accType.isNullOrEmpty() ||
				currency.isNullOrEmpty() ||
				availableBalance == null ||
				bookingBalance == null ||
				accountHolderType.isNullOrEmpty() ||
				bankName.isNullOrEmpty() ||
				bankAddress.isNullOrEmpty() ||
				ownerName.isNullOrEmpty()

			return !isWrong
		}
		constructor(responseObj : JSONObject){
			try{
				val accountDetailsObj = responseObj.getJSONObject("account")
				val accNumber = accountDetailsObj.getString("accountNumber")
				val accountTypeName = accountDetailsObj.getString("accountTypeName")
				val currency = accountDetailsObj.getString("currency")
				val availableBalance = accountDetailsObj.getString("availableBalance")
				val bookingBalance = accountDetailsObj.getString("bookingBalance")
				val accountHolderType = accountDetailsObj.getString("accountHolderType")
				val bankName = accountDetailsObj.getJSONObject("bank").getString("name")

				val bankAddressArray = accountDetailsObj.getJSONObject("bank").getJSONObject("address").getJSONArray("value")
				var bankAddress = ""
				for (i in 0 until bankAddressArray.length())
					bankAddress += "${bankAddressArray[i]},"
				bankAddress = bankAddress.substring(0,bankAddress.length-1)

				val ownerNameArray = accountDetailsObj.getJSONObject("nameAddress").getJSONArray("value")
				var ownerName = ""
				for (i in 0 until ownerNameArray.length())
					ownerName += "${ownerNameArray[i]},"
				ownerName = ownerName.substring(0,ownerName.length-1)

				this.accNumber = accNumber
				this.accType = accountTypeName
				this.currency = currency
				this.availableBalance = availableBalance.toDoubleOrNull()
				this.bookingBalance = bookingBalance.toDoubleOrNull()
				this.accountHolderType = accountHolderType
				this.bankName = bankName
				this.bankAddress = bankAddress
				this.ownerName = ownerName
				this.ownerName = ownerName
			}catch (e : Exception){
				Log.e(Utilities.TagProduction, "Failed to convert response body from getAccount methode to Account obj [${e.toString()}]")
			}
		}
		constructor(accNumber: String){
			this.accNumber = accNumber
		}
		fun getDisplayString() : String{
			return "[${availableBalance.toString()} $currency]  $accNumber"
		}
	}

	companion object{
		var accessTokenStruct : AccessTokenStruct? = null
	}
}