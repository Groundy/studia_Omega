package com.example.omega

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Spinner

class AccountHistroyActivity : AppCompatActivity() {
	private lateinit var spinner : Spinner
	private lateinit var list : ListView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_histroy)
	}

	private fun getToken() : Token?{
		
	}
}