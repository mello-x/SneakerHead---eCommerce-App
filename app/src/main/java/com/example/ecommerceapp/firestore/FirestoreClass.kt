package com.example.ecommerceapp.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.ecommerceapp.model.*
import com.example.ecommerceapp.ui.activities.*
import com.example.ecommerceapp.ui.fragments.DashboardFragment
import com.example.ecommerceapp.ui.fragments.OrdersFragment
import com.example.ecommerceapp.ui.fragments.ProductsFragment
import com.example.ecommerceapp.ui.fragments.SoldProductsFragment
import com.example.ecommerceapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {

        // collection path in firestore
        mFireStore.collection(Constants.USERS)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // here call a function to base activity for transferring the result to it
                activity.userRegistrationSuccess()
        }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.", e
                )
            }
    }

    fun getCurrentUserID(): String {
        // an instance of currentUser using firebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // a variable to assign the currentUserID if it is not null or else it will be blank.
        var currentUserID =""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getUserDetails(activity: Activity) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
            Log.i(activity.javaClass.simpleName, document.toString())

                // here we have received the document snapshot which is converted into User data model object.
                val user = document.toObject(User::class.java)!!

                // sharepreferences to save data
                val sharedPreferences = activity.getSharedPreferences(
                    Constants.MELLOSHOP_PREFERENCES, Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                // key:Value logged_in_username: Assawin Chittanandha
                // but depends on what name u login with.

                editor.putString(Constants.LOGGED_IN_USERNAME, "${user.firstName} ${user.lastName}")
                editor.apply()

                // start
                when(activity){
                    is LoginActivity -> {
                        // call a function of base activity for transferring the result of it.
                        activity.userLoggedInSuccess(user)
                    }
                    is SettingActivity -> {
                        activity.userDetailsSuccess(user)
                    }

                }
                // end

        }
            .addOnFailureListener { e ->
                //hide the progress dialog if there is any error
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SettingActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            . addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        // hide the progress dialog when there is an error. and print error in logcat.
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while updating the user details.",e)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {
        val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "." + Constants.getFileExtension(activity,imageFileURI))

        sRef.putFile(imageFileURI!!).addOnSuccessListener { taskSnapshot ->
            // the image upload is success
            Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            // get the downloadable url from the task snapshot
            taskSnapshot.metadata!!.reference!!.downloadUrl
                .addOnSuccessListener { uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    when (activity) {
                        is UserProfileActivity -> {
                            activity.imageUploadSuccess(uri.toString())
                        }
                        is AddProductActivity -> {
                            activity.imageUploadSuccess(uri.toString())
                        }
                    }
                }
        } .addOnFailureListener { exception ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            Log.e(activity.javaClass.simpleName,exception.message, exception)
        }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Products) {
        mFireStore.collection(Constants.PRODUCTS)
                .document()
                .set(productInfo, SetOptions.merge())
                .addOnSuccessListener {
                    // call a function of base activity for transferring the result to it.
                    activity.productUploadSuccess()
                }
                .addOnFailureListener{ e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while uploading the product details.",e)
                }
    }

    fun getProductsList(fragment: Fragment) {
        mFireStore.collection(Constants.PRODUCTS)
            // whereEqualTo so we called the product that is added by the user id that match with the product id.
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString())

                val productsList: ArrayList<Products> = ArrayList()

                for (i in document.documents) {
                    val product = i.toObject(Products::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                when(fragment) {
                    is ProductsFragment -> {
                        fragment.hideProgressDialog()
                    }
                }
                Log.e("Get Product List", "Error while getting product list.",e)
            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val product = document.toObject(Products::class.java)
                if (product != null) {
                    activity.productDetailsSuccess(product)
                }
            }
            .addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting the product details.",e)

            }
    }

    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {
        mFireStore.collection(Constants.CART_ITEMS)
                .document()
                .set(addToCart, SetOptions.merge())
                .addOnSuccessListener {
                    activity.addToCartSuccess()
                }
                .addOnFailureListener{ e->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating the document for cart item.",e)
                }
    }


    fun deleteProduct(fragment: ProductsFragment, productId: String) {
        mFireStore.collection(Constants.PRODUCTS)
                .document(productId)
                .delete()
                .addOnSuccessListener {
                    fragment.productDeleteSuccess()
                } .addOnFailureListener { e ->
                    fragment.hideProgressDialog()
                    Log.e(fragment.requireActivity().javaClass.simpleName,"Error while deleting the product.",e)

                }
    }

    fun getCartList(activity: Activity) {
        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val list : ArrayList<CartItem> = ArrayList()
                for (i in document.documents) {
                    val cartItem = i.toObject( CartItem::class.java)!!
                    cartItem.id = i.id

                    list.add(cartItem)
                }
                when (activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(list)
                    }
                    is CheckoutActivity -> {
                        activity.successCartItemsList(list)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e)

            }
    }

    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .update(itemHashMap)
            .addOnSuccessListener {
                when(context) {
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }


            }
            .addOnFailureListener { e->
                when(context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e (context.javaClass.simpleName, "Error while updating the cart.",e)

            }
    }

    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {
        mFireStore.collection(Constants.CART_ITEMS)
                .whereEqualTo(Constants.USER_ID,getCurrentUserID())
                .whereEqualTo(Constants.PRODUCT_ID, productId)
                .get()
                .addOnSuccessListener { document ->
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    if (document.documents.size > 0) {
                        activity.productExistsInCart()
                    } else {
                        activity.hideProgressDialog()
                    }
                }
                .addOnFailureListener { e->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating the document for cart item.",e)
                }
    }

    fun removeItemFromCart(context: Context, cart_id: String ) {
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .delete()
            .addOnSuccessListener {
                when(context) {
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener { e->
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e (context.javaClass.simpleName, "Error while removing the item from the cart list.",e)

            }
    }

    fun placeOrder(activity: CheckoutActivity, order: Order) {
        mFireStore.collection(Constants.ORDERS)
                .document()
                // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
                .set(order, SetOptions.merge())
                .addOnSuccessListener {

                    // TODO Step 9: Notify the success result.
                    // START
                    // Here call a function of base activity for transferring the result to it.
                    activity.orderPlacedSuccess()
                    // END
                }
                .addOnFailureListener { e ->

                    // Hide the progress dialog if there is any error.
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while placing an order.",
                            e
                    )
                }
    }


    fun getAllProductsList(activity: Activity) {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString())
                val productsList : ArrayList<Products> = ArrayList()
                for(i in document.documents) {
                    val product = i.toObject(Products::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }
                when(activity) {
                    is CartListActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                    is CheckoutActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                }

            }
            .addOnFailureListener{ e->
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while getting all product list.",e)

            }
    }

    fun getDashboardItemsList(fragment: DashboardFragment) {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e(fragment.javaClass.simpleName, document.documents.toString())

                val productsList: ArrayList<Products> = ArrayList()

                for (i in document.documents) {
                    val product = i.toObject(Products::class.java)!!
                    product.product_id = i.id
                    productsList.add(product)
                }

                fragment.successDashboardItemsList(productsList)
            }
            .addOnFailureListener{ e ->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list.",e)
            }
    }

    fun getAddressesList(activity: AddressListActivity) {
        mFireStore.collection(Constants.ADDRESSES)
                .whereEqualTo(Constants.USER_ID,getCurrentUserID())
                .get()
                .addOnSuccessListener {
                    document ->
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    // create new instance for address ArrayList
                    val addressList:  ArrayList<Address> = ArrayList()
                    for (i in document.documents) {
                        val address = i.toObject(Address::class.java)!!
                            address.id = i.id
                        addressList.add(address)
                    }
                    // call success address from FireStore
                    activity.successAddressListFromFirestore(addressList)
                }
                .addOnFailureListener {
                    e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while getting address list.",e)
                }
    }

    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {
        mFireStore.collection(Constants.ADDRESSES)
                .document()
                .set(addressInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.addUpdateAddressSuccess()

                }
                .addOnFailureListener { e->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while adding the address.",e)
                }
    }

    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
                .document(addressId)
                .set(addressInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.addUpdateAddressSuccess()
                }
                .addOnFailureListener {
                    e ->
                    activity.hideProgressDialog()
                    Log.e(javaClass.simpleName, "Error while updating the Address.",e)
                }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
                .document(addressId)
                .delete()
                .addOnSuccessListener {
                    activity.deleteAddressSuccess()
                }
                .addOnFailureListener {
                    e->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while deleting the address.",e)
                }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order ) {
        val writeBatch = mFireStore.batch()

        // update the cart items
        for (cartItem in cartList) {
          //  val productHashMap = HashMap<String, Any>()
            // productHashMap[Constants.STOCK_QUANTITY] = (cartItem.stock_quantity.toInt() - cartItem.cart_quantity.toInt()).toString()

            val soldProduct = SoldProduct(
                cartItem.product_owner_id, // here the user id will be of product owner.
                cartItem.title,
                cartItem.price,
                cartItem.cart_quantity,
                cartItem.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )

            // to have collection of Products from fireStore and we want to have the reference of product_id
            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS)
                    .document(cartItem.product_id)

            writeBatch.set(documentReference, soldProduct)
        }
        // delete the cart items
        for (cartItem in cartList) {
            val documentReference = mFireStore.collection(Constants.CART_ITEMS)
                    .document(cartItem.id)

            writeBatch.delete(documentReference)
        }
        writeBatch.commit()
                .addOnSuccessListener {
                    activity.allDetailsUpdatedSuccessfully()
                }
                .addOnFailureListener {
                    e->
                    activity.hideProgressDialog()

                    Log.e(activity.javaClass.simpleName, "Error while updating all the details after order placed.",e)
                }
    }

    fun getMyOrdersList(fragment: OrdersFragment) {
        mFireStore.collection(Constants.ORDERS)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                .get()
                .addOnSuccessListener { document->
                    val list: ArrayList<Order> = ArrayList()

                    for (i in document.documents) {
                        val orderItem = i.toObject(Order::class.java)!!
                        orderItem.id = i.id

                        list.add(orderItem)

                    }
                    fragment.populateOrdersListInUI(list)
                }
                .addOnFailureListener { e->
                    fragment.hideProgressDialog()
                    Log.e(fragment.javaClass.simpleName,"Error while getting order lists.",e)
                }
    }

    fun getSoldProductsList(fragment: SoldProductsFragment) {

        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(fragment.javaClass.simpleName, document.documents.toString())
                // Sold Products ArrayList.
                val list: ArrayList<SoldProduct> = ArrayList()

                for (i in document.documents) {

                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id = i.id

                    list.add(soldProduct)
                }
                fragment.successSoldProductsList(list)
            }
            .addOnFailureListener { e ->
                fragment.hideProgressDialog()
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while getting the list of sold products.",
                    e
                )
            }
    }
}




