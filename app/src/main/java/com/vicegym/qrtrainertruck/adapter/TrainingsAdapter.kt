package com.vicegym.qrtrainertruck.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color.GREEN
import android.graphics.Color.LTGRAY
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.databinding.CardTrainingBinding

class TrainingsAdapter(private val context: Context) :
    ListAdapter<TrainingData, TrainingsAdapter.TrainingsViewHolder>(ItemCallback) {

    private val trainingsList: MutableList<TrainingData> = mutableListOf()
    private var lastPosition = -1
    private val user = Firebase.auth.currentUser

    class TrainingsViewHolder(binding: CardTrainingBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvTrainingType = binding.tvTrainingType
        val tvTrainer: TextView = binding.tvTrainer
        val tvGymPlace: TextView = binding.tvGymPlace
        val tvDate: TextView = binding.tvGymDate
        val card = binding.cardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TrainingsViewHolder(CardTrainingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TrainingsViewHolder, position: Int) {
        val tmpTraining = trainingsList[position]
        holder.tvTrainingType.text = tmpTraining.title
        holder.tvTrainer.text = tmpTraining.trainer
        holder.tvGymPlace.text = tmpTraining.location
        holder.tvDate.text = tmpTraining.date
        setCardsColor(holder.card, tmpTraining.trainees!!)
        holder.card.setOnClickListener {
            manageTraining(tmpTraining, holder.card)
        }
        setAnimation(holder.itemView, position)
    }

    private fun setCardsColor(card: CardView, trainees: ArrayList<String>) {
        if (trainees.contains(user!!.uid))
            card.setCardBackgroundColor(GREEN)
        else
            card.setCardBackgroundColor(LTGRAY)
    }

    private fun manageTraining(training: TrainingData, card: CardView) {
        /* Ha a user nem szerepel a trainees között */
        //user felvétele az edzés trainees-hez//
        if (!(training.trainees!!.contains(user!!.uid))) {
            val db = Firebase.firestore
            db.collection("trainings").document(training.sorter.toString())
                .update("trainees", FieldValue.arrayUnion(user.uid))

            //edzes felvétele a userhez
            val data = hashMapOf(
                "date" to training.date,
                "location" to training.location,
                "sorter" to training.sorter
            )
            db.collection("users").document(user.uid).update("trainings", FieldValue.arrayUnion(data))

            training.trainees!!.add(user.uid)
            submitList(trainingsList)
            card.setCardBackgroundColor(GREEN)
        }
        else {
            val db = Firebase.firestore
            db.collection("trainings").document(training.sorter.toString())
                .update("trainees", FieldValue.arrayRemove(user.uid))

            //edzes torlese a userbol
            val data = hashMapOf(
                "date" to training.date,
                "location" to training.location,
                "sorter" to training.sorter
            )
            db.collection("users").document(user.uid).update("trainings", FieldValue.arrayRemove(data))

            training.trainees!!.remove(user.uid)
            submitList(trainingsList)
            card.setCardBackgroundColor(LTGRAY)
        }
    }

    fun addTrainings(training: TrainingData?) {
        training ?: return

        trainingsList += (training)
        submitList((trainingsList))
    }

    fun removeTrainings(training: TrainingData?) {
        training ?: return

        trainingsList -= training
        submitList(trainingsList)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    companion object {
        object ItemCallback : DiffUtil.ItemCallback<TrainingData>() {
            override fun areItemsTheSame(oldItem: TrainingData, newItem: TrainingData): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: TrainingData, newItem: TrainingData): Boolean {
                return oldItem == newItem
            }
        }
    }
}