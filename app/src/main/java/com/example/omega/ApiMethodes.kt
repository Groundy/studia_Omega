package com.example.omega

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.lang.Exception
import org.json.JSONObject

import org.json.JSONArray

import org.json.JSONException







class ApiMethodes {
	companion object{
		/*
		fun AIS_deleteConsent(){
			//Usuwa zezwolenie / Removes consent
		}
		fun AIS_getAccount(){
			//Uzyskanie szczegółowych informacji o koncie płatniczym użytkownika / Get detailed information about user payment account
			//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
		}
		fun AIS_getAccounts(){
			//Uzyskanie informacji na temat wszystkich kont płatniczych użytkownika / Get information about all user's payment account
			//Identyfikacja użytkownika na podstawie tokena dostępu / User identification based on access token
		}
		fun AIS_getHolds(){
			//Pobranie informacji o blokadach na koncie użytkownika / Get list of user's held operations
		}
		fun AIS_getTransactionDetail(){
			//Pobranie szczegółowych informacji o pojedynczej transkacji użytkownika / Get detailed information about user's single transaction
		}
		fun AIS_getTransactionsCancelled(){
			//Pobranie informacji o anulowanych transakcjach użytkownika / Get list of user cancelled transactions
		}
		fun AIS_getTransactionsDone(){
			//Pobranie informacji o zaksięgowanych transakcjach użytkownika / Get list of user done transactions
		}
		fun AIS_getTransactionsPending(){
			//Pobranie informacji o oczekujących transakcjach użytkownika / Get list of user's pending transactions
		}
		fun AIS_getTransactionsRejected(){
			//Pobranie informacji o odrzuconych transakcjach użytkownika / Get list of user's rejected transactions
		}
		fun AIS_getTransactionsScheduled(){
			//Pobranie informacji o zaplanowanych transakcjach użytkownika / Get list of user scheduled transactions
		}

		//AS

		fun AS_authorize(){
			//Żądanie kodu autoryzacji OAuth2 / Requests OAuth2 authorization code
		}
		fun AS_authorizeExt(){
			/*
			Żądanie wydania kodu autoryzacji OAuth2 na podstawie jednorazowego kodu autoryzacji wydanego przez narzędzie autoryzacji zewnętrznej.
			Kod autoryzacji zostanie dostarczony do TPP jako zapytanie zwrotne z ASPSP, jeśli uwierzytelnienie PSU zostanie potwierdzone przez EAT.
			Funkcja zwrotna musi zapewniać podobne powiadomienie również w przypadku nieudanego uwierzytelnienia lub jego rezygnacji.
			Requests OAuth2 authorization code based One-time authorization code issued by External Authorization Tool.
			Authorization code will be delivered to TPP as callback request from ASPSP if PSU authentication is confirmed by EAT.
			Callback function must provide similar notification also in case of unsuccessful authentication or its abandonment.
			 */
		}
		fun AS_token(){
			//Żądanie wydania tokena dostępu OAuth2 / Requests OAuth2 access token value
		}

		//CAF

		fun CAF_getConfirmationOfFunds(){
			/*
			Potwierdzenie dostępności na rachunku płatnika kwoty niezbędnej do wykonania transakcji płatniczej, o której mowa w Art. 62 PSD2.
			Confirming the availability on the payers account of the amount necessary to execute the payment transaction, as defined in Art. 65 PSD2.
			*/
		}

		//PIS

		fun PIS_bundle(){
			//Inicjacja wielu przelewów / Initiate many transfers as bundle
		}
		fun PIS_cancelPayments(){
			//Anulowanie zaplanowanych płatności / Cancelation of future dated payment
		}
		fun PIS_cancelRecurringPayment(){
			//Anulowanie płatności cyklicznej / Cancelation of recurring payment
		}
		fun PIS_domestic(){
			//Inicjacja przelewu krajowego / Initiate domestic transfer
		}
		fun PIS_eEA(){
			//Inicjacja przelewów zagranicznych SEPA / Initiate SEPA foreign transfers
		}
		fun PIS_getBundle(){
			//Uzyskanie status paczki przelewów / Get the status of bundle of payments
		}
		fun PIS_getMultiplePayments(){
			//Uzyskanie statusu wielu płatności / Get the status of multiple payments
		}
		fun PIS_getPayment(){
			//Uzyskanie statusu płatności / Get the status of payment
		}
		fun PIS_getRecurringPayment(){
			//Uzyskanie status płatności cyklicznej / Get the status of recurring payment
		}
		fun PIS_nonEEA(){
			//Inicjacja przelewów zagranicznych niezgodnych z SEPA / Initiate non SEPA foreign transfers
		}
		fun PIS_recurring(){
			//Definicja nowej płatności cyklicznej / Defines new recurring payment
		}
		fun PIS_tax(){
			//Inicjacja przelewu do organu podatkowego / Initiate tax transfer
		}

		 */
	}
}