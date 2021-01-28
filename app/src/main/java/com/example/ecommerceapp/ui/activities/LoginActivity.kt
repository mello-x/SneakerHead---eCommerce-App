package com.example.ecommerceapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.User
import com.example.ecommerceapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tv_forgetPassword.setOnClickListener(this)
        login_Button.setOnClickListener(this)
        tv_register.setOnClickListener(this)

    }

    fun userLoggedInSuccess(user: User) {

        // hide the progress dialog
        hideProgressDialog()

        if(user.profileCompleted == 0) {
            // if the user profile is incomplete then launch the UserProfileActivity.
                val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
                startActivity(intent)
        } else {
            // redirect the user to main screen after log in.
            startActivity(Intent(this@LoginActivity, dashboardActivity::class.java))
        }
        finish()
    }

    // In login screen the clickable components are login button, forgetPassword text and register text.
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.tv_forgetPassword -> {
                    startActivity(Intent(this@LoginActivity, ForgotPassowordActivity::class.java))
                }
                R.id.login_Button -> {
                    logInRegisteredUser()
                }
                R.id.tv_register -> {
                    // launch the register screen when click on the text.
                    startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(ed_login.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(ed_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true

            }
        }
    }

    private fun logInRegisteredUser() {

        if(validateLoginDetails()) {

            // show the progess dialog
            showProgressDialog(resources.getString(R.string.please_wait))

            // get the text from editText and trim the space
            val email = ed_login.text.toString().trim { it <= ' '}
            val password = ed_password.text.toString().trim { it <= ' '}

            // login using firebaseauth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if(task.isSuccessful) {
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    }
                    else {
                        // hide progress dialog
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
        }
    }



}