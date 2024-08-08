package com.example.inquizitive.ui.userProfile.logout

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants

class LogoutConfirmationViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val prefs = application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableLiveData<Boolean>()

    fun logout() {
        _isLoggedIn.value = false
        prefs.edit().putBoolean(AppConstants.KEY_IS_LOGGED_IN, false).apply()
        prefs.edit().clear().apply()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}