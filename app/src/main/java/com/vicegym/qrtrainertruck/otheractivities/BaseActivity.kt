package com.vicegym.qrtrainertruck.otheractivities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

abstract class BaseActivity : AppCompatActivity() {

    val uid = Firebase.auth.uid
    val storage = Firebase.storage.reference

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

    /*fun uploadImageToStorage(file: Uri) {
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storage.child("profile_pictures/${MyUser.id!!}.jpg").putFile(file, metadata)
        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Log.d("UploadImage", "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d("UploadImage", "Upload is paused")
        }.addOnFailureListener {
            Log.d("UploadImage", "NEM OK: $it")
        }.addOnSuccessListener {
            Log.d("UploadImage", "OK")
        }
    }*/

    /*fun updateUserProfilePictureUri(child: StorageReference) {
        child.downloadUrl.addOnSuccessListener {
            Firebase.firestore.collection("users").document(MyUser.id!!).update("onlineProfilePictureUri", it.toString())
        }
    }*/
}