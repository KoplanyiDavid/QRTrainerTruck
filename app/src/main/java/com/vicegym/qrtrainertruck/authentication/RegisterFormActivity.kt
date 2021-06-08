package com.vicegym.qrtrainertruck.authentication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.vicegym.qrtrainertruck.BaseActivity
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding

class RegisterFormActivity : BaseActivity() {
    private val TAG = "GoogleActivity"

    private lateinit var binding: ActivityRegisterFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { registerWithEmailAndPassword() }
    }

    private fun registerWithEmailAndPassword() {
        if (binding.etName.text.isEmpty() || binding.etEmail.text.isEmpty() || binding.etPassword.text.isEmpty() || binding.etConfirmPassword.text.isEmpty())
            Toast.makeText(this, "Missing requirement(s)", Toast.LENGTH_SHORT).show()
        else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString())
            Toast.makeText(this, "Password confirmation failed (did you misspelled something?)", Toast.LENGTH_SHORT).show()
        else {
            auth.createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        user = auth.currentUser
                        //updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        //updateUI(null)
                    }
                }
        }
    }
}