package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox

class PermissionList(){
	var permissions = arrayListOf<ApiConsts.Privileges>()
	private val separator = ";;;"

	override fun toString() : String{
		var toRet = String()
		ApiConsts.Privileges.values().forEach {
			if(permissions.contains(it))
				toRet = toRet.plus(it.text).plus(separator)
		}
		return toRet
	}
	constructor(inputString : String) : this() {
		ApiConsts.Privileges.values().forEach {
			if(inputString.contains(it.text))
				this.permissions.add(it)
		}
	}
	constructor(privilegesArray : List<ApiConsts.Privileges>) : this() {
		this.permissions = privilegesArray as ArrayList<ApiConsts.Privileges>
	}
	fun add(permission : ApiConsts.Privileges){
		this.permissions.add(permission)
	}
}


class UserPermissionList : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_user_permission_list)
		setListeners()
	}
	private fun setListeners(){
		findViewById<Button>(R.id.userPermisionList_continue_Button).setOnClickListener{okClicked()}
		findViewById<Button>(R.id.userPermisionList_cancel_Button).setOnClickListener{endActivity(false)}
	}

	private fun okClicked(){
		val permissionsList = PermissionList()

		if(findViewById<CheckBox>(R.id.userPermisionList_accDetails_checkBox).isChecked)
			permissionsList.add(ApiConsts.Privileges.accountsDetails)

		if(findViewById<CheckBox>(R.id.userPermisionList_accHistory_checkBox).isChecked)
			permissionsList.add(ApiConsts.Privileges.accountsHistory)

		endActivity(true, permissionsList)
	}
	private fun endActivity(result : Boolean, permissionList : PermissionList? = null){
		if(!result){
			setResult(RESULT_CANCELED)
			finish()
		}

		val serializedData = permissionList.toString()
		val dataField = getString(R.string.userPermissionList_outputField)
		val outputIntent = Intent().putExtra(dataField, serializedData)
		setResult(RESULT_OK, outputIntent)
		finish()
	}
}