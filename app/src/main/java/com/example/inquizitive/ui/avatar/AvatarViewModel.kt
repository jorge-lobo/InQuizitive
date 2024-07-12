package com.example.inquizitive.ui.avatar

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inquizitive.ui.common.BaseViewModel

class AvatarViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val _avatars = MutableLiveData<List<Int>>()
    private val _isFemale = MutableLiveData<Boolean>()

    val avatars: LiveData<List<Int>> get() = _avatars
    val isFemale: LiveData<Boolean> get() = _isFemale

    init {
        selectFemaleAvatars()
    }

    fun selectFemaleAvatars() {
        _avatars.value = AvatarResourceMap.femaleAvatarResourceMap.values.toList()
        _isFemale.value = true
    }

    fun selectMaleAvatars() {
        _avatars.value = AvatarResourceMap.maleAvatarResourceMap.values.toList()
        _isFemale.value = false
    }

    fun getAvatars(): List<Int> {
        return _avatars.value ?: emptyList()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}