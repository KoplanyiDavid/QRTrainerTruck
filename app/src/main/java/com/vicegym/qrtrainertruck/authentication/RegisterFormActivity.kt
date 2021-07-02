package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding
import com.vicegym.qrtrainertruck.data.myUser

class RegisterFormActivity : BaseActivity() {
    private val TAG = "UserRegistration"
    private lateinit var binding: ActivityRegisterFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivRegisterProfPic.setOnClickListener { setProfilePicture() }
        binding.btnRegister.setOnClickListener { registerWithEmailAndPassword() }
        setupHyperlink()
    }

    private fun setProfilePicture() {
        val openGalleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(openGalleryIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            data?.data?.let {
                myUser.profilePicture = it.toString()
                binding.ivRegisterProfPic.setImageURI(it)
            }
        }
    }

    private fun registerWithEmailAndPassword() {
        if (binding.etName.text.toString().isEmpty()
            || binding.etEmail.text.toString().isEmpty()
            || binding.etPassword.text.toString().isEmpty()
            || binding.etConfirmPassword.text.toString().isEmpty()
        )
            Toast.makeText(this, "Hiányzó adatok!", Toast.LENGTH_SHORT).show()
        else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString())
            Toast.makeText(
                this,
                "A jelszó megerősítése sikertelen.(Elírtál valamit?)",
                Toast.LENGTH_SHORT
            ).show()
        else {
            if (binding.cbTermsAndConditions.isChecked) {
                //register to database
                auth.createUserWithEmailAndPassword(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            user = auth.currentUser
                            /* -- Set myUser object data --*/
                            myUser.id = user?.uid
                            myUser.name = binding.etName.text.toString()
                            myUser.email = binding.etEmail.text.toString()
                            myUser.mobile = binding.etMobileNum.text.toString()
                            myUser.acceptedTermsAndConditions = true
                            uploadUserData() //upload username, email etc to cloud firebase
                            Toast.makeText(baseContext, "Sikeres regisztráció:)", Toast.LENGTH_SHORT).show()
                            startMainActivity(baseContext)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else
                Toast.makeText(
                    baseContext,
                    "Nem fogadtad el a felhasználási feltételeket.",
                    Toast.LENGTH_SHORT
                )
                    .show()
        }
    }

    private fun setupHyperlink() {
        val linkTextView = binding.cbTermsAndConditions
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}