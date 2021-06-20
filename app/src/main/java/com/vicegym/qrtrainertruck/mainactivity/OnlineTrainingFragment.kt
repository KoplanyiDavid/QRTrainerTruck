package com.vicegym.qrtrainertruck.mainactivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vicegym.qrtrainertruck.databinding.FragmentOnlinetrainingBinding

class OnlineTrainingFragment : Fragment() {
    private lateinit var binding: FragmentOnlinetrainingBinding
    private val ytplaylistList = hashMapOf(
        "Mobilizacio" to "https://www.youtube.com/playlist?list=PLyfiU-McsWgsQIQmNp2l0zJ6kX5IhUak2",
        "Erosites" to "https://www.youtube.com/playlist?list=PLyfiU-McsWguH9D584n9rynvMMnjSSI3X",
        "Rehab" to "https://www.youtube.com/playlist?list=PLyfiU-McsWgsibGwfffPg4QePw6wQBXZS"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlinetrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ytIv1.setOnClickListener {
            ytplaylistList["Mobilizacio"]?.let { it1 -> startYtActivity(it1) }
        }
        binding.ytIv2.setOnClickListener {
            ytplaylistList["Erosites"]?.let { it2 -> startYtActivity(it2) }
        }
    }

    private fun startYtActivity(category: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(category))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.google.android.youtube")
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            OnlineTrainingFragment()
    }
}