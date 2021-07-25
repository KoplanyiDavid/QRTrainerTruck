package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityLoginBinding
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import java.io.File

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        if (Firebase.auth.currentUser != null)
            getUserData()
        else
            init()
    }

    private fun init() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.btnRegister.setOnClickListener { startActivity(Intent(this, RegisterFormActivity::class.java)) }
    }

    private fun signInWithEmailAndPassword() {
        if (binding.etEmail.text.toString().isEmpty() || binding.etPassword.text.toString().isEmpty()) {
            Toast.makeText(baseContext, "Hiányzó adatok!", Toast.LENGTH_SHORT).show()
        } else {
            Firebase.auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignInWithEAP", "signInWithEmail:success")
                        getUserData()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignInWithEAP", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Sikertelen autentikáció",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun getUserData() {
        Firebase.firestore.collection("users").document("${Firebase.auth.currentUser?.uid}").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    myUser.id = document.data?.get("id") as String?
                    myUser.name = document.data?.get("name") as String?
                    myUser.email = document.data?.get("email") as String?
                    myUser.mobile = document.data?.get("mobile") as String?
                    myUser.address = document.data?.get("address") as String?
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

    private fun downloadUserProfilePicture() {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("${Firebase.auth.currentUser?.uid}/profileimage.jpg")
        val localFile = File.createTempFile("profilepicture", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            Log.d("DWPIC", "OK")
            myUser.profilePicture = Uri.fromFile(localFile).toString()
            startActivity(Intent(baseContext, MainActivity::class.java))
        }
            .addOnFailureListener {
                Log.d("DWPIC", "NEM OK: $it")
            }
    }
}