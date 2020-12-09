package com.amrmustafa.ecommerce.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.amrmustafa.ecommerce.R
import java.text.DecimalFormat


/*
this function [callSeller] call the merchant
 */
fun View.callSeller() {
    val phone: String = context.getString(R.string.seller_number_developer)
    val intent =
        Intent(Intent.ACTION_DIAL, Uri.fromParts(context.getString(R.string.tel), phone, null))
    context.startActivity(intent)
}

/*
set the view visibility to be visible
 */
fun View.visible() {
    this.visibility = View.VISIBLE

}

/*
set the view visibility to be gone
 */
fun View.gone() {
    this.visibility = View.GONE

}



/**
 * Check whether network is available
 * @return Whether device is connected to Network.
 */
fun Context.checkInternetAccess(): Boolean {
    try {

        with(getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Device is running on Marshmallow or later Android OS.
                with(getNetworkCapabilities(activeNetwork)) {
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    return hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                activeNetworkInfo?.let {
                    // connected to the internet
                    @Suppress("DEPRECATION")
                    return listOf(
                        ConnectivityManager.TYPE_WIFI,
                        ConnectivityManager.TYPE_MOBILE
                    ).contains(it.type)
                }
            }
        }
        return false
    } catch (exc: NullPointerException) {
        return false
    }


}

fun Context.message(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.chatSeller() {
    try {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(this.getString(R.string.seller_whatsap_link))
        )
        this.startActivity(browserIntent)
    } catch (exception: PackageManager.NameNotFoundException) {
        Log.e("Utils", exception.toString())
        message("Whastsapp is not install on your phone")
    }


}



/*
hide the keyboard
 */
fun Activity.hideKeyboard() {
    val inputMethodManager: InputMethodManager? =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
}



/*
currency formatter
 */

fun inDollar(context: Context, price: String?): String? {
    var actualPrice = price?.let { formatPriceInThousand(context, it) }
    actualPrice = actualPrice?.let { truncateDecimalPrice(context, it) }
    return String.format(context.getString(R.string.dollar_sign), actualPrice)
}
fun formatPriceInThousand(context: Context, price: String): String? {
    var price = price
    price = price.replace(context.getString(R.string.comma), context.getString(R.string.empty))
    price = price.replace(":", "")
    val format = price.toDouble()
    val formatter = DecimalFormat(context.getString(R.string.pattern))
    return formatter.format(format)
}

fun truncateDecimalPrice(context: Context, actualPrice: String): String? {
    var actualPrice = actualPrice
    if (actualPrice.contains(context.getString(R.string.zero_after_decimal))) {
        actualPrice = actualPrice.replace(
            context.getString(R.string.zero_after_decimal),
            context.getString(R.string.empty)
        )
    }
    return actualPrice
}





