package com.example.ecommerceapp.utils


import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.ecommerceapp.R
import java.io.IOException

class GlideLoader(val context: Context){


    fun loadUserPicture(image: Any, imageView: ImageView) {
        try{
            // load the user image in the imageView.
            Glide.with(context)
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.ic_person_foreground)
                .into(imageView)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadProductPicture(image: Any, imageView: ImageView) {
        try{
            // load the user image in the imageView.
            Glide
                .with(context)
                .load(image)
                .centerCrop()
                .into(imageView)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}