package com.vicegym.qrtrainertruck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import java.util.*

abstract class BaseActivity : AppCompatActivity() {
    protected var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    protected var auth: FirebaseAuth = Firebase.auth
    protected var user: FirebaseUser? = null

    //user
    object User {
        var id: String? = null
        var name: String? = null
        var email: String? = null
        var phoneNumber: String? = null
        var profilePictureUrl: Uri? = null
        var acceptedTermsAndConditions: Boolean = false
        var rank: String? = "Újonc"
        var score: Int = 0
    }

    protected fun uploadUserData() {

        val user = hashMapOf(
            "ID" to User.id,
            "Név" to User.name,
            "Telefonszám" to User.phoneNumber,
            "Email" to User.email,
            "ÁSZF-et elfogadta" to User.acceptedTermsAndConditions,
            "Rang" to User.rank,
            "Pontok" to User.score
        )

        User.id?.let {
            db.collection("Registered users").document("${User.name}.$it")
                .set(user)
                .addOnSuccessListener {
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
    }

    protected fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(baseContext, MainActivity::class.java))
        } else
            Toast.makeText(baseContext, "user is null", Toast.LENGTH_SHORT).show()
    }

    protected fun saveUserData(activity: Activity?) {
        val sharedPref = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("Name", User.name)
            putString("Email", User.email)
            putString("PhoneNumber", User.phoneNumber)
            putString("Rank", User.rank)
            putInt("Score", User.score)
            apply()
        }
    }

    protected fun getUserData(activity: Activity?) {
        val sharedPref = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE) ?: return
        User.name = sharedPref.getString("Name", "null")
        User.email = sharedPref.getString("Email", "null")
        User.phoneNumber = sharedPref.getString("PhoneNumber", "null")
        User.rank = sharedPref.getString("Rank", "null")
        User.score = sharedPref.getInt("Score", 0)
    }
}