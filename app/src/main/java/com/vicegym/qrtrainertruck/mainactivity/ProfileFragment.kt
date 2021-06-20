package com.vicegym.qrtrainertruck.mainactivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.databinding.FragmentProfileBinding
import com.vicegym.qrtrainertruck.myUser

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private var galleryReqCode = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvProfName.text = myUser.name
        binding.tvProfEmail.text = myUser.email
        binding.tvProfFragmentName.text = myUser.name
        binding.tvProfFragmentEmail.text = myUser.email
        binding.tvProfFragmentMobile.text = myUser.phoneNumber
        binding.tvProfFragmentAddress.text = myUser.address
        binding.ivProfPic.setImageURI(Uri.parse(myUser.profilePicture))
        binding.ivProfPic.setOnClickListener {
            changeProfilePicture()
        }
    }

    private fun changeProfilePicture() {
        val openGalleryIntent =
            Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(openGalleryIntent, galleryReqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == galleryReqCode) {
            data?.data?.let {
                uploadImageToStorage(it)
                myUser.profilePicture = it.toString()
                binding.ivProfPic.setImageURI(it)
            }
        }
    }

    private fun uploadImageToStorage(file: Uri) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("${myUser.id}/profileimage.jpg").putFile(file, metadata)
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

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment()
    }
}