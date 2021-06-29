package com.vicegym.qrtrainertruck

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
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import java.io.File

abstract class BaseActivity : AppCompatActivity() {
    protected var db: FirebaseFirestore = Firebase.firestore
    protected var auth: FirebaseAuth = Firebase.auth
    protected var user: FirebaseUser? = auth.currentUser

    protected fun uploadUserData() {
        val userHashMap = hashMapOf(
            "id" to myUser.id,
            "name" to myUser.name,
            "mobile" to myUser.mobile,
            "email" to myUser.email,
            "profpic" to myUser.profilePicture,
            "traininglocation" to myUser.address,
            "acceptedtermsandcons" to myUser.acceptedTermsAndConditions,
            "rank" to myUser.rank,
            "score" to myUser.score,
            "trainings" to myUser.trainingList
        )

        db.collection("users").document("${user?.uid}")
            .set(userHashMap)
            .addOnSuccessListener {
                uploadUserProfilePicture(myUser.profilePicture)
                Toast.makeText(
                    this,
                    "Document snapshot successfully written",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error writing document", Toast.LENGTH_SHORT).show()
            }
    }

    protected fun getUserData() {
        db.collection("users").document("${user?.uid}").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    myUser.id = document.data?.get("id") as String?
                    myUser.name = document.data?.get("name") as String?
                    myUser.email = document.data?.get("email") as String?
                    myUser.mobile = document.data?.get("mobile") as String?
                    myUser.profilePicture = document.data?.get("profpic") as String
                    myUser.acceptedTermsAndConditions = document.data?.get("acceptedtermsandcons") as Boolean
                    myUser.rank = document.data?.get("rank") as String
                    myUser.score = document.data?.get("score") as Number
                    myUser.trainingList = document.data?.get("trainings") as MutableList<TrainingData>
                    Log.d("FC", "${document.data}")
                    downloadUserProfilePicture()
                } else {
                    Log.d("FirestoreComm", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirestoreComm", "get failed with ", exception)
            }
    }

    private fun uploadUserProfilePicture(file: String) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("${user?.uid}/profileimage.jpg").putFile(Uri.parse(file), metadata)
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
    }

    private fun downloadUserProfilePicture() {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("${user?.uid}/profileimage.jpg")
        val localFile = File.createTempFile("profilepicture", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            Log.d("DWPIC", "OK")
            myUser.profilePicture = Uri.fromFile(localFile).toString()
            startMainActivity(baseContext)
        }
            .addOnFailureListener {
                Log.d("DWPIC", "NEM OK: $it")
            }
    }

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