package com.vicegym.qrtrainertruck.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vicegym.qrtrainertruck.data.Post
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.CardPostBinding

class PostsAdapter(private val context: Context) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(ItemCallback) {

    private val postList: MutableList<Post> = mutableListOf()
    private var lastPosition = -1

    class PostViewHolder(binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivProfilePicture = binding.ivPostCardProfilePicture
        val tvAuthor: TextView = binding.tvAuthor
        val tvTime: TextView = binding.tvDailyChallengeTime
        val tvDescription: TextView = binding.tvDailyChallengeDescription
        val imgPost: ImageView = binding.imgPost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostViewHolder(CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val tmpPost = postList[position]
        holder.ivProfilePicture.setImageURI(myUser.profilePicture.toUri())
        holder.tvAuthor.text = tmpPost.author
        holder.tvTime.text = tmpPost.time
        holder.tvDescription.text = tmpPost.description

        Glide.with(context).load(tmpPost.imageUrl).into(holder.imgPost)

        setAnimation(holder.itemView, position)
    }

    fun addPost(post: Post?) {
        post ?: return

        postList += (post)
        submitList((postList))
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    companion object {
        object ItemCallback : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }
}