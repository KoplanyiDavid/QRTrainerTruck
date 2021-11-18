package com.vicegym.qrtrainertruck.otheractivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import kotlinx.coroutines.launch

class LoadingScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            FirebaseHelper.loadMyUser(Firebase.auth.currentUser!!.uid)
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}