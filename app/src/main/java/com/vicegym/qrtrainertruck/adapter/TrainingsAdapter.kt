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
import android.widget.Toast
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
        getUserData(holder.card, tmpTraining)
        holder.card.setOnClickListener {
            manageTraining(tmpTraining.id!!, holder.card)
        }
        setAnimation(holder.itemView, position)
    }

    private fun getUserData(card: CardView, tmpTraining: TrainingData) {
        val tmpList: ArrayList<TrainingData> = arrayListOf()
        val db = Firebase.firestore.collection("users").document("${Firebase.auth.currentUser?.uid}")
        db.get()
            .addOnSuccessListener { document ->
                if (document.exists() && document != null) {
                    val trainings = document.data?.get("trainings") as ArrayList<HashMap<String, String>>
                    for (training in trainings) {
                        val trainingData = TrainingData()
                        trainingData.id = training["id"]
                        trainingData.title = training["title"]
                        trainingData.date = training["date"]
                        trainingData.location = training["location"]
                        trainingData.trainer = training["trainer"]
                        tmpList.add(trainingData)
                    }
                    MyUser.trainingList = tmpList
                    if (MyUser.trainingList.contains(tmpTraining))
                        card.setCardBackgroundColor(GREEN)
                } else {
                    Toast.makeText(context, "document not exists", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirestoreComm", "get failed with ", exception)
            }
    }

    private fun manageTraining(id: String, card: CardView) {
        val db = Firebase.firestore.collection("trainings").document(id)
        db.get().addOnSuccessListener { document ->
            val trainingList = document.data?.get("trainees") as ArrayList<*>?
            if (trainingList != null) {
                if (!(trainingList.contains(MyUser.id))) {
                    db.update("trainees", FieldValue.arrayUnion(MyUser.id))
                    val trainingData = TrainingData()
                    db.get().addOnSuccessListener { document1 ->
                        trainingData.id = document1.data?.get("id") as String?
                        trainingData.title = document1.data?.get("title") as String?
                        trainingData.date = document1.data?.get("date") as String?
                        trainingData.location = document1.data?.get("location") as String?
                        trainingData.trainer = document1.data?.get("trainer") as String?
                        uploadUserTrainingData(trainingData)
                        card.setCardBackgroundColor(GREEN)
                    }
                } else if (trainingList.contains(MyUser.id)) {
                    db.update("trainees", FieldValue.arrayRemove(MyUser.id))
                    val trainingData = TrainingData()
                    db.get().addOnSuccessListener { document1 ->
                        trainingData.id = document1.data?.get("id") as String?
                        trainingData.title = document1.data?.get("title") as String?
                        trainingData.date = document1.data?.get("date") as String?
                        trainingData.location = document1.data?.get("location") as String?
                        trainingData.trainer = document1.data?.get("trainer") as String?
                        deleteUserTrainingData(trainingData)
                        card.setCardBackgroundColor(DKGRAY)
                    }
                }
            }
        }
    }

    private fun uploadUserTrainingData(trainingData: TrainingData) {
        val db = Firebase.firestore.collection("users").document(MyUser.id!!)
        db.update("trainings", FieldValue.arrayUnion(trainingData))
        MyUser.trainingList.add(trainingData)
    }

    private fun deleteUserTrainingData(trainingData: TrainingData) {
        val db = Firebase.firestore.collection("users").document(MyUser.id!!)
        db.update("trainings", FieldValue.arrayRemove(trainingData))
        MyUser.trainingList.remove(trainingData)
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