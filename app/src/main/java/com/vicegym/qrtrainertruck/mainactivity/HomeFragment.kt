package com.vicegym.qrtrainertruck.mainactivity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.databinding.FragmentHomeBinding
import com.vicegym.qrtrainertruck.myUser

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUserName.text = myUser.name
        binding.tvUserRank.text = myUser.rank
        binding.tvUserScore.text = myUser.score.toString()
        binding.ivProfilePicture.setImageURI((Uri.parse(myUser.profilePicture)))
        if (myUser.trainingList.isNotEmpty()) {
            binding.tvTrainingTime.text = myUser.trainingList[0].date
            binding.tvTrainingPlace.text = myUser.trainingList[0].location
        }

        //Map fragment init
        val mapFragment = parentFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment()
    }

    override fun onMapReady(p0: GoogleMap) {
        p0.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
        )
    }
}