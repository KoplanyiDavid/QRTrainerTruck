package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.databinding.ActivityLoginBinding
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import com.vicegym.qrtrainertruck.otheractivities.LoadingScreenActivity
import java.io.File

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        user = auth.currentUser
        // Check if user is signed in (non-null)
        if (user != null && user!!.isEmailVerified)
            getUserData()
        else
            init()
    }

    private fun init() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.btnRegister.setOnClickListener { startActivity(Intent(this, RegisterFormActivity::class.java)) }
        binding.btnForgotPassword.setOnClickListener { popupWindow() }
    }

    private fun popupWindow() {
        val popupView = layoutInflater.inflate(R.layout.popup_window_forgot_password, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val etEmail = findViewById<EditText>(R.id.etForgotPasswordEmail)

        popupWindow.elevation = 5.0f

        popupView.findViewById<Button>(R.id.btnForgotPasswordGo).setOnClickListener {
            if (etEmail != null)
                if (etEmail.text.toString().isNotEmpty())
                    passwordResetEmail(etEmail.text.toString())
        }

        popupView.findViewById<Button>(R.id.btnForgotPasswordCancel).setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        popupWindow.isFocusable = true
        popupWindow.update()
    }

    private fun passwordResetEmail(email: String?) {
        if (email != null && email.isNotEmpty())
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "A jelszó módosításához elküldtük az emailt :)", Toast.LENGTH_SHORT).show()
                    }
                }
        else
            Toast.makeText(applicationContext, "Az email mező üres!", Toast.LENGTH_SHORT).show()
    }

    private fun signInWithEmailAndPassword() {
        if (binding.etEmail.text.toString().isEmpty() || binding.etPassword.text.toString().isEmpty()) {
            Toast.makeText(baseContext, "Hiányzó adatok!", Toast.LENGTH_SHORT).show()
        } else {
            Firebase.auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = Firebase.auth.currentUser
                        user!!.reload()
                        if (user!!.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignInWithEAP", "signInWithEmail:success")
                            getUserData()
                        } else {
                            Toast.makeText(this, "Nem erősítetted meg a regisztrációt az emailben kapott link segítségével!", Toast.LENGTH_LONG)
                                .show()
                        }
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
        startActivity(Intent(this, LoadingScreenActivity::class.java))
        Firebase.firestore.collection("users").document("${Firebase.auth.currentUser?.uid}").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    MyUser.id = document.data?.get("id") as String?
                    MyUser.name = document.data?.get("name") as String?
                    MyUser.email = document.data?.get("email") as String?
                    MyUser.password = document.data?.get("password") as String?
                    MyUser.mobile = document.data?.get("mobile") as String?
                    MyUser.acceptedTermsAndConditions = document.data?.get("acceptedtermsandcons") as Boolean
                    MyUser.rank = document.data?.get("rank") as String
                    MyUser.score = document.data?.get("score") as Number
                    val trainings = document.data?.get("trainings") as ArrayList<HashMap<String, String>>
                    for (training in trainings) {
                        val trainingData = TrainingData()
                        trainingData.id = training["id"]
                        trainingData.title = training["title"]
                        trainingData.date = training["date"]
                        trainingData.location = training["location"]
                        trainingData.trainer = training["trainer"]
                        MyUser.trainingList.add(trainingData)
                    }
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
        val imageRef = storageRef.child("profile_pictures/${MyUser.id!!}.jpg")
        val localFile = File.createTempFile("profilepicture", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            Log.d("DWPIC", "OK")
            MyUser.profilePicture = Uri.fromFile(localFile).toString()
            startActivity(Intent(baseContext, MainActivity::class.java))
        }
            .addOnFailureListener {
                Log.d("DWPIC", "NEM OK: $it")
            }
    }
}