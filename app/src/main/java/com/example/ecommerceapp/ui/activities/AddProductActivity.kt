package com.example.ecommerceapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.Products
import com.example.ecommerceapp.utils.Constants
import com.example.ecommerceapp.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException
import java.lang.Exception

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSelectedImageFileURI : Uri? = null
    private var mProductImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        iv_add_update_product.setOnClickListener(this)
        submit_Button_addProduct.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v != null ) {
            when (v.id) {
                R.id.iv_add_update_product -> {
                    if(ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            == PackageManager.PERMISSION_GRANTED
                    )  {
                        Constants.showImageChooser(this@AddProductActivity)
                    } else {
                        // request permissions to be granted to this application.
                        ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }
                R.id.submit_Button_addProduct -> {
                    if(validateProductDetails()) {
                        uploadProductImage()
                    }
                }
            }
        }
    }


    private fun uploadProductImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        // upload image with the product_image name to the cloud storage in firebase
        FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageFileURI, Constants.PRODUCT_IMAGE)
    }

    fun productUploadSuccess(){
        hideProgressDialog()
        Toast.makeText(this@AddProductActivity, resources.getString(R.string.product_uploaded_success), Toast.LENGTH_SHORT).show()
        finish()
    }

    fun imageUploadSuccess(imageURL : String) {
       /* hideProgressDialog()
        showErrorSnackBar("Product image is uploaded successfully. Image URL: $imageURL", false)
        */
        mProductImageURL = imageURL

        uploadProductDetails()

    }

    private fun uploadProductDetails() {
        val username = this.getSharedPreferences(
                Constants.MELLOSHOP_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USERNAME,"")!!
        val product = Products(
                FirestoreClass().getCurrentUserID(),
                username,
                ed_product_title.text.toString().trim{ it <= ' '},
                ed_product_price.text.toString().trim{ it <= ' '},
                ed_product_description.text.toString().trim{ it <= ' '},
                ed_product_quantity.text.toString().trim{ it <= ' '},
                mProductImageURL
        )
        FirestoreClass().uploadProductDetails(this, product)
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
                Constants.showImageChooser(this@AddProductActivity)
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
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {

            // Replace the add icon with edit icon once the image is selected.
            iv_add_update_product.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AddProductActivity,
                    R.drawable.ic_vector_edit
                )
            )

            // The uri of selection image from phone storage.
            mSelectedImageFileURI = data.data!!

            try {
                // Load the product image in the ImageView.
                GlideLoader(this@AddProductActivity).loadProductPicture(
                    mSelectedImageFileURI!!,
                    iv_product_image
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            mSelectedImageFileURI == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }
            TextUtils.isEmpty(ed_product_title.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title),true)
                false
            }
            TextUtils.isEmpty(ed_product_price.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price),true)
                false
            }
            TextUtils.isEmpty(ed_product_description.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description),true)
                false
            }
            TextUtils.isEmpty(ed_product_quantity.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_product_quantity),true)
                false
            } else -> {
                true
            }
        }
    }

}

