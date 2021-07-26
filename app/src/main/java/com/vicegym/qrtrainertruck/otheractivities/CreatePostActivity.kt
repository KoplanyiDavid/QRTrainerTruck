package com.vicegym.qrtrainertruck.otheractivities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.data.Post
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.ActivityCreatePostBinding
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
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
        binding.ivDailyChallengePicture.setOnClickListener { attachClick() }
        binding.btnDailyChallengeSendPost.setOnClickListener { sendClick() }
    }

    private fun attachClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, CAMERA_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == CAMERA_REQ_CODE) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            binding.ivDailyChallengePicture.setImageBitmap(imageBitmap)
            isPictureTaken = true
        }
    }

    private fun sendClick() {
        if (isPictureTaken && binding.etDailyChallengeTime.text.isNotBlank() && binding.etDailyChallengeTime.text.isNotEmpty())
            try {
                uploadPostWithImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        else
            Toast.makeText(baseContext, "Nem csináltál képet, vagy nem írtad be a futott idődet!", Toast.LENGTH_SHORT).show()
    }

    private fun uploadPostWithImage() {
        val bitmap: Bitmap = (binding.ivDailyChallengePicture.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageInBytes = baos.toByteArray()

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