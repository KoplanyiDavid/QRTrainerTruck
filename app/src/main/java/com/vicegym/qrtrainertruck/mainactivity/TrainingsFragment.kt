package com.vicegym.qrtrainertruck.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.adapter.TrainingsAdapter
import com.vicegym.qrtrainertruck.databinding.FragmentTrainingsBinding

class TrainingsFragment : Fragment() {
    private lateinit var binding: FragmentTrainingsBinding
    private lateinit var trainingsAdapter: TrainingsAdapter

    companion object {
        fun newInstance() =
            TrainingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        trainingsAdapter = TrainingsAdapter(requireContext())
        binding.rvTrainings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrainings.adapter = trainingsAdapter
        initTrainingsListener()
    }

    private fun initTrainingsListener() {
        val db = Firebase.firestore
        db.collection("trainings")
            .orderBy("sorter")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> trainingsAdapter.addTrainings(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> {}
                        DocumentChange.Type.REMOVED -> trainingsAdapter.removeTrainings(dc.document.toObject())
                    }
                }
            }

        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> trainingsAdapter.addUser(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> {}
                        DocumentChange.Type.REMOVED -> trainingsAdapter.removeUser(dc.document.toObject())
                    }
                }
            }
    }
}