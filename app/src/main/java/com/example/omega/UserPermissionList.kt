package com.example.omega

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton

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
		this.title = Utilities.strToEditable("Zgoda na:")

		setContentView(R.layout.activity_user_permission_list)
		setListeners()
	}
	private fun setListeners(){
		val okButton = findViewById<Button>(R.id.userPermisionList_continue_Button)
		okButton.setOnClickListener{okClicked()}
		val cancelButton = findViewById<Button>(R.id.userPermisionList_cancel_Button)
		cancelButton.setOnClickListener{endActivity(false)}

		val firstCheckBox = findViewById<CheckBox>(R.id.userPermisionList_accDetails_checkBox)
		val secondCheckBox = findViewById<CheckBox>(R.id.userPermisionList_accHistory_checkBox)
		val checkBoxListner = CompoundButton.OnCheckedChangeListener { _, isCheckd ->
			val allChecked = firstCheckBox.isChecked && secondCheckBox.isChecked
			okButton.isEnabled = allChecked
		}
		firstCheckBox.setOnCheckedChangeListener(checkBoxListner)
		secondCheckBox.setOnCheckedChangeListener(checkBoxListner)
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