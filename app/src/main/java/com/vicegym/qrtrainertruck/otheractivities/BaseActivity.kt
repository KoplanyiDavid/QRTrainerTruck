package com.vicegym.qrtrainertruck.otheractivities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {


    protected fun haveInternedConnection(context: Context?): Boolean {
        return if (context != null) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            activeNetwork?.isConnected == true
        } else {
            Log.e("haveInternetConnection", "contect argument is null")
            false
        }
    }
}