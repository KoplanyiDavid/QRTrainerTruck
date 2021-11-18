package com.vicegym.qrtrainertruck.mainactivity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.databinding.FragmentProfileBinding
import com.vicegym.qrtrainertruck.helpers.FirebaseHelper
import com.vicegym.qrtrainertruck.otheractivities.UserDataModifyActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val user = Firebase.auth.currentUser

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
        Glide.with(requireContext()).load(FirebaseHelper.profilePictureUrl).into(binding.ivProfPic)
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
                FirebaseHelper.profilePictureUrl = it
                binding.ivProfPic.setImageURI(it)
                lifecycleScope.launch {
                    FirebaseHelper.uploadImageFromUri(it, "profile_pictures/${user!!.uid}")
                    val newPath = FirebaseHelper.getImageUrl("profile_pictures/${user.uid}")
                    FirebaseHelper.updateFieldInCollectionDocument("users", user.uid, "profilePictureUrl", newPath.toString())
                }
            }
        }

        if (requestCode == REQUEST_USER_DATA_MODIFY_ACTIVITY) {
            init()
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