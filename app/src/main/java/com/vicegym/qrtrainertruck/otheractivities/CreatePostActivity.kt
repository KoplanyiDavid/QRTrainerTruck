package com.vicegym.qrtrainertruck.otheractivities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
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
        private const val REQUEST_CODE = 101
    }

    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.btnSend.setOnClickListener { sendClick() }
        binding.btnAttach.setOnClickListener { attachClick() }
    }

    private fun sendClick() {
        /*if (binding.etTitle.text.isNullOrEmpty() || binding.etBody.text.isNullOrEmpty()) {
            return
        }*/
        if (binding.ivAttach.visibility == View.VISIBLE) {
            try {
                uploadPostWithImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            Toast.makeText(this, "Nem készítettél képet!", Toast.LENGTH_SHORT).show()
    }

    private fun uploadPostWithImage() {
        val bitmap: Bitmap = (binding.ivAttach.drawable as BitmapDrawable).bitmap
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
            myUser.name,
            binding.etDescription.text.toString(),
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

    private fun attachClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            binding.ivAttach.setImageBitmap(imageBitmap)
            binding.ivAttach.visibility = View.VISIBLE
        }
    }
}