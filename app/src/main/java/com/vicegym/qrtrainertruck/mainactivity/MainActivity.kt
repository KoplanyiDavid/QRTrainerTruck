package com.vicegym.qrtrainertruck.mainactivity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Fragments
        val homeFragment = HomeFragment.newInstance()
        val signUpFragment = SignUpFragment.newInstance()
        val onlineTrainingFragment = OnlineTrainingFragment.newInstance()
        val forumFragment = ForumFragment.newInstance("param1", "param2")
        val profileFragment = ProfileFragment.newInstance()

        setCurrentFragment(homeFragment)

        binding.bottomNavBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottomnav_home -> {
                    setCurrentFragment(homeFragment)
                    true
                }
                R.id.bottomnav_training -> {
                    setCurrentFragment(signUpFragment)
                    true
                }
                R.id.bottomnav_videos -> {
                    setCurrentFragment(onlineTrainingFragment)
                    true
                }
                R.id.bottomnav_forum -> {
                    setCurrentFragment(forumFragment)
                    true
                }
                R.id.bottomnav_profile -> {
                    setCurrentFragment(profileFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentMainMenu, fragment).commit()
        }
    }
}