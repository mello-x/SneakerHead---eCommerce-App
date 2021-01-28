package com.example.ecommerceapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.CartItem
import com.example.ecommerceapp.model.Products
import com.example.ecommerceapp.utils.Constants
import com.example.ecommerceapp.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var mProductId: String = ""
    private lateinit var mProductDetails: Products
    private var mProductOwnerId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        if(intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
          mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }

        //var productOwnerId : String = ""
        if(intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }
        if(FirestoreClass().getCurrentUserID() == mProductOwnerId) {
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
        } else {
            btn_add_to_cart.visibility = View.VISIBLE
        }
        getProductDetails()

        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)

    }

    private fun getProductDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductDetails(this, mProductId)
    }

    fun productExistsInCart() {
        hideProgressDialog()
        btn_go_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productDetailsSuccess(products: Products) {
        mProductDetails = products
        //hideProgressDialog()
        GlideLoader(this@ProductDetailsActivity).loadProductPicture(products.image,iv_product_detail_image)
        tv_product_details_title.text = products.title
        tv_product_details_price.text = "$${products.price}"
        tv_product_details_description.text = products.description
        tv_product_details_stock_quantity.text = products.stock_quantity

        if(products.stock_quantity.toInt() == 0) {
            hideProgressDialog()

            btn_add_to_cart.visibility = View.GONE
            tv_product_details_stock_quantity.text = resources.getString(R.string.out_of_stock)
            tv_product_details_stock_quantity.setTextColor(ContextCompat.getColor(this, R.color.colorSnackBarError))
        }
        else {
            if(FirestoreClass().getCurrentUserID() == products.user_id) {
                hideProgressDialog()
            } else {
                FirestoreClass().checkIfItemExistInCart(this, mProductId )
            }
        }

    }

    private fun addToCart(){
        val addToCart = CartItem(
                FirestoreClass().getCurrentUserID(),
                mProductOwnerId,
                mProductId,
                mProductDetails.title,
                mProductDetails.price,
                mProductDetails.image,
                Constants.DEFAULT_CART_QUANTITY
        )
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addCartItems(this@ProductDetailsActivity, addToCart)
    }

    fun addToCartSuccess() {
        hideProgressDialog()
        Toast.makeText(this, resources.getString(R.string.success_message_item_added_to_cart), Toast.LENGTH_SHORT).show()
        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        if( v!= null) {
            when (v.id) {
                R.id.btn_add_to_cart -> {
                    addToCart()
                }
                R.id.btn_go_to_cart -> {
                    startActivity(Intent(this, CartListActivity::class.java))
                }
            }
        }
    }


}