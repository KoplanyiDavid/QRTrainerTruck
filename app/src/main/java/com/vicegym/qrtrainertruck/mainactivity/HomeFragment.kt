package com.vicegym.qrtrainertruck.mainactivity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vicegym.qrtrainertruck.databinding.FragmentHomeBinding
import com.vicegym.qrtrainertruck.myUser

class HomeFragment : Fragment() {
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
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment()
    }
}