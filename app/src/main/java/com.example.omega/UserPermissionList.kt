package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import com.example.omega.Utilities.Companion.TagProduction

class PermissionList(){
	companion object{
		private const val separator = ";;;"
	}
	var permissionsArray = arrayListOf<ApiConsts.Privileges>()

	override fun toString() : String{
		var toRet = String()
		ApiConsts.Privileges.values().forEach {
			if(permissionsArray.contains(it))
				toRet = toRet.plus(it.text).plus(separator)
		}
		return toRet
	}
	constructor(inputString : String) : this() {
		ApiConsts.Privileges.values().forEach {
			if(inputString.contains(it.text))
				permissionsArray.add(it)
		}
	}
	constructor(privilegesArray : List<ApiConsts.Privileges>) : this() {
		permissionsArray = privilegesArray as ArrayList<ApiConsts.Privileges>
	}
	constructor(vararg permissions : ApiConsts.Privileges) : this() {
		permissions.forEach {
			permissionsArray.add(permissionsArray.size, it)
		}
	}
	fun add(permission : ApiConsts.Privileges){
		this.permissionsArray.add(permission)
	}
}


class UserPermissionList : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		//supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_user_permission_list)
		setListeners()
	}
	private fun setListeners(){
		findViewById<Button>(R.id.userPermisionList_continue_Button).setOnClickListener{
			okClicked()}
		findViewById<Button>(R.id.userPermisionList_cancel_Button).setOnClickListener{
			endActivity(false)}
	}

	private fun okClicked(){
		val permissionsList = PermissionList()

		if(findViewById<CheckBox>(R.id.userPermisionList_accDetails_checkBox).isChecked)
			permissionsList.add(ApiConsts.Privileges.AccountsDetails)

		if(findViewById<CheckBox>(R.id.userPermisionList_accHistory_checkBox).isChecked)
			permissionsList.add(ApiConsts.Privileges.AccountsHistory)

		endActivity(true, permissionsList)
	}
	private fun endActivity(result : Boolean, permissionList : PermissionList? = null){
		if(!result){
			setResult(RESULT_CANCELED)
			finish()
		}

		val serializedData = permissionList.toString()
		val dataField = getString(R.string.ACT_COM_USERPERMISSIONLIST_FIELDNAME)
		val outputIntent = Intent().putExtra(dataField, serializedData)
		setResult(RESULT_OK, outputIntent)
		finish()
	}
}