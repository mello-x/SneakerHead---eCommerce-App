package com.example.ecommerceapp.ui.activities

import android.app.Dialog
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ecommerceapp.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_progress.*



open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressaDialog: Dialog

    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        if(errorMessage) {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.colorSnackBarError
                )
            )
        } else {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.colorSnackBarSuccess
                )
            )
        }
        snackBar.show()

    }

    fun showProgressDialog(text: String) {
        mProgressaDialog = Dialog(this)


        //the resource will be inflated, add all top-level views to the screen
        mProgressaDialog.setContentView(R.layout.dialog_progress)

        mProgressaDialog.tv_progress_text.text = text

        mProgressaDialog.setCancelable(false)
        mProgressaDialog.setCanceledOnTouchOutside(false)

        // start the dialog and display it on screen
        mProgressaDialog.show()
    }

    fun hideProgressDialog() {
        mProgressaDialog.dismiss()
    }

    fun doubleBackToExit() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true

        Toast.makeText(this, resources.getString(R.string.please_click_back_again_to_exit),Toast.LENGTH_SHORT).show()

        @Suppress("DEPRECATION")
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}
