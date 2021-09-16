package com.vicegym.qrtrainertruck.mainactivity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.databinding.FragmentProfileBinding
import com.vicegym.qrtrainertruck.otheractivities.UserDataModifyActivity

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.tvProfName.text = MyUser.name
        binding.tvProfEmail.text = MyUser.email
        binding.tvProfFragmentName.text = MyUser.name
        binding.tvProfFragmentEmail.text = MyUser.email
        binding.tvProfFragmentMobile.text = MyUser.mobile
        binding.ivProfPic.setImageURI(Uri.parse(MyUser.profilePicture))
        binding.ivProfPic.setOnClickListener {
            changeProfilePicture()
        }
        binding.btnModifyData.setOnClickListener {
            startActivityForResult(Intent(requireContext(), UserDataModifyActivity::class.java), REQUEST_USER_DATA_MODIFY_ACTIVITY)
        }
    }

    private fun changeProfilePicture() {
        when (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> {
                val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(openGalleryIntent, REQUEST_GALLERY)
            }
            else -> ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_GALLERY)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GALLERY -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    changeProfilePicture()
                } else {
                    val dialog = AlertDialog.Builder(requireContext()).setTitle("FIGYELEM").setMessage("Engedély nélkül nem férek hozzá a fotódhoz")
                        .setPositiveButton("Engedély megadása") { _, _ ->
                            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_GALLERY)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY) {
            data?.data?.let {
                uploadImageToStorage(it)
                MyUser.profilePicture = it.toString()
                binding.ivProfPic.setImageURI(it)
            }
        }

        if (requestCode == REQUEST_USER_DATA_MODIFY_ACTIVITY) {
            init()
        }
    }

    private fun uploadImageToStorage(file: Uri) {
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

    companion object {
        private const val REQUEST_GALLERY = 1000
        private const val REQUEST_USER_DATA_MODIFY_ACTIVITY = 1001

        @JvmStatic
        fun newInstance() =
            ProfileFragment()
    }
}