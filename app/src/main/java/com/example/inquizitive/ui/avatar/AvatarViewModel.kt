package com.example.inquizitive.ui.avatar

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel

class AvatarViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository: UserRepository = UserRepository(application)

    private val _avatars = MutableLiveData<List<Int>>()
    private val _isFemale = MutableLiveData<Boolean>()
    private val _user = MutableLiveData<User?>()

    val avatars: LiveData<List<Int>> get() = _avatars
    val user: LiveData<User?> get() = _user

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

    fun setGender(isFemale: Boolean) {
        _isFemale.value = isFemale
    }

    fun updateUserAvatar(userId: Int, newAvatar: String) {
        userRepository.updateUserAvatar(userId, newAvatar)
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}