package com.vicegym.qrtrainertruck.otheractivities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.common.math.IntMath.pow
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.authentication.LoginActivity
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.databinding.ActivityCreatePostBinding
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreatePostActivity : BaseActivity() {

    companion object {
        private const val REQUEST_WRITE = 101
        private const val REQUEST_CAMERA = 100
    }

    //private var isPictureTaken = false
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var photoPath: String
    private val user = Firebase.auth.currentUser
    private lateinit var dialog: AlertDialog

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

        binding = ActivityCreatePostBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.ivDailyChallengePicture.setOnClickListener { checkCameraPermission() }
        binding.btnDailyChallengeSendPost.setOnClickListener { sendClick() }
    }

    private fun checkCameraPermission() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                checkWritePermission()
            }
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    private fun checkWritePermission() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> {
                createPhoto()
            }
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkCameraPermission()
                } else {
                    val dialog = AlertDialog.Builder(this).setTitle("FIGYELEM")
                        .setMessage("Engedély nélkül nem tudom megnyitni a kamerát :(")
                        .setPositiveButton("Engedély megadása") { _, _ ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
                        }
                        .setNegativeButton("Engedély elutasítása") { dialog, _ -> dialog.cancel() }
                        .create()
                    dialog.show()
                }
                return
            }
            REQUEST_WRITE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkWritePermission()
                } else {
                    val dialog = AlertDialog.Builder(this).setTitle("FIGYELEM")
                        .setMessage("Engedély nélkül nem tudom elmenteni a fotót a háttértárra :(")
                        .setPositiveButton("Engedély megadása") { _, _ ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE)
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

    private fun createPhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("Create file for Photo", "Error")
                    buildAlertDialog("HIBA", "Nem sikerült a fotót elkészíteni :(")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.vicegym.qrtrainertruck.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA)
                }
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (storageDir != null) {
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
                .apply { photoPath = absolutePath }
        } else {
            Log.e("PhotoSave", "Photo not saved")
            buildAlertDialog(dialogMessage = "A fotót nem sikerült elmenteni :( (hiányzik valami engedély?)")
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CAMERA) {
            setPic()
            //isPictureTaken = true
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = binding.ivDailyChallengePicture.width
        val targetH: Int = binding.ivDailyChallengePicture.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(photoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = 1.coerceAtLeast((photoW / targetW).coerceAtMost(photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        BitmapFactory.decodeFile(photoPath, bmOptions)?.also { bitmap ->
            binding.ivDailyChallengePicture.rotation = 90f
            binding.ivDailyChallengePicture.setImageBitmap(bitmap)
        }
    }

    private fun sendClick() {
        try {
            binding.btnDailyChallengeSendPost.isClickable = false //ne töltse fel többször ugyanazt
            loadingAlertDialog()
            uploadPost()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadingAlertDialog() {
        dialog = AlertDialog.Builder(this)
            .setTitle("Poszt feltöltése...")
            .setMessage("A posztod feltöltése folyamatban van, kérlek légy türelemmel, amint befejeződik, én eltűnök...")
            .create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun uploadPost() {
        var sorter = Date().time
        var temp = sorter
        var i = 0
        while (temp != 0L) {
            temp /= 10
            i += 1
        }
        if (i > 10) {
            val a = i - 10
            sorter /= pow(10, a)
        } else if (i < 10) {
            val a = 10 - i
            sorter *= pow(10, a)
        }
        lifecycleScope.launch {
            FirebaseHelper.uploadImageFromImageView(binding.ivDailyChallengePicture, "postimages/${user!!.uid}_$sorter.jpg")
            val imageUrl = FirebaseHelper.getImageUrl("postimages/${user.uid}_$sorter.jpg")
            val newPost = hashMapOf<String, Any>(
                "authorId" to user.uid,
                "authorName" to MyUser.name!!,
                "title" to binding.etPostTitle.text.toString(),
                "description" to binding.etPostDescription.text.toString(),
                "imageUrl" to imageUrl.toString(),
                "sorter" to sorter
            )
            FirebaseHelper.setCollectionDocument("posts", sorter.toString(), newPost)

            dialog.dismiss()
            finish()
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