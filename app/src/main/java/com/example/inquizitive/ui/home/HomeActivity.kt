package com.example.inquizitive.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityHomeBinding
import com.example.inquizitive.ui.home.auth.AuthFragment
import com.example.inquizitive.ui.home.userData.UserDataFragment
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val mHomeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setupObservers()
        setupViews()
        setupListeners()
        openFragment()
    }

    private fun setupObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            mHomeViewModel.checkLoginStatus()
        }
    }

    private fun setupViews() {
        binding.apply {
            mHomeViewModel.checkLoginStatus().let { isLoggedIn ->
                btnPlay.visibility = if (isLoggedIn) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnPlay.setOnClickListener {
                Utils.showToast(this@HomeActivity, "Play Quiz")
            }

            btnLeaderboards.setOnClickListener {
                Utils.showToast(this@HomeActivity, "Leaderboards")
            }
        }
    }

    private fun openFragment() {
        mHomeViewModel.checkLoginStatus().let { isLoggedIn ->
            val fragmentToOpen = if (isLoggedIn) UserDataFragment() else AuthFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, fragmentToOpen)
                .commit()
        }
    }
}