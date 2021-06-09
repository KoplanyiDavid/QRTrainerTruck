package com.vicegym.qrtrainertruck

import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

abstract class BaseActivity : AppCompatActivity() {
    protected var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    protected var auth: FirebaseAuth = Firebase.auth
    protected var user: FirebaseUser? = null

    //userdata
    protected var userName: String? = null
    protected var userEmail: String? = null
    protected var userPhoneNumber: String? = null
    protected var userProfilePictureUrl: Uri? = null
    protected var userAcceptedTermsAndConditions: Boolean = false

    private fun uploadUserData() {

        val user = hashMapOf(
            "Name" to userName,
            "Phone number" to userPhoneNumber,
            "Email" to userEmail,
            "AcceptedTermsAndConditions" to userAcceptedTermsAndConditions
        )

        userName?.let {
            db.collection("Registered users").document(it)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Document snapshot successfully written", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error writing document", Toast.LENGTH_SHORT).show()
                }
        }
    }
}