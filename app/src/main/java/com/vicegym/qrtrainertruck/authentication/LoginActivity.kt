package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.vicegym.qrtrainertruck.BaseActivity
import com.vicegym.qrtrainertruck.databinding.ActivityLoginBinding
import com.vicegym.qrtrainertruck.myUser

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val googleSignInClient = GoogleSignInClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.btnRegister.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterFormActivity::class.java
                )
            )
        }
        binding.btnGoogleSignIn.setOnClickListener { googleSignInClient.signInWithGoogle(this) }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        user = auth.currentUser
        user?.let { getUserData() } //ha user nem null, akk lefut
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("GoogleActivity", "firebaseAuthWithGoogle:" + account.id)
                myUser.name = account.displayName
                myUser.email = account.email
                myUser.acceptedTermsAndConditions = true
                //userProfilePictureUrl = account.photoUrl
                account.idToken?.let { googleSignInClient.firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("GoogleActivity", "Google sign in failed", e)
            }
        }
    }

    private fun signInWithEmailAndPassword() {
        if (binding.etEmail.text.toString().isEmpty() || binding.etPassword.text.toString().isEmpty()) {
            Toast.makeText(baseContext, "Hiányzó adatok!", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignInWithEAP", "signInWithEmail:success")
                        user = auth.currentUser
                        myUser.id = user?.uid
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

    /*private fun signInWithGoogle() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }*/


    /*private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    user = auth.currentUser
                    myUser.id = user?.uid
                    uploadUserData()
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }*/
}