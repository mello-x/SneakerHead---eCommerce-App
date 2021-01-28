package com.example.ecommerceapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.User
import com.example.ecommerceapp.utils.Constants
import com.example.ecommerceapp.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        tv_edit.setOnClickListener(this)
        logoutButton.setOnClickListener(this)
        ll_address.setOnClickListener(this)
    }
    override fun onResume() {
        super.onResume()
        getUserDetails()
    }
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id){
                R.id.tv_edit -> {
                    val intent = Intent(this@SettingActivity, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }
                R.id.logoutButton -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@SettingActivity,LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                R.id.ll_address -> {
                    val intent = Intent(this@SettingActivity, AddressListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getUserDetails(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this@SettingActivity)
    }

    fun userDetailsSuccess(user : User) {
        mUserDetails = user
        hideProgressDialog()

        GlideLoader(this@SettingActivity).loadUserPicture(user.image, iv_user_photo)
        tv_name.text = "${user.firstName} ${user.lastName}"
        tv_gender.text = user.gender
        tv_email.text = user.email
        tv_mobile_number.text = "${user.mobile}"
    }





}