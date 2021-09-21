package com.vicegym.qrtrainertruck.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color.DKGRAY
import android.graphics.Color.GREEN
import android.util.Log
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
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.databinding.CardTrainingBinding

class TrainingsAdapter(private val context: Context) :
    ListAdapter<TrainingData, TrainingsAdapter.TrainingsViewHolder>(ItemCallback) {

    private val trainingsList: MutableList<TrainingData> = mutableListOf()
    private var lastPosition = -1

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
        setGreenCards(holder.card, tmpTraining)
        holder.card.setOnClickListener {
            manageTraining(tmpTraining.id!!, holder.card)
        }
        setAnimation(holder.itemView, position)
    }

    private fun setGreenCards(card: CardView, tmpTraining: TrainingData) {
        val db = Firebase.firestore.collection("users")
        db.document("${Firebase.auth.currentUser?.uid}").get().addOnSuccessListener { document ->
            if (document.exists() && document != null) {
                val trainings = document.data?.get("trainings") as ArrayList<HashMap<String, Any>>
                for (training in trainings) {
                    if (tmpTraining.id == training["id"])
                        card.setCardBackgroundColor(GREEN)
                }
            }
        }
            .addOnFailureListener { exception ->
                Log.d("FirestoreComm", "get failed with ", exception)
            }
    }

    private fun manageTraining(id: String, card: CardView) {
        val db = Firebase.firestore.collection("trainings").document(id)
        db.get().addOnSuccessListener { document ->
            val trainingList = document.data?.get("trainees") as ArrayList<String>
            if (!(trainingList.contains(MyUser.id))) {
                db.update("trainees", FieldValue.arrayUnion(MyUser.id))
                val trainingData = TrainingData()
                trainingData.id = document.data?.get("id") as String
                trainingData.title = document.data?.get("title") as String
                trainingData.date = document.data?.get("date") as String
                trainingData.location = document.data?.get("location") as String
                trainingData.trainer = document.data?.get("trainer") as String
                trainingData.sorter = document.data?.get("sorter") as Long
                uploadUserTrainingData(trainingData)
                card.setCardBackgroundColor(GREEN)
            } else if (trainingList.contains(MyUser.id)) {
                db.update("trainees", FieldValue.arrayRemove(MyUser.id))
                val trainingData = TrainingData()
                trainingData.id = document.data?.get("id") as String
                trainingData.title = document.data?.get("title") as String
                trainingData.date = document.data?.get("date") as String
                trainingData.location = document.data?.get("location") as String
                trainingData.trainer = document.data?.get("trainer") as String
                trainingData.sorter = document.data?.get("sorter") as Long
                deleteUserTrainingData(trainingData)
                card.setCardBackgroundColor(DKGRAY)
            }
        }
    }

    private fun uploadUserTrainingData(trainingData: TrainingData) {
        val db = Firebase.firestore.collection("users").document(MyUser.id!!)
        db.update("trainings", FieldValue.arrayUnion(trainingData))
    }

    private fun deleteUserTrainingData(trainingData: TrainingData) {
        val db = Firebase.firestore.collection("users").document(MyUser.id!!)
        db.update("trainings", FieldValue.arrayRemove(trainingData))
    }

    fun addTrainings(training: TrainingData?) {
        training ?: return

        trainingsList += (training)
        submitList((trainingsList))
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