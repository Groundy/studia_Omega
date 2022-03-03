package com.example.omega

class UserData  {
	class AccessTokenStruct{
		var tokenType : String? = null
		var tokenContent : String? = null
		fun setTokenType(tokenTypeToSet : String?) : AccessTokenStruct{
			this.tokenType = tokenTypeToSet
			return this
		}
		fun setTokenContent(tokenContToSet : String?) : AccessTokenStruct{
			this.tokenContent = tokenContToSet
			return this
		}
	}
	class Account{
		var accNumber : String? = null
		var accType : String? = null
		fun setType(type : String?) : Account{
			this.accType = type
			return this
		}
		fun setNumber(number : String?) : Account{
			this.accNumber = number
			return this
		}
	}

	companion object{
		var authCode : String? = null
		var accessTokenStruct : AccessTokenStruct? = null
		var accList : ArrayList<Account>? = null
	}
}