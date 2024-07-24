package com.example.inquizitive.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants

class HomeViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    val internetConnection = MutableLiveData<Boolean>()
    var isNetworkCallbackRegistered = false

    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            internetConnection.postValue(true)
        }

        override fun onLost(network: Network) {
            internetConnection.postValue(false)
        }
    }

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
        isNetworkCallbackRegistered = true
    }

    private fun unregisterNetworkCallback() {
        if (isNetworkCallbackRegistered) {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
            isNetworkCallbackRegistered = false
        }
    }

    fun checkInternetConnectivity() {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        internetConnection.postValue(isConnected)
    }

    fun setInternetConnection(isConnected: Boolean) {
        internetConnection.postValue(isConnected)
    }

    fun checkLoginStatus(): Boolean {
        val sharedPreferences = getApplication<Application>()
            .getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false)
    }

    override fun onCleared() {
        super.onCleared()
        unregisterNetworkCallback()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}