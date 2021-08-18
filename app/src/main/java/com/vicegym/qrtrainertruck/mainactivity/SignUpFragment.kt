package com.vicegym.qrtrainertruck.mainactivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.data.TrainingData
import com.vicegym.qrtrainertruck.data.myUser
import com.vicegym.qrtrainertruck.databinding.FragmentSignupBinding
import java.util.*

class SignUpFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    class TrainingDate(
        var year: Int,
        var month: Int,
        var dayOfMonth: Int,
        var hourOfDay: Int,
        var minute: Int
    ) {
        override fun toString(): String {
            return "$year/$month/$dayOfMonth $hourOfDay:$minute"
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignUpFragment()
    }

    private lateinit var binding: FragmentSignupBinding
    private lateinit var trainingDate: TrainingDate
    private lateinit var trainingData: TrainingData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnDatePicker.setOnClickListener { showDatePickerDialog() }
        //binding.btnPlacePicker.setOnClickListener { showPlacePicker() }
        binding.btnSignUpToTraining.setOnClickListener {
            uploadTrainingData()
            //activity?.supportFragmentManager?.beginTransaction()
            //  ?.apply { replace(R.id.fragmentMainMenu, HomeFragment()).commit() }
        }
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            requireContext(),
            this,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).show() //vagy requireActivity()
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(
            requireContext(),
            this,
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            true
        ).show()
    }

    //a kiválasztott dátumot tartalmazza
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        //trainingData = TrainingData(myUser.id, "$year.${month + 1}.$dayOfMonth")
        //trainingData.date = "$year.${month + 1}.$dayOfMonth"
        trainingDate = TrainingDate(year, month, dayOfMonth, 0, 0)
        showTimePickerDialog()
    }

    //a kiválasztott időt tartalmazza
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        trainingDate.hourOfDay = hourOfDay
        trainingDate.minute = minute
        trainingData = TrainingData(myUser.id)
        /*trainingData.date = GregorianCalendar(
            trainingDate.year,
            trainingDate.month,
            trainingDate.dayOfMonth,
            trainingDate.hourOfDay,
            trainingDate.minute
        )*/
        binding.tvTrainingTime.text = trainingDate.toString()
    }

    private fun uploadTrainingData() {
        if (trainingData.date == null || trainingData.location == null)
            Toast.makeText(requireContext(), "Hiányzó adatok", Toast.LENGTH_SHORT).show()
        else if (trainingDataValidate()) {
            myUser.trainingList.add(trainingData)
            val db = Firebase.firestore
            /*-- új TrainingData felvétele a Userhez tartozó dokumentumba --*/
            db.collection("users")
                .document("${myUser.id}")
                .update("trainings", FieldValue.arrayUnion(trainingData)).addOnSuccessListener {
                    Log.d("EdzesekUpdate", "OK")
                }
                .addOnFailureListener {
                    Log.d("EdzesekUpdate", "Nem OK: $it")
                }

            /*-- új TrainingData felvétele az admin apphoz egy külön collection-be --*/
            db.collection("Trainings").document("TrainingList")
                .update("trainingList", FieldValue.arrayUnion(trainingData))
        } else
            Toast.makeText(requireContext(), "Már van edzésed ezzel az időponttal!", Toast.LENGTH_SHORT).show()
    }

    private fun trainingDataValidate(): Boolean {
        var notExistInList = true
        var before3Days = true

        myUser.trainingList.forEach {
            if (it.date == trainingData.date) {
                notExistInList = false
                return@forEach
            }
        }
        return notExistInList
    }
}