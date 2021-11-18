package com.vicegym.qrtrainertruck.otheractivities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.authentication.LoginActivity
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.databinding.ActivityUserDataModifyBinding
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper

class UserDataModifyActivity : BaseActivity() {
    private lateinit var binding: ActivityUserDataModifyBinding
    private val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.package.ACTION_LOGOUT")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("onReceive", "Logout in progress")
                //At this point you should start the login activity and finish this one
                finish()
            }
        }, intentFilter)

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
        binding.etProfName.hint = MyUser.name
        binding.etProfEmail.hint = MyUser.email
        binding.etProfMobile.hint = MyUser.mobile

        /*--edittextek szövegváltozás figyelői--*/
        binding.etProfName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty())
                    return
                MyUser.name = s.toString()

                FirebaseHelper.updateFieldInCollectionDocument("users", user!!.uid, "name", MyUser.name)
            }
        })
        binding.etProfEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty())
                    return
                if (s.contains('@', true) && s.contains('.', true)) {
                    MyUser.email = s.toString()
                    val user = Firebase.auth.currentUser
                    user!!.updateEmail(MyUser.email!!)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("FBUpdateUserEmail", "User email address updated.")
                            }
                        }
                    FirebaseHelper.updateFieldInCollectionDocument("users", user.uid, "email", MyUser.email)
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
                    MyUser.mobile = s.toString()
                    FirebaseHelper.updateFieldInCollectionDocument("users", user!!.uid, "mobile", MyUser.mobile)
                }
            }
        })
    }

    private fun changePasswordRequest() {
        MyUser.email?.let {
            Firebase.auth.sendPasswordResetEmail(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        buildAlertDialog(dialogMessage = "A jelszó módosításához elküldtük az emailt :)")
                }
        }
    }

    private fun deleteUserDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("FIÓK VÉGLEGES TÖRLÉSE").setMessage("Biztosan törlöd a fiókodat?")
            .setPositiveButton("Igen") { dialog, _ ->
                dialog.dismiss()
                deleteUser()
            }
            .setNegativeButton("Nem") { dialog, _ -> dialog.cancel() }
            .create()
        dialog.show()
    }

    private fun deleteUser() {
        /* Reauth the user */
        val passwordDailog = AlertDialog.Builder(this)
        passwordDailog.setTitle("Add meg jelszavad a fiók törléséhez!")

        val input = EditText(this)
        input.hint = "jelszó"
        input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        passwordDailog.setView(input)

        passwordDailog.setPositiveButton("Törlés") { dialog, which ->
            if (input.text.isNullOrEmpty() || user == null)
                Toast.makeText(applicationContext, "Nem adtál meg jelszót!", Toast.LENGTH_LONG).show()
            else {
                /* Delete user data from firestore */
                FirebaseHelper.deleteDocumentInCollection("users", user.uid)

                /* Delete user data from Storage */
                val storageRef = Firebase.storage.reference.child("profile_pictures/${user.uid}")
                storageRef.delete()

                /* Delete user from trainings */

                val db = Firebase.firestore
                val trainings: MutableList<TrainingData?> = mutableListOf()
                db.collection("trainings").get().addOnSuccessListener {
                    for (doc in it.documents) {
                        doc ?: return@addOnSuccessListener
                        trainings += doc.toObject()
                    }

                    for (training in trainings) {
                        if (training!!.trainees.isNullOrEmpty())
                            return@addOnSuccessListener
                        else
                            if (training.trainees!!.contains(user.uid))
                                db.collection("trainings").document(training.sorter.toString())
                                    .update("trainees", FieldValue.arrayRemove(user.uid))
                    }
                }

                /* Delete user from Firebase */
                val credential = EmailAuthProvider.getCredential(user.email!!, input.text.toString())
                user.reauthenticate(credential).addOnCompleteListener {
                    Firebase.auth.currentUser!!.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Fiók törölve!", Toast.LENGTH_LONG).show()
                                val broadcastIntent = Intent()
                                broadcastIntent.action = "com.package.ACTION_LOGOUT"
                                sendBroadcast(broadcastIntent)
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                            }
                        }
                }
            }
        }
        passwordDailog.show()
    }

    private fun validateUserData() {
        val db = Firebase.firestore
        db.collection("users").document(user!!.uid).get().addOnSuccessListener { document ->
            if (document != null) {
                val uName = document.data?.get("name") as String?
                val uEmail = document.data?.get("email") as String?
                val uMobile = document.data?.get("mobile") as String?

                if (uName == MyUser.name && uEmail == MyUser.email && uMobile == MyUser.mobile) {
                    Log.d("validateUserData", "OK")
                    finish()
                } else {
                    buildAlertDialog("HIBA!", "Az adatok módosítása sikertelen, kérlek próbáld meg újra és ellenőrizd az internet elérésedet!")
                    Log.d("validateUserData", "Nem OK")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                Firebase.auth.signOut()
                startActivity(Intent(baseContext, LoginActivity::class.java))
                val broadcastIntent = Intent()
                broadcastIntent.action = "com.package.ACTION_LOGOUT"
                sendBroadcast(broadcastIntent)
                return true
            }
            R.id.menu_TandC -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://docs.google.com/document/d/1jmm1wLmqKIgFZMPiUWM2nMmfHlh4yq1HrLc_-bT-EAo/edit?usp=sharing")
                )
                startActivity(intent)
                return true
            }
            R.id.menu_help -> {
                //TODO
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}