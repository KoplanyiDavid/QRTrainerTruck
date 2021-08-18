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
        "Mobilization" to "https://youtube.com/playlist?list=PLyfiU-McsWgsy-90ZaEwVkx1Mk0bM7cxZ",
        "BodyWeight" to "https://youtube.com/playlist?list=PLyfiU-McsWgv27WjcGS7RS3hezsnQjITZ",
        "Equipment" to "https://youtube.com/playlist?list=PLyfiU-McsWguH9D584n9rynvMMnjSSI3X",
        "GoodMorning" to "https://youtube.com/playlist?list=PLyfiU-McsWgsibGwfffPg4QePw6wQBXZS",
        "GoodNight" to "https://youtube.com/playlist?list=PLyfiU-McsWgsQIQmNp2l0zJ6kX5IhUak2"
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
        binding.ivMorning.setOnClickListener {
            ytplaylistList["GoodMorning"]?.let { it1 -> startYtActivity(it1) }
        }
        binding.ivMobilize.setOnClickListener {
            ytplaylistList["Mobilization"]?.let { it2 -> startYtActivity(it2) }
        }
        binding.ivBodyWeight.setOnClickListener {
            ytplaylistList["BodyWeight"]?.let { it3 -> startYtActivity(it3) }
        }
        binding.ivEquipment.setOnClickListener {
            ytplaylistList["Equipment"]?.let { it4 -> startYtActivity(it4) }
        }
        binding.ivNight.setOnClickListener {
            ytplaylistList["GoodNight"]?.let { it5 -> startYtActivity(it5) }
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