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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.databinding.ActivityRegisterFormBinding
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity

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
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            data?.data?.let {
                MyUser.profilePicture = it.toString()
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
                    buildAlertDialog(dialogMessage = "A regisztráció megerősítésére vonatkozó email-t elküldtük a megadott email címre, megerősítés után tudsz belépni :)")
                }
            }
    }

    private fun registerWithEmailAndPassword() {
        if (binding.etName.text.toString().isEmpty()
            || binding.etEmail.text.toString().isEmpty()
            || binding.etPassword.text.toString().isEmpty()
            || binding.etConfirmPassword.text.toString().isEmpty()
        )
            buildAlertDialog(dialogMessage = "Hiányzó adatok!")
        else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString())
            buildAlertDialog(dialogMessage = "A jelszó megerősítése sikertelen.(Elírtál valamit?)")
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
                            /* -- Set MyUser object data --*/
                            user = Firebase.auth.currentUser
                            MyUser.id = user!!.uid
                            MyUser.name = binding.etName.text.toString()
                            MyUser.email = binding.etEmail.text.toString()
                            MyUser.password = binding.etPassword.text.toString()
                            MyUser.acceptedTermsAndConditions = true
                            uploadUserData() //upload username, email etc to cloud firebase
                            sendVerificationEmail()
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            buildAlertDialog("ERROR", task.exception.toString())
                        }
                    }
            } else
                buildAlertDialog(dialogMessage = "Nem fogadtad el a felhasználási feltételeket.")
        }
    }

    private fun uploadUserData() {
        val trainings: ArrayList<HashMap<String, Any>> = arrayListOf()
        val userHashMap = hashMapOf(
            "id" to MyUser.id,
            "name" to MyUser.name,
            "mobile" to MyUser.mobile,
            "email" to MyUser.email,
            "password" to MyUser.password,
            "profpic" to MyUser.profilePicture,
            "acceptedtermsandcons" to MyUser.acceptedTermsAndConditions,
            "rank" to MyUser.rank,
            "score" to MyUser.score,
            "nextTraining" to MyUser.nextTraining,
            "trainings" to trainings
        )

        Firebase.firestore.collection("users").document(MyUser.id!!)
            .set(userHashMap)
            .addOnSuccessListener {
                uploadUserProfilePicture(MyUser.profilePicture.toUri())
            }
            .addOnFailureListener {
                buildAlertDialog("FIGYELEM", "Hiba történt az adatok feltöltése közben.Hiba: $it")
            }
    }

    private fun uploadUserProfilePicture(file: Uri) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("profile_pictures/${MyUser.id!!}.jpg").putFile(file, metadata)
        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Log.d("UploadImage", "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d("UploadImage", "Upload is paused")
        }.addOnFailureListener {
            Log.d("UploadImage", "NEM OK: $it")
        }.addOnSuccessListener {
            getUri(storageRef.child("profile_pictures/${MyUser.id!!}.jpg"))
            Log.d("UploadImage", "OK")
        }
    }

    private fun getUri(child: StorageReference) {
        child.downloadUrl.addOnSuccessListener {
            Firebase.firestore.collection("users").document(MyUser.id!!).update("onlineProfilePictureUri", it.toString())
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