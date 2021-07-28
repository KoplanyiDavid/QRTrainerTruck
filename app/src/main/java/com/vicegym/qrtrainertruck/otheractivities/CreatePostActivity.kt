package com.vicegym.qrtrainertruck.otheractivities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.data.Post
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityCreatePostBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


class CreatePostActivity : BaseActivity() {

    companion object {
        private const val CAMERA_REQ_CODE = 101
    }

    private var isPictureTaken = false
    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.ivDailyChallengePicture.setOnClickListener { createPhoto() }
        binding.btnDailyChallengeSendPost.setOnClickListener { sendClick() }
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
                    Toast.makeText(this, "Error while creating the file for photo", Toast.LENGTH_SHORT).show()
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
                    startActivityForResult(takePictureIntent, CAMERA_REQ_CODE)
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
                .apply { myUser.profilePicture = absolutePath }
        } else {
            Log.e("PhotoSave", "Photo not saved")
            Toast.makeText(this, "Photo not saved", Toast.LENGTH_SHORT).show()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == CAMERA_REQ_CODE) {
            setPic()
            isPictureTaken = true
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = binding.ivDailyChallengePicture.width
        val targetH: Int = binding.ivDailyChallengePicture.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(myUser.profilePicture, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = 1.coerceAtLeast((photoW / targetW).coerceAtMost(photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(myUser.profilePicture, bmOptions)?.also { bitmap ->
            binding.ivDailyChallengePicture.rotation = 90f
            binding.ivDailyChallengePicture.setImageBitmap(bitmap)
        }
    }

    private fun sendClick() {
        when {
            !isPictureTaken -> Toast.makeText(baseContext, "Nem készítettél képet!", Toast.LENGTH_SHORT).show()
            binding.etDailyChallengeTime.text.isNullOrEmpty() -> Toast.makeText(baseContext, "Nem írtad be a futott idődet!", Toast.LENGTH_SHORT)
                .show()
            else -> {
                try {
                    uploadPostWithImage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun uploadPostWithImage() {
        val bitmap: Bitmap = (binding.ivDailyChallengePicture.drawable as BitmapDrawable).bitmap
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())
        val imageInBytes = ByteArrayOutputStream().toByteArray()

        val storageReference = Firebase.storage.reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                uploadPost(downloadUri.toString())
            }
    }

    private fun uploadPost(imageUrl: String? = null) {
        val newPost = Post(
            myUser.id,
            myUser.profilePicture,
            myUser.name,
            binding.etDailyChallengeTime.text.toString(),
            binding.etDailyChallengeDescription.text.toString(),
            imageUrl
        )

        val db = Firebase.firestore

        db.collection("posts").document("${Date().time}")
            .set(newPost)
            .addOnSuccessListener {
                Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e -> Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show() }
    }


}