package com.example.ecommerceapp.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tv_loginHere.setOnClickListener {
            onBackPressed()
        }
        registerButton.setOnClickListener{
            registerUser()
        }
    }

        // function to validate the entries of a new user.
    private fun validateRegisterDetails(): Boolean {
            return when {
                TextUtils.isEmpty(ed_firstName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                    false
                }
                TextUtils.isEmpty(ed_lastName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                    false
                }
                TextUtils.isEmpty(ed_emailID.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                    false
                }
                TextUtils.isEmpty(ed_passwordRegister.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                    false
                }
                TextUtils.isEmpty(ed_confirmPassword.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_confirm_password), true)
                    false
                }
                ed_passwordRegister.text.toString().trim { it <= ' '} != ed_confirmPassword.text.toString().trim { it <= ' '} -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_passowrd_and_confirm_password_mismatch),true)
                    false
                }
                !cb_term_and_conditions.isChecked -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_conditions),true)
                    false
                }
                else -> {
                   // showErrorSnackBar(resources.getString(R.string.registry_successful),false)
                    true
                }
            }
    }
    // Firebase email register
    private fun registerUser() {
        if (validateRegisterDetails()) {

            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = ed_emailID.text.toString().trim() { it <= ' '}
            val password: String = ed_passwordRegister.text.toString().trim() { it <= ' '}

            // Create an instance and create a register a user with email and password 
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                  // if the registration is successfully done
                    if (task.isSuccessful) {

                      //Firebase registered User
                      val firebaseUser : FirebaseUser = task.result!!.user!!

                        val user = User(
                            firebaseUser.uid,
                            ed_firstName.text.toString().trim { it <= ' '},
                            ed_lastName.text.toString().trim { it <= ' '},
                            ed_emailID.text.toString().trim { it <= ' '}
                        )

                        FirestoreClass().registerUser(this@RegisterActivity, user)

                        //FirebaseAuth.getInstance().signOut()
                        //finish()

                    } else {
                        hideProgressDialog()
                      //if registering isn't successful then show error message
                      showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                })
        }
    }

    fun userRegistrationSuccess() {

        // hide the progress dialog
        hideProgressDialog()

        Toast.makeText(this, resources.getString(R.string.registry_successful),Toast.LENGTH_LONG).show()
    }

}



