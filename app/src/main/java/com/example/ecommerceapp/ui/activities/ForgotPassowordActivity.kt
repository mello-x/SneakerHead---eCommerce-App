package com.example.ecommerceapp.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.example.ecommerceapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_passoword.*

class ForgotPassowordActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_passoword)

        submit_Button.setOnClickListener {
            val email: String = ed_forgetEmail.text.toString().trim { it <= ' '}
            if (email.isEmpty()) {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)

            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener{ task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            Toast.makeText(this, resources.getString(R.string.email_sent_success), Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            }
        }
    }
}