package com.vicegym.qrtrainertruck.mainactivity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mapView: MapView
    private lateinit var gMap: GoogleMap
    private var marker: Marker? = null

    companion object {
        private const val REQUEST_GALLERY = 101

        @JvmStatic
        fun newInstance() =
            HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        mapInit(savedInstanceState)
    }

    private fun init() {
        binding.tvUserName.text = myUser.name
        binding.tvUserRank.text = myUser.rank
        binding.tvUserScore.text = myUser.score.toString()
        binding.ivProfilePicture.setImageURI(myUser.profilePicture.toUri())
        binding.ivProfilePicture.setOnClickListener { changeProfilePicture() }

        if (myUser.trainingList.isNotEmpty()) {
            binding.tvTrainingTime.text = myUser.trainingList[0].date.toString()
            binding.tvTrainingPlace.text = myUser.trainingList[0].location
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
                myUser.profilePicture = it.toString()
                binding.ivProfilePicture.setImageURI(it)
            }
        }
    }

    private fun uploadImageToStorage(file: Uri) {
        val storageRef = Firebase.storage.reference
        val metadata = storageMetadata { contentType = "profile_image/jpeg" }
        val uploadTask = storageRef.child("profile_pictures/${myUser.id!!}.jpg").putFile(file, metadata)
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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /* Google MapView */
    private fun mapInit(savedInstanceState: Bundle?) {
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        try {
            MapsInitializer.initialize(requireActivity())
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        locationUpdate()
    }

    private fun locationUpdate() {
        /*--DB URL megadása kötelező, különben US centralt keres!--*/
        val database =
            FirebaseDatabase.getInstance("https://qrtrainertruck-default-rtdb.europe-west1.firebasedatabase.app").reference.child("TrainerTruckLocation")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                marker?.remove()
                updateMap(dataSnapshot)
                Log.d("lU", "OK")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("LU", "Failed to read value.")
            }
        })
    }

    private fun updateMap(dataSnapshot: DataSnapshot) {
        val lat: Double = dataSnapshot.child("latitude").value as Double
        val lng: Double = dataSnapshot.child("longitude").value as Double

        val truckLatLng = LatLng(lat, lng)
        val markerIcon = BitmapFactory.decodeResource(context?.resources, R.mipmap.truckvektor)

        marker = gMap.addMarker(
            MarkerOptions().position(truckLatLng).title("Trainer Truck")
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(markerIcon, 80, 60, false)))
        )
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(truckLatLng, 16f))
        Toast.makeText(requireContext(), truckLatLng.toString(), Toast.LENGTH_SHORT).show()
    }
}