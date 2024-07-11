package com.example.inquizitive.ui.auth.signUp

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants

class SignUpViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val prefs = application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    fun saveRememberMeToSharedPreferences(isRememberMe: Boolean) {
        prefs.edit().putBoolean(AppConstants.KEY_REMEMBER_ME, isRememberMe).apply()
    }

    fun clearSharedPreferences() {
        prefs.edit().clear().apply()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}