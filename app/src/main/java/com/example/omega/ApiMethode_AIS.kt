package com.example.omega

class ApiMethode_AIS {
	fun deleteConsent(){
		//Usuwa zezwolenie / Removes consent
	}
	fun getAccount(){
		//Uzyskanie szczegółowych informacji o koncie płatniczym użytkownika / Get detailed information about user payment account
		//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
	}
	fun getAccounts(){
		//Uzyskanie informacji na temat wszystkich kont płatniczych użytkownika / Get information about all user's payment account
		//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
	}
	fun getHolds(){
		//Pobranie informacji o blokadach na koncie użytkownika / Get list of user's held operations
	}
	fun getTransactionDetail(){
		//Pobranie szczegółowych informacji o pojedynczej transkacji użytkownika / Get detailed information about user's single transaction
	}
	fun getTransactionsCancelled(){
		//Pobranie informacji o anulowanych transakcjach użytkownika / Get list of user cancelled transactions
	}
	fun getTransactionsDone(){
		//Pobranie informacji o zaksięgowanych transakcjach użytkownika / Get list of user done transactions
	}
	fun getTransactionsPending(){
		//Pobranie informacji o oczekujących transakcjach użytkownika / Get list of user's pending transactions
	}
	fun getTransactionsRejected(){
		//Pobranie informacji o odrzuconych transakcjach użytkownika / Get list of user's rejected transactions
	}
	fun getTransactionsScheduled(){
		//Pobranie informacji o zaplanowanych transakcjach użytkownika / Get list of user scheduled transactions
	}
}