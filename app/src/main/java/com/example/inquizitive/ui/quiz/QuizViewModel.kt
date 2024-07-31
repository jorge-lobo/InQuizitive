package com.example.inquizitive.ui.quiz

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs = application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userCoins = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val userCoins: LiveData<String> get() = _userCoins
    val userAvatar: LiveData<String> get() = _userAvatar

    fun initialize() {
        loadLoggedInUser()
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
    }

    private fun loadLoggedInUser() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val userId = getLoggedInUserId()
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    _loggedInUser.value = user
                    user?.let { updateUserLiveData(it) } ?: onError("User not found")
                } else {
                    _loggedInUser.value = null
                }
            } catch (e: Exception) {
                onError("Failed to load user: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun updateUserLiveData(user: User) {
        _username.value = user.username ?: ""
        _userCoins.value = Utils.formatNumberWithThousandSeparator(user.actualCoins ?: 0)
        _userAvatar.value = user.avatar ?: ""
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}