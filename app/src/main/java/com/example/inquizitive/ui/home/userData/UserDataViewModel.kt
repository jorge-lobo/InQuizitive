package com.example.inquizitive.ui.home.userData

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
import kotlinx.coroutines.launch

class UserDataViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userCoins = MutableLiveData<Int>()
    private val _userQuizzes = MutableLiveData<Int>()
    private val _userPoints = MutableLiveData<Int>()
    private val _userBest = MutableLiveData<Int>()
    private val _userAvatar = MutableLiveData<String>()
    private val _welcomeMessage = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val userCoins: LiveData<Int> get() = _userCoins
    val userQuizzes: LiveData<Int> get() = _userQuizzes
    val userPoints: LiveData<Int> get() = _userPoints
    val userBest: LiveData<Int> get() = _userBest
    val userAvatar: LiveData<String> get() = _userAvatar
    val welcomeMessage: LiveData<String> get() = _welcomeMessage

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
        _userCoins.value = user.actualCoins ?: 0
        _userQuizzes.value = user.quizzesPlayed ?: 0
        _userPoints.value = user.totalPoints ?: 0
        _userBest.value = user.bestResult ?: 0
        _userAvatar.value = user.avatar ?: ""
        _welcomeMessage.value = "Hello ${user.username ?: "User"} !"
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}