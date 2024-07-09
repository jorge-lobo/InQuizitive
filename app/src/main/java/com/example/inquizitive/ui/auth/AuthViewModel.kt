package com.example.inquizitive.ui.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private var userRepository = UserRepository(application)
    private val prefs: SharedPreferences =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()

    private val _loginSuccessEvent = MutableLiveData<Unit>()
    private val _loginFailureEvent = MutableLiveData<Unit>()

    val loginSuccessEvent: LiveData<Unit> get() = _loginSuccessEvent
    val loginFailureEvent: LiveData<Unit> get() = _loginFailureEvent

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByUsername(username)

            if (user != null && isPasswordCorrect(password, user.password)) {
                _loggedInUser.value = user
                handleLoginSuccess(user)
            } else {
                _loggedInUser.value = null
                handleLoginFailure()
            }
        }
    }

    private fun isPasswordCorrect(enteredPassword: String, storedPassword: String?): Boolean {
        if (storedPassword == null) return false
        val digest = MessageDigest.getInstance("SHA-256")
        val enteredPasswordHash = digest.digest(enteredPassword.toByteArray())
        val storedPasswordHash = digest.digest(storedPassword.toByteArray())

        return enteredPasswordHash.contentEquals(storedPasswordHash)
    }

    private fun saveUserToSharedPreferences(user: User) {
        user.id?.let {
            prefs.edit()
                .putBoolean(AppConstants.IS_LOGGED_IN, true)
                .putInt(AppConstants.KEY_CURRENT_USER_ID, it)
                .apply()
        }
    }

    private fun handleLoginSuccess(user: User) {
        setLoggedInUser(user)
        saveUserToSharedPreferences(user)
        userRepository.saveCurrentUserId(user.id!!)
        _loginSuccessEvent.value = Unit
    }

    private fun setLoggedInUser(user: User) {
        user.id?.let { prefs.edit().putInt(AppConstants.KEY_CURRENT_USER_ID, it).apply() }
        _loggedInUser.value = user
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
    }

    private fun handleLoginFailure() {
        _loginFailureEvent.value = Unit
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}