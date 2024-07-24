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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class UserDataViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userCoins = MutableLiveData<String>()
    private val _userQuizzes = MutableLiveData<String>()
    private val _userPoints = MutableLiveData<String>()
    private val _userBest = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()
    private val _welcomeMessage = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val userCoins: LiveData<String> get() = _userCoins
    val userQuizzes: LiveData<String> get() = _userQuizzes
    val userPoints: LiveData<String> get() = _userPoints
    val userBest: LiveData<String> get() = _userBest
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
        _userCoins.value = formatNumberWithThousandSeparator(user.actualCoins ?: 0)
        _userQuizzes.value = formatNumberWithThousandSeparator(user.quizzesPlayed ?: 0)
        _userPoints.value = formatNumberWithThousandSeparator(user.totalPoints ?: 0)
        _userBest.value = formatNumberWithThousandSeparator(user.bestResult ?: 0)
        _userAvatar.value = user.avatar ?: ""
        _welcomeMessage.value = "Hello ${user.username ?: "User"} !"
    }

    private fun formatNumberWithThousandSeparator(number: Int): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
        }
        return DecimalFormat("#,###", symbols).format(number)
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}