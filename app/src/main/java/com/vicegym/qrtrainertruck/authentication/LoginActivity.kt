package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.databinding.ActivityLoginBinding
import com.vicegym.qrtrainertruck.databinding.PopupWindowForgotPasswordBinding
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper
import com.vicegym.qrtrainertruck.mainactivity.MainActivity
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initFirebase()
        initBroadcastReceiver()
    }

    private fun initFirebase() {
        auth = Firebase.auth
        user = auth.currentUser
        user?.reload() // <-- enélkül ha fb-ből törlődik a user, az app crashel, mert cacheből még betölti a usert
        if (user != null && user!!.isEmailVerified) {
            lifecycleScope.launch {
                FirebaseHelper.loadMyUser(user!!.uid)
                startActivity(Intent(baseContext, MainActivity::class.java))
                finish()
            }
        } else
            init()
    }

    private fun init() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterFormActivity::class.java))
        }
        binding.btnForgotPassword.setOnClickListener { popupWindow() }
    }

    /*private fun getUserData() {
        startActivity(Intent(this, LoadingScreenActivity::class.java))
        finish()
        val db = Firebase.firestore.collection("users").document("${Firebase.auth.currentUser?.uid}")
        db.get().addOnSuccessListener { document ->
            if (document.exists() && document != null) {
                MyUser.id = document.data?.get("id") as String?
                MyUser.name = document.data?.get("name") as String?
                MyUser.email = document.data?.get("email") as String?
                MyUser.password = document.data?.get("password") as String?
                MyUser.mobile = document.data?.get("mobile") as String?
                MyUser.acceptedTermsAndConditions = document.data?.get("acceptedtermsandcons") as Boolean
                MyUser.rank = document.data?.get("rank") as String
                MyUser.score = document.data?.get("score") as Number
                MyUser.onlineProfilePictureUri = document.data?.get("onlineProfilePictureUri") as String?
                startActivity(Intent(baseContext, MainActivity::class.java))
            } else {
                Log.d("FirestoreComm", "No such document")
                startActivity(Intent(baseContext, LoginActivity::class.java))
            }
        }
            .addOnFailureListener { exception ->
                Log.d("FirestoreComm", "get failed with ", exception)
            }
    }*/

/*    private fun findNextTraining(trainingList: ArrayList<HashMap<String, Any>>): TrainingData {
        var nextTrainingHashMap = trainingList[0]
        for (training in trainingList) {
            if ((training["sorter"] as Long) < (nextTrainingHashMap["sorter"] as Long)) {
                nextTrainingHashMap = training
            }
        }
        val nextTraining = TrainingData()
        nextTraining.date = nextTrainingHashMap["date"] as String
        nextTraining.location = nextTrainingHashMap["location"] as String
        return nextTraining
    }*/

    /*private fun downloadUserProfilePicture() {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_pictures/${MyUser.id!!}.jpg")
        val localFile = File.createTempFile("profilepicture", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            Log.d("DWPIC", "OK")
            MyUser.profilePicture = Uri.fromFile(localFile).toString()
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                Log.d("DWPIC", "NEM OK: $it")
            }
    }*/


    private fun popupWindow() {
        val popupBinding: PopupWindowForgotPasswordBinding = PopupWindowForgotPasswordBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(popupBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val etEmail = popupBinding.popupWindowEmail
        popupWindow.elevation = 5.0f

        popupBinding.btnForgotPasswordGo.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                passwordResetEmail(email)
                popupWindow.dismiss()
            }
        }

        popupBinding.btnForgotPasswordCancel.setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(popupBinding.root, Gravity.CENTER, 0, 0)
        popupWindow.isFocusable = true
        popupWindow.update()
    }

    private fun passwordResetEmail(email: String?) {
        if (!email.isNullOrEmpty() && email.isNotBlank() && email.contains('@', true))
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        buildAlertDialog("Új jelszó kérelem sikeres", "Elküldtük az emailt a jelszó módosításához :)")
                    else
                        buildAlertDialog("Új jelszó kérelem HIBA", "A megadott email-cím nem szerepel az adatbázisban :(")
                }
        else
            buildAlertDialog("HIBA", "Az email mező üres, vagy rossz a formátum!")
    }

    private fun signInWithEmailAndPassword() {
        val emailText = binding.etEmail.text.toString()
        val passwordText = binding.etPassword.text.toString()
        if (emailText.isEmpty() || passwordText.isEmpty()) {
            buildAlertDialog(dialogMessage = "Hiányzó adatok!")
        } else if (emailText.contains('@', true)) {
            Firebase.auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = Firebase.auth.currentUser
                        user!!.reload()
                        if (user!!.isEmailVerified) {
                            lifecycleScope.launch {
                                FirebaseHelper.loadMyUser(user!!.uid)
                                startActivity(Intent(baseContext, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            buildAlertDialog("FIGYELEM!", "Nem erősítetted meg a regisztrációt az emailben kapott link segítségével!")
                        }
                    } else {
                        buildAlertDialog("FIGYELEM!", "A felhasználónév, vagy a jelszó helytelen!")
                    }
                }
        } else
            buildAlertDialog(dialogMessage = "Rossz formátum! (Elírtad az email-címed?)")
    }
}