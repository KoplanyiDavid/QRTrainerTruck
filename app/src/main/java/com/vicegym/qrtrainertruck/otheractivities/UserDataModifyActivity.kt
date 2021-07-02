package com.vicegym.qrtrainertruck.otheractivities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityUserDataModifyBinding

class UserDataModifyActivity : AppCompatActivity() {
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

        /*--hint szövegek--*/
        binding.etProfName.hint = myUser.name
        binding.etProfEmail.hint = myUser.email
        binding.etProfMobile.hint = myUser.mobile
        binding.etProfAddress.hint = myUser.address

        /*--edittextek szövegváltozás figyelői--*/
        binding.etProfName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                myUser.name = s.toString()
                val db = Firebase.firestore
                db.collection("users").document("${myUser.id}").update("name", myUser.name)
            }
        })
        binding.etProfEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                myUser.email = s.toString()
                val db = Firebase.firestore
                db.collection("users").document("${myUser.id}").update("email", myUser.email)
            }
        })
        binding.etProfMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                myUser.mobile = s.toString()
                val db = Firebase.firestore
                db.collection("users").document("${myUser.id}").update("mobile", myUser.mobile)
            }
        })
        binding.etProfAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                myUser.address = s.toString()
                val db = Firebase.firestore
                db.collection("users").document("${myUser.id}").update("address", myUser.address)
            }
        })
    }

    private fun validateUserData() {
        val db = Firebase.firestore
        db.collection("users").document("${myUser.id}").get().addOnSuccessListener { document ->
            if (document != null) {
                val uName = document.data?.get("name") as String?
                val uEmail = document.data?.get("email") as String?
                val uMobile = document.data?.get("mobile") as String?
                val uAddress = document.data?.get("address") as String?

                if (uName == myUser.name && uEmail == myUser.email && uMobile == myUser.mobile && uAddress == myUser.address) {
                    Log.d("validateUserData", "OK")
                    finish()
                } else
                    Log.d("validateUserData", "Nem OK")
            }
        }
    }
}