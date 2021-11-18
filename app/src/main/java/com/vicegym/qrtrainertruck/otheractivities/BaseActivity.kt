package com.vicegym.qrtrainertruck.otheractivities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected fun initBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.package.ACTION_LOGOUT")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("onReceive", "Logout in progress")
                //At this point you should start the login activity and finish this one
                finish()
            }
        }, intentFilter)
    }

    protected fun buildAlertDialog(dialogTitle: String? = null, dialogMessage: String? = null) {
        if (dialogTitle == null && dialogMessage == null)
            return
        val dialog = AlertDialog.Builder(this)
        if (dialogTitle != null)
            dialog.setTitle(dialogTitle)
        if (dialogMessage != null)
            dialog.setMessage(dialogMessage)
        dialog.create().show()
    }
}