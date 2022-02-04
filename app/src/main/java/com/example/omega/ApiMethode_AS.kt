package com.example.omega

class ApiMethode_AS {
	fun authorize(){
		//Żądanie kodu autoryzacji OAuth2 / Requests OAuth2 authorization code
	}
	fun authorizeExt(){
		/*
		Żądanie wydania kodu autoryzacji OAuth2 na podstawie jednorazowego kodu autoryzacji wydanego przez narzędzie autoryzacji zewnętrznej.
		Kod autoryzacji zostanie dostarczony do TPP jako zapytanie zwrotne z ASPSP, jeśli uwierzytelnienie PSU zostanie potwierdzone przez EAT.
		Funkcja zwrotna musi zapewniać podobne powiadomienie również w przypadku nieudanego uwierzytelnienia lub jego rezygnacji.
		Requests OAuth2 authorization code based One-time authorization code issued by External Authorization Tool.
		Authorization code will be delivered to TPP as callback request from ASPSP if PSU authentication is confirmed by EAT.
		Callback function must provide similar notification also in case of unsuccessful authentication or its abandonment.
		 */
	}
	fun register(){
		//Żądanie rejestracji aplikacji klienckiej / Client application registration request
	}
	fun token(){
		//Żądanie wydania tokena dostępu OAuth2 / Requests OAuth2 access token value
	}
}