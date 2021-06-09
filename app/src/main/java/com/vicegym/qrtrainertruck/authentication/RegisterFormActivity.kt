package com.vicegym.qrtrainertruck.authentication

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import com.vicegym.qrtrainertruck.BaseActivity
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding

class RegisterFormActivity : BaseActivity() {
    private val TAG = "UserRegistration"

    private lateinit var binding: ActivityRegisterFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { registerWithEmailAndPassword() }
        setupHyperlink()
    }

    private fun registerWithEmailAndPassword() {
        if (binding.etName.text.isEmpty() || binding.etEmail.text.isEmpty() || binding.etPassword.text.isEmpty() || binding.etConfirmPassword.text.isEmpty())
            Toast.makeText(this, "Missing requirement(s)", Toast.LENGTH_SHORT).show()
        else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString())
            Toast.makeText(this, "Password confirmation failed (did you misspelled something?)", Toast.LENGTH_SHORT).show()
        else {
            if (binding.cbTermsAndConditions.isChecked) {
                //set user data
                userAcceptedTermsAndConditions = true
                userName = binding.etName.text.toString()
                userEmail = binding.etEmail.text.toString()
                //register to database
                auth.createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            user = auth.currentUser
                            userID = user?.uid
                            uploadUserData() //upload username, email etc to cloud firebase
                            Toast.makeText(baseContext, "Registration successful", Toast.LENGTH_SHORT).show()
                            //updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            //updateUI(null)
                        }
                    }
            }
            else
                Toast.makeText(baseContext, "You have to accept Terms And Conditions!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHyperlink() {
        val linkTextView = binding.cbTermsAndConditions
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}