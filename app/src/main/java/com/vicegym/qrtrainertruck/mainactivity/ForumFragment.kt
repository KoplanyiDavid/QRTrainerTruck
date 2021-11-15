package com.vicegym.qrtrainertruck.mainactivity

import android.content.Intent
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
import com.vicegym.qrtrainertruck.adapter.PostsAdapter
import com.vicegym.qrtrainertruck.databinding.FragmentForumBinding
import com.vicegym.qrtrainertruck.otheractivities.CreatePostActivity

class ForumFragment : Fragment() {
    private lateinit var binding: FragmentForumBinding
    private lateinit var postsAdapter: PostsAdapter
    private val createPostReqCode = 1011

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
        init()
    }

    private fun init() {
        postsAdapter = PostsAdapter(requireContext())
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        binding.rvPosts.adapter = postsAdapter
        initPostsListener()
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("posts").orderBy("posts")
        db.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> {}
                        DocumentChange.Type.REMOVED -> postsAdapter.removePost(dc.document.toObject())
                    }
                }
            }
        binding.ivNewPost.setOnClickListener {
            val createPostIntent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivityForResult(createPostIntent, createPostReqCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createPostReqCode)
            init()
    }
}