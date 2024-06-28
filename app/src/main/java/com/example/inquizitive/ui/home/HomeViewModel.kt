package com.example.inquizitive.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants

class HomeViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    fun checkLoginStatus(): Boolean {
        val sharedPreferences = getApplication<Application>()
            .getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(AppConstants.IS_LOGGED_IN, false)
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}