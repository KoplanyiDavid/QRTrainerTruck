package com.vicegym.qrtrainertruck.mainactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vicegym.qrtrainertruck.R
import com.vicegym.qrtrainertruck.authentication.LoginActivity
import com.vicegym.qrtrainertruck.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.package.ACTION_LOGOUT")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("onReceive", "Logout in progress")
                //At this point you should start the login activity and finish this one
                finish()
            }
        }, intentFilter)

        //Fragments
        val homeFragment = HomeFragment.newInstance()
        val trainingsFragment = TrainingsFragment.newInstance()
        val onlineTrainingFragment = OnlineTrainingFragment.newInstance()
        val forumFragment = ForumFragment.newInstance()
        val profileFragment = ProfileFragment.newInstance()

        setCurrentFragment(homeFragment)

        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottomnav_home -> {
                    setCurrentFragment(homeFragment)
                    true
                }
                R.id.bottomnav_training -> {
                    setCurrentFragment(trainingsFragment)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                Firebase.auth.signOut()
                startActivity(Intent(baseContext, LoginActivity::class.java))
                val broadcastIntent = Intent()
                broadcastIntent.action = "com.package.ACTION_LOGOUT"
                sendBroadcast(broadcastIntent)
                return true
            }
            R.id.menu_TandC -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://docs.google.com/document/d/1jmm1wLmqKIgFZMPiUWM2nMmfHlh4yq1HrLc_-bT-EAo/edit?usp=sharing")
                )
                startActivity(intent)
                return true
            }
            R.id.menu_help -> {
                //TODO
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}