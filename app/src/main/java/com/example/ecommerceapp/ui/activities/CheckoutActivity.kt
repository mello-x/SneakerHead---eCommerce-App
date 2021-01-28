package com.example.ecommerceapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.R
import com.example.ecommerceapp.firestore.FirestoreClass
import com.example.ecommerceapp.model.Address
import com.example.ecommerceapp.model.CartItem
import com.example.ecommerceapp.model.Order
import com.example.ecommerceapp.model.Products
import com.example.ecommerceapp.ui.adapters.CartItemsListAdapter
import com.example.ecommerceapp.utils.Constants
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : BaseActivity() {

    // a global variable for the selected address details
    private var mAddressDetails : Address? = null
    // global variable for product list
    private lateinit var mProductList: ArrayList<Products>
    // global variable for cart list
    private lateinit var mCartItemsList: ArrayList<CartItem>
    // global variable for sub total
    private var mSubTotal: Double = 0.0
    // global variable for total amount
    private var mTotalAmount: Double = 0.0
    private lateinit var mOrderDetails: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        if(intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mAddressDetails = intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)
        }
        if(mAddressDetails != null) {
            tv_checkout_address_type.text = mAddressDetails?.type
            tv_checkout_full_name.text = mAddressDetails?.name
            tv_checkout_address.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            tv_checkout_additional_note.text = mAddressDetails?.additionalNote

            if(mAddressDetails?.otherDetails!!.isNotEmpty()) {
                tv_checkout_other_details.text = mAddressDetails?.otherDetails
            }
            tv_checkout_mobile_number.text = mAddressDetails?.mobileNumber
        }

        getProductList()

        btn_place_order.setOnClickListener {
            placeAnOrder()
        }
    }

    fun successProductsListFromFireStore(productList: ArrayList<Products>) {
        mProductList = productList
        getCartItemsList()
    }

    fun orderPlacedSuccess() {
        FirestoreClass().updateAllDetails(this , mCartItemsList, mOrderDetails)
    }

    fun allDetailsUpdatedSuccessfully() {
        hideProgressDialog()

        Toast.makeText(this@CheckoutActivity, "Your order was placed successfully.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this@CheckoutActivity, dashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun placeAnOrder() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mAddressDetails !=null ){
            mOrderDetails = Order(
                    FirestoreClass().getCurrentUserID(),
                    mCartItemsList,
                    mAddressDetails!!,
                    "My order ${System.currentTimeMillis()}",
                    mCartItemsList[0].image,
                    mSubTotal.toString(),
                    "15.0", // fixed shipping Charge as $15 for now.
                    mTotalAmount.toString(),
                    System.currentTimeMillis()
            )
            FirestoreClass().placeOrder(this, mOrderDetails)
        }

    }

    private fun getCartItemsList() {
        FirestoreClass().getCartList(this)
    }

    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()
        for (product in mProductList) {
            for (cartItm in cartList) {
                if(product.product_id == cartItm.product_id) {
                    cartItm.stock_quantity = product.stock_quantity
                }
            }
        }
        mCartItemsList = cartList

        rv_cart_list_items.layoutManager = LinearLayoutManager(this)
        rv_cart_list_items.setHasFixedSize(true)
        val cartListAdapter = CartItemsListAdapter(this, mCartItemsList,false)
        rv_cart_list_items.adapter = cartListAdapter

        for (item in mCartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }
        tv_checkout_sub_total.text = "$$mSubTotal"
        tv_checkout_shipping_charge.text = "$10.0"

        if (mSubTotal > 0) {
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + 10.0
            tv_checkout_total_amount.text = "$$mTotalAmount"
        } else {
            ll_checkout_place_order.visibility = View.GONE
        }

    }

    private fun getProductList() {
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }



}