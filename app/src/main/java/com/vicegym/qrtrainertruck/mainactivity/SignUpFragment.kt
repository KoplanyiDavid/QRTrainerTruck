package com.vicegym.qrtrainertruck.mainactivity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.databinding.FragmentSignupBinding
import java.util.*

class SignUpFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: FragmentSignupBinding

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
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            requireContext(),
            this,
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH
        ).show() //vagy requireActivity()
    }

    //a kiválasztott dátumot tartalmazza
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SignUpFragment()
    }
}