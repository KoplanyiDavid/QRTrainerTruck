package com.vicegym.qrtrainertruck.authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.vicegym.qrtrainertruck.mainactivity.MainActivity

open class RegisterFormActivity : AppCompatActivity() {
    private val TAG = "UserRegistration"
    private val REQUEST_GALLERY = 1000
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
        val openGalleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(openGalleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY) {
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
                            myUser.acceptedTermsAndConditions = true
                            uploadUserData() //upload username, email etc to cloud firebase
                            Toast.makeText(baseContext, "Sikeres regisztráció:)", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(baseContext, MainActivity::class.java))
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
            "profpic" to myUser.profilePicture,
            "address" to myUser.address,
            "acceptedtermsandcons" to myUser.acceptedTermsAndConditions,
            "rank" to myUser.rank,
            "score" to myUser.score,
            "trainings" to myUser.trainingList
        )

        Firebase.firestore.collection("users").document(myUser.id!!)
            .set(userHashMap)
            .addOnSuccessListener {
                uploadUserProfilePicture(myUser.profilePicture)
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

    private fun uploadUserProfilePicture(file: String) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("${myUser.id!!}/profileimage.jpg").putFile(Uri.parse(file), metadata)
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

    private fun setupHyperlink() {
        val linkTextView = binding.cbTermsAndConditions
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}