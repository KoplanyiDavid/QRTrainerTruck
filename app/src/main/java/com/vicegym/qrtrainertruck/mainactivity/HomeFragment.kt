package com.vicegym.qrtrainertruck.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vicegym.qrtrainertruck.BaseActivity
import com.vicegym.qrtrainertruck.databinding.FragmentHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//keys
private const val ARG_USERNAME = "userName"
private const val ARG_USERRANK = "userRank"
private const val ARG_USERSCORE = "userScore"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var userName: String? = null
    private var userRank: String? = null
    private var userScore: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userName = it.getString(ARG_USERNAME)
            userRank = it.getString(ARG_USERRANK)
            userScore = it.getInt(ARG_USERSCORE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvUserName.text = userName
        binding.tvUserRank.text = userRank
        binding.tvUserScore.text = userScore.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance(userName: String?, userRank: String?, userScore: Int) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, userName)
                    putString(ARG_USERRANK, userRank)
                    putInt(ARG_USERSCORE, userScore)
                }
            }
    }
}