package com.example.inquizitive.ui.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants

class AuthViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)



    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}