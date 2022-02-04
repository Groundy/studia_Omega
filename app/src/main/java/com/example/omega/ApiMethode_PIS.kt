package com.example.omega

class ApiMethode_PIS {
	fun bundle(){
		//Inicjacja wielu przelewów / Initiate many transfers as bundle
	}
	fun cancelPayments(){
		//Anulowanie zaplanowanych płatności / Cancelation of future dated payment
	}
	fun cancelRecurringPayment(){
		//Anulowanie płatności cyklicznej / Cancelation of recurring payment
	}
	fun domestic(){
		//Inicjacja przelewu krajowego / Initiate domestic transfer
	}
	fun eEA(){
		//Inicjacja przelewów zagranicznych SEPA / Initiate SEPA foreign transfers
	}
	fun getBundle(){
		//Uzyskanie status paczki przelewów / Get the status of bundle of payments
	}
	fun getMultiplePayments(){
		//Uzyskanie statusu wielu płatności / Get the status of multiple payments
	}
	fun getPayment(){
		//Uzyskanie statusu płatności / Get the status of payment
	}
	fun getRecurringPayment(){
		//Uzyskanie status płatności cyklicznej / Get the status of recurring payment
	}
	fun nonEEA(){
		//Inicjacja przelewów zagranicznych niezgodnych z SEPA / Initiate non SEPA foreign transfers
	}
	fun recurring(){
		//Definicja nowej płatności cyklicznej / Defines new recurring payment
	}
	fun tax(){
		//Inicjacja przelewu do organu podatkowego / Initiate tax transfer
	}
}