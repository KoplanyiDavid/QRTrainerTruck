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
import com.google.firebase.firestore.FirebaseFirestore
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.TrainingData
import com.vicegym.qrtrainertruck.databinding.FragmentSignupBinding
import com.vicegym.qrtrainertruck.myUser
import java.util.*

class SignUpFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    AdapterView.OnItemSelectedListener {

    companion object {
        @JvmStatic
        fun newInstance() =
            SignUpFragment()
    }

    private lateinit var binding: FragmentSignupBinding
    private var trainingData: TrainingData = TrainingData()

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
        binding.btnPlacePicker.setOnClickListener { showPlacePicker() }
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
        trainingData.date = "$year.${month + 1}.$dayOfMonth"
        showTimePickerDialog()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        trainingData.date += "($hourOfDay:$minute)"
        binding.tvTrainingTime.text = trainingData.date
    }

    private fun showPlacePicker() {
        val spinner: Spinner = binding.trainingplacesSpinner
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.training_places,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    private fun uploadTrainingData() {
        if (trainingData.date == null || trainingData.location == null)
            Toast.makeText(requireContext(), "Hiányzó adatok", Toast.LENGTH_SHORT).show()
        else {
            myUser.trainingList.add(trainingData)
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document("${myUser.id}")
                .update("trainings", myUser.trainingList).addOnSuccessListener {
                    Log.d("EdzesekUpdate", "OK")
                }
                .addOnFailureListener {
                    Log.d("EdzesekUpdate", "Nem OK: $it")
                }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            binding.tvTrainingPlace.text = parent.getItemAtPosition(position).toString()
            Log.d("PLACE", "OK")
        }
        else
            Log.d("PLACE", "NEM OK")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "nincs valasztott hely", Toast.LENGTH_SHORT).show()
    }
}