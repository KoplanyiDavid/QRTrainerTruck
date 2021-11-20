package com.vicegym.qrtrainertruck.authentication

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity
import kotlinx.coroutines.launch
import java.util.*

open class RegisterFormActivity : BaseActivity() {

    companion object {
        private const val TAG = "UserRegistration"
        private const val REQUEST_GALLERY = 1000
    }

    private var user: FirebaseUser? = null
    private lateinit var binding: ActivityRegisterFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.ivRegisterProfPic.setOnClickListener { setProfilePicture() }
        binding.btnRegister.setOnClickListener { registerWithEmailAndPassword() }
        setupHyperlink()
    }

    private fun setProfilePicture() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> {
                val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(openGalleryIntent, REQUEST_GALLERY)
            }
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_GALLERY)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY) {
            data?.data?.let {
                binding.ivRegisterProfPic.setImageURI(it)
            }
        } else {
            buildAlertDialog(dialogMessage = "Nem férek hozzá a galériához :(")
        }
    }

    private fun sendVerificationEmail() {
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("A regisztráció megerősítésére vonatkozó email-t elküldtük a megadott email címre, megerősítés után tudsz belépni :)")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            startActivity(Intent(baseContext, LoginActivity::class.java))
                            finish()
                        }
                    dialog.create().show()
                }
            }
    }

    private fun registerWithEmailAndPassword() {
        binding.btnRegister.isClickable = false

        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
            buildAlertDialog(dialogMessage = "Hiányzó adatok!")
        else if (!email.contains('@', true))
            buildAlertDialog(dialogMessage = "A megadott email-cím formátuma nem jó!")
        else if (password != confirmPassword)
            buildAlertDialog(dialogMessage = "A jelszó megerősítése sikertelen.(Elírtál valamit?)")
        else {
            if (binding.cbTermsAndConditions.isChecked) {
                val registerDialog = AlertDialog.Builder(this)
                    .setTitle("Regisztráció folyamatban...")
                    .create()

                //register to database
                registerDialog.show()
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        /* -- Set MyUser object data --*/
                        user = Firebase.auth.currentUser
                        val trainings = ArrayList<TrainingData>()
                        lifecycleScope.launch {
                            FirebaseHelper.uploadImageFromImageView(binding.ivRegisterProfPic, "profile_pictures/${user!!.uid}")
                            val userData = hashMapOf<String, Any>(
                                "id" to user!!.uid,
                                "name" to binding.etName.text.toString(),
                                "email" to email,
                                "mobile" to "",
                                "acceptedtermsandcons" to true,
                                "rank" to "Újonc",
                                "score" to 0,
                                "trainings" to trainings
                            )
                            FirebaseHelper.setCollectionDocument("users", user!!.uid, userData)
                            registerDialog.dismiss()
                            sendVerificationEmail()
                        }

                    } else {
                        registerDialog.dismiss()
                        buildAlertDialog("HIBA", "Hibakód:\n" + task.exception.toString())
                    }
                }
            } else
                buildAlertDialog(dialogMessage = "Nem fogadtad el a felhasználási feltételeket.")
        }

        binding.btnRegister.isClickable = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GALLERY -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setProfilePicture()
                } else {
                    val dialog = AlertDialog.Builder(this).setTitle("FIGYELEM").setMessage("Engedély nélkül nem férek hozzá a fotódhoz")
                        .setPositiveButton("Engedély megadása") { _, _ ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_GALLERY)
                        }
                        .setNegativeButton("Engedély elutasítása") { dialog, _ -> dialog.cancel() }
                        .create()
                    dialog.show()
                }
                return
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun setupHyperlink() {
        val linkTextView = binding.cbTermsAndConditions
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}