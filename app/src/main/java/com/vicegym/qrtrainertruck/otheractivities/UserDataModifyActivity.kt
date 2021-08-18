package com.vicegym.qrtrainertruck.otheractivities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.authentication.LoginActivity
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityUserDataModifyBinding

class UserDataModifyActivity : BaseActivity() {
    private lateinit var binding: ActivityUserDataModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDataModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init() {
        binding.btnModifyData.setOnClickListener { validateUserData() }
        binding.btnDeleteUser.setOnClickListener { deleteUserDialog() }
        binding.btnChangePassword.setOnClickListener { changePasswordRequest() }

        /*--hint szövegek--*/
        binding.etProfName.hint = myUser.name
        binding.etProfEmail.hint = myUser.email
        binding.etProfMobile.hint = myUser.mobile

        /*--edittextek szövegváltozás figyelői--*/
        binding.etProfName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty())
                    return
                myUser.name = s.toString()
                val profileUpdates = userProfileChangeRequest {
                    displayName = myUser.name
                }

                Firebase.auth.currentUser!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FBUpdateUserName", "User profile updated.")
                        }
                    }
                Firebase.firestore.collection("users").document("${myUser.id}").update("name", myUser.name)
            }
        })
        binding.etProfEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty())
                    return
                if (s.contains('@', true) && s.contains('.', true)) {
                    myUser.email = s.toString()
                    val user = Firebase.auth.currentUser
                    user!!.updateEmail(myUser.email!!)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("FBUpdateUserEmail", "User email address updated.")
                            }
                        }
                    val db = Firebase.firestore
                    db.collection("users").document("${myUser.id}").update("email", myUser.email)
                }
            }
        })
        binding.etProfMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty())
                    return
                if (s.length == 11 || s.length == 12) {
                    myUser.mobile = s.toString()
                    val db = Firebase.firestore
                    db.collection("users").document("${myUser.id}").update("mobile", myUser.mobile)
                }
            }
        })
    }

    private fun changePasswordRequest() {
        myUser.email?.let {
            Firebase.auth.sendPasswordResetEmail(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "A jelszó módosításához elküldtük az emailt :)", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun deleteUserDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("FIÓK VÉGLEGES TÖRLÉSE").setMessage("Biztosan törlöd a fiókodat?")
            .setPositiveButton("Igen") { _, _ -> reAuthUser() }
            .setNegativeButton("Nem") { dialog, _ -> dialog.cancel() }
            .create()
        dialog.show()
    }

    private fun reAuthUser() {
        val user = Firebase.auth.currentUser!!
        val credential = EmailAuthProvider
            .getCredential(myUser.email!!, myUser.password!!)

        user.reauthenticate(credential)
            .addOnCompleteListener { deleteUser() }
    }

    private fun deleteUser() {
        /* Delete user data from firestore */
        val db = Firebase.firestore
        db.collection("users").document(myUser.id!!)
            .delete()

        /* Delete user data from Storage */

        val storageRef = Firebase.storage.reference
        val desertRef = storageRef.child("profile_pictures/${myUser.id!!}.jpg")
        desertRef.delete()

        Firebase.auth.currentUser!!.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Fiók törölve!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                }
            }
    }

    private fun validateUserData() {
        val db = Firebase.firestore
        db.collection("users").document("${myUser.id}").get().addOnSuccessListener { document ->
            if (document != null) {
                val uName = document.data?.get("name") as String?
                val uEmail = document.data?.get("email") as String?
                val uMobile = document.data?.get("mobile") as String?

                if (uName == myUser.name && uEmail == myUser.email && uMobile == myUser.mobile) {
                    Log.d("validateUserData", "OK")
                    finish()
                } else
                    Log.d("validateUserData", "Nem OK")
            }
        }
    }
}