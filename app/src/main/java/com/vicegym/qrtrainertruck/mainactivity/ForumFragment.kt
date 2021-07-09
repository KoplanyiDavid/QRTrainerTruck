package com.vicegym.qrtrainertruck.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.adapter.PostsAdapter
import com.vicegym.qrtrainertruck.data.Post
import com.vicegym.qrtrainertruck.databinding.FragmentForumBinding

class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding
    private lateinit var postsAdapter: PostsAdapter

    companion object {
        fun newInstance() =
            ForumFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postsAdapter = PostsAdapter(requireContext())
        binding.contentPosts.rvPosts.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        binding.contentPosts.rvPosts.adapter = postsAdapter

        initPostsListener()
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(dc.document.toObject<Post>())
                        DocumentChange.Type.MODIFIED -> Toast.makeText(
                            requireContext(),
                            dc.document.data.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        DocumentChange.Type.REMOVED -> Toast.makeText(
                            requireContext(),
                            dc.document.data.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}