package com.example.inquizitive.ui.home

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityHomeBinding
import com.example.inquizitive.ui.home.auth.AuthFragment
import com.example.inquizitive.ui.home.userData.UserDataFragment
import com.example.inquizitive.ui.leaderboard.LeaderboardActivity
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val mHomeViewModel: HomeViewModel by viewModels()
    private var isInitialConnectionChecked = false

    private val handler = Handler(Looper.getMainLooper())
    private val internetCheckRunnable = object : Runnable {
        override fun run() {
            checkInternetConnectivity()
            if (!mHomeViewModel.internetConnection.value!!) {
                handler.postDelayed(this, 5000)
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            mHomeViewModel.setInternetConnection(true)
        }

        override fun onLost(network: Network) {
            mHomeViewModel.setInternetConnection(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setupObservers()
        setupViews()
        setupListeners()
        openFragment()
        checkInternetConnectivity()
    }

    private fun setupObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            mHomeViewModel.checkLoginStatus()
        }

        mHomeViewModel.apply {
            internetConnection.observe(this@HomeActivity) { isConnected ->
                if (!isInitialConnectionChecked) {
                    isInitialConnectionChecked = true
                    updateUI(isConnected)
                } else {
                    updateUI(isConnected)
                }
            }
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
                Utils.startActivity(this@HomeActivity, LeaderboardActivity::class.java)
            }

            btnConnect.setOnClickListener {
                openWiFiSettings()
                handler.postDelayed(internetCheckRunnable, 5000)
            }
        }
    }

    private fun checkInternetConnectivity() {
        mHomeViewModel.checkInternetConnectivity()
    }

    private fun setupConnectivity() {
        val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback
        )
        mHomeViewModel.isNetworkCallbackRegistered = true
    }

    private fun unregisterNetworkCallback() {
        if (mHomeViewModel.isNetworkCallbackRegistered) {
            val connectivityManager = this.getSystemService(ConnectivityManager::class.java)
            connectivityManager?.unregisterNetworkCallback(networkCallback)
            mHomeViewModel.isNetworkCallbackRegistered = false
        }
    }

    override fun onStart() {
        super.onStart()
        setupConnectivity()
    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkCallback()
    }

    private fun openWiFiSettings() {
        val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(wifiIntent)
    }

    private fun openFragment() {
        mHomeViewModel.checkLoginStatus().let { isLoggedIn ->
            val fragmentToOpen = if (isLoggedIn) UserDataFragment() else AuthFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, fragmentToOpen)
                .commit()
        }
    }

    private fun updateUI(isConnected: Boolean) {
        binding.apply {
            if (isConnected) {
                gameDescriptionContainer.visibility = View.VISIBLE
                internetConnectionWarningContainer.visibility = View.GONE
                btnPlay.apply {
                    mHomeViewModel.checkLoginStatus().let { isLoggedIn ->
                        visibility = if (isLoggedIn) View.VISIBLE else View.INVISIBLE
                    }
                    isEnabled = true
                }
            } else {
                gameDescriptionContainer.visibility = View.GONE
                internetConnectionWarningContainer.visibility = View.VISIBLE
                btnPlay.apply {
                    visibility = View.INVISIBLE
                    isEnabled = false
                }
            }
        }
    }
}