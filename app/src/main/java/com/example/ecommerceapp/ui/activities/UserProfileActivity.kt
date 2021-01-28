package com.example.ecommerceapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.User
import com.example.ecommerceapp.utils.Constants
import com.example.ecommerceapp.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException


class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)


        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // get user details from intent as a parcelableExtra.
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        ed_ProfileFirstName.setText(mUserDetails.firstName)
        ed_ProfileLastName.setText(mUserDetails.lastName)
        ed_ProfileEmail.isEnabled = false
        ed_ProfileEmail.setText(mUserDetails.email)

        if (mUserDetails.profileCompleted == 0) {
            tv_title.text= resources.getString(R.string.title_complete_profile)
            ed_ProfileFirstName.isEnabled = false
            ed_ProfileLastName.isEnabled = false

        } else {
            tv_title.text = resources.getString(R.string.title_edit_profile)
            GlideLoader(this@UserProfileActivity).loadUserPicture(mUserDetails.image,iv_user_photo)

            if (mUserDetails.mobile != 0L ) {
                ed_ProfileMobile.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender == Constants.MALE) {
                rb_male.isChecked = true
            } else {
                rb_female.isChecked = true
            }
        }

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        submitButton.setOnClickListener(this@UserProfileActivity)
    }

    override fun onClick(v: View?) {
        if(v != null) {
            when(v.id) {

                R.id.iv_user_photo -> {
                    // here we will check if the permission is already allowed or we need to request for it.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ){
                        Constants.showImageChooser(this)
                        //showErrorSnackBar("You already have the storage permission.", false)
                    } else {
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )

                    }
                }

                R.id.submitButton -> {
                    if (validateUserProfileDetails()) {

                        showProgressDialog(resources.getString(R.string.please_wait))
                        if(mSelectedImageFileUri != null)
                            FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageFileUri, Constants.USER_PROFILE_IMAGE)
                        else{
                            updateUserProfileDetails()
                        }
                    }
                }
            }
        }
    }

    private fun updateUserProfileDetails(){
      //  FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageFileUri)

        val userHashMap = HashMap<String, Any>()

        val firstName = ed_ProfileFirstName.text.toString().trim { it <= ' '}
        if (firstName != mUserDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }
        val lastName = ed_ProfileLastName.text.toString().trim { it <= ' '}
        if (lastName != mUserDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = firstName
        }

        val mobileNumber = ed_ProfileMobile.text.toString().trim{ it <= ' '}
        val gender = if(rb_male.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }

        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        if (gender.isNotEmpty() && gender != mUserDetails.gender){
            userHashMap[Constants.GENDER] = gender
        }

        // storing gender in userhashmap
        userHashMap[Constants.GENDER] = gender

        userHashMap[Constants.COMPLETE_PROFILE] = 1
        //showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    fun userProfileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this@UserProfileActivity, resources.getString(R.string.msg_profile_update_success),Toast.LENGTH_SHORT).show()

        startActivity(Intent(this@UserProfileActivity, dashboardActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            // if permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            //showErrorSnackBar("The storage permission is granted.", false)
            } else {
                // displaying another toast if permission is not granted
                Toast.makeText(this, resources.getString(R.string.read_storage_permission_denied),
                Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if(data != null) {
                    try {
                        // the uri of selected image from the storage.
                       mSelectedImageFileUri = data.data!!

                        // iv_user_photo.setImageURI(Uri.parse(selectedImageFileUri.toString()))
                        GlideLoader(this).loadUserPicture(mSelectedImageFileUri!!, iv_user_photo)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this,resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
                    }
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {


            }
        }

    private fun validateUserProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(ed_ProfileMobile.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL : String) {
        // hideProgressDialog()

        mUserProfileImageURL = imageURL
        updateUserProfileDetails()
    }





}