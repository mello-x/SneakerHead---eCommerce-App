package com.example.ecommerceapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.Address
import com.example.ecommerceapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_edit_address.*
import kotlinx.android.synthetic.main.activity_address_list.*

class AddEditAddressActivity : BaseActivity() {

    private var mAddressDetails: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)

        if(intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)
        }

        if(mAddressDetails != null) {
            if (mAddressDetails!!.id.isNotEmpty()) {
                tv_title_add_address.text = resources.getString(R.string.title_edit_address)
                addressAddSubmitButton.text = resources.getString(R.string.btn_lbl_update)

                ed_address_fullName.setText(mAddressDetails?.name)
                ed_address_phoneNumber.setText(mAddressDetails?.mobileNumber)
                ed_address_address.setText(mAddressDetails?.address)
                ed_address_zipCode.setText(mAddressDetails?.zipCode)
                ed_address_additionalNote.setText(mAddressDetails?.additionalNote)

                when(mAddressDetails?.type) {
                    Constants.HOME -> {
                        rb_home.isChecked = true
                    }
                    Constants.OFFICE -> {
                        rb_office.isChecked = true
                    }
                    else -> {
                        rb_other.isChecked = true
                        ed_address_noteForOther.visibility = View.VISIBLE
                        ed_address_noteForOther.setText(mAddressDetails?.otherDetails)
                    }

                }
            }
        }

        addressAddSubmitButton.setOnClickListener { saveAddressToFirestore() }

        rg_type.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_other) {
                ed_address_noteForOther.visibility = View.VISIBLE
            } else {
                ed_address_noteForOther.visibility = View.GONE
            }
        }
    }

    private fun saveAddressToFirestore() {
        val fullName: String = ed_address_fullName.text.toString().trim { it <= ' ' }
        val phoneNumber: String = ed_address_phoneNumber.text.toString().trim { it <= ' ' }
        val address: String = ed_address_address.text.toString().trim { it <= ' ' }
        val zipCode: String = ed_address_zipCode.text.toString().trim { it <= ' ' }
        val additionalNote: String = ed_address_additionalNote.text.toString().trim { it <= ' ' }
        val otherDetails: String = ed_address_noteForOther.text.toString().trim { it <= ' ' }

        if (validateData()) {
            showProgressDialog(resources.getString(R.string.please_wait))

            val addressType: String = when {
                rb_home.isChecked -> {
                    Constants.HOME
                }
                rb_office.isChecked -> {
                    Constants.OFFICE
                }
                else -> {
                    Constants.OTHER
                }
            }
            val addressModel = Address(
                    FirestoreClass().getCurrentUserID(),
                    fullName,
                    phoneNumber,
                    address,
                    zipCode,
                    additionalNote,
                    addressType,
                    otherDetails
            )
            if(mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
                FirestoreClass().updateAddress(this, addressModel, mAddressDetails!!.id)
            }
            else {
                FirestoreClass().addAddress(this, addressModel)
            }
        }
    }

    fun addUpdateAddressSuccess() {
        hideProgressDialog()

        val notifySuccessMessage: String = if( mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
            resources.getString(R.string.msg_your_address_updated_successfully)
        } else {
            resources.getString(R.string.err_your_address_added_successfully)
        }

        Toast.makeText(this@AddEditAddressActivity, notifySuccessMessage,Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun validateData(): Boolean {
        return when {

            TextUtils.isEmpty(ed_address_fullName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_please_enter_full_name),
                        true
                )
                false
            }

            TextUtils.isEmpty(ed_address_phoneNumber.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_please_enter_phone_number),
                        true
                )
                false
            }

            TextUtils.isEmpty(ed_address_address.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }

            TextUtils.isEmpty(ed_address_zipCode.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }

            rb_other.isChecked && TextUtils.isEmpty(
                    ed_address_zipCode.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }
            else -> {
                true
            }
        }
    }
}