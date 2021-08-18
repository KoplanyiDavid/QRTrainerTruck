package com.vicegym.qrtrainertruck.authentication

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding

open class RegisterFormActivity : AppCompatActivity() {

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
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            data?.data?.let {
                myUser.profilePicture = it.toString()
                binding.ivRegisterProfPic.setImageURI(it)
            }
        } else
            Toast.makeText(this, "baj", Toast.LENGTH_SHORT).show()
    }

    private fun sendVerificationEmail() {
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    Toast.makeText(
                        this,
                        "A regisztráció megerősítésére vonatkozó email-t elküldtük a megadott email címre, megerősítés után tudsz belépni :)",
                        Toast.LENGTH_SHORT
                    ).show()
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
                Firebase.auth.createUserWithEmailAndPassword(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            /* -- Set myUser object data --*/
                            user = Firebase.auth.currentUser
                            myUser.id = user!!.uid
                            myUser.name = binding.etName.text.toString()
                            myUser.email = binding.etEmail.text.toString()
                            myUser.password = binding.etPassword.text.toString()
                            myUser.acceptedTermsAndConditions = true
                            uploadUserData() //upload username, email etc to cloud firebase
                            sendVerificationEmail()
                            finish()
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

    private fun uploadUserData() {
        val userHashMap = hashMapOf(
            "id" to myUser.id,
            "name" to myUser.name,
            "mobile" to myUser.mobile,
            "email" to myUser.email,
            "password" to myUser.password,
            "profpic" to myUser.profilePicture,
            "acceptedtermsandcons" to myUser.acceptedTermsAndConditions,
            "rank" to myUser.rank,
            "score" to myUser.score,
            "trainings" to myUser.trainingList
        )

        Firebase.firestore.collection("users").document(myUser.id!!)
            .set(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Document snapshot successfully written",
                    Toast.LENGTH_SHORT
                ).show()
                uploadUserProfilePicture(myUser.profilePicture.toUri())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error writing document", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadUserProfilePicture(file: Uri) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("profile_pictures/${myUser.id!!}.jpg").putFile(file, metadata)
        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Log.d("UploadImage", "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d("UploadImage", "Upload is paused")
        }.addOnFailureListener {
            Log.d("UploadImage", "NEM OK: $it")
        }.addOnSuccessListener {
            Log.d("UploadImage", "OK")
        }
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