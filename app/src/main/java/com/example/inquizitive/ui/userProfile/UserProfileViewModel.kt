package com.example.inquizitive.ui.userProfile

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import com.example.inquizitive.ui.common.BaseViewModel

class UserProfileViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {


    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}