package com.vicegym.qrtrainertruck.otheractivities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import java.io.File

abstract class BaseActivity : AppCompatActivity() {
    protected var auth: FirebaseAuth = Firebase.auth
    protected var db: FirebaseFirestore = Firebase.firestore

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

    protected fun startMainActivity(context: Context) {
        startActivity(Intent(context, MainActivity::class.java))
    }
}