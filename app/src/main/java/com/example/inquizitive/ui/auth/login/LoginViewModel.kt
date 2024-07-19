package com.example.inquizitive.ui.auth.login

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
import java.security.MessageDigest

class LoginViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private var userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _loginSuccessEvent = MutableLiveData<Unit>()
    private val _loginFailureEvent = MutableLiveData<Unit>()
    private val _isUsernameNull = MutableLiveData<Boolean>()
    private val _isPasswordIncorrect = MutableLiveData<Boolean>()

    val loginSuccessEvent: LiveData<Unit> get() = _loginSuccessEvent
    val isUsernameNull: LiveData<Boolean> get() = _isUsernameNull
    val isPasswordIncorrect: LiveData<Boolean> get() = _isPasswordIncorrect

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByUsername(username)

            if (user != null && isPasswordCorrect(password, user.password)) {
                handleLoginSuccess(user)
            } else {
                handleLoginFailure(user, password)
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

    private fun handleLoginSuccess(user: User) {
        _loggedInUser.value = user
        setLoggedInUser(user)
        saveUserToSharedPreferences(user)
        userRepository.saveCurrentUserId(user.id!!)
        _loginSuccessEvent.value = Unit
    }

    private fun handleLoginFailure(user: User?, password: String) {
        _loggedInUser.value = null
        _loginFailureEvent.value = Unit

        if (user != null && !isPasswordCorrect(password, user.password)) {
            _isPasswordIncorrect.value = true
        } else {
            _isUsernameNull.value = true
        }
    }

    private fun setLoggedInUser(user: User) {
        prefs.edit().putInt(AppConstants.KEY_CURRENT_USER_ID, user.id!!).apply()
        _loggedInUser.value = user
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
    }

    private fun saveUserToSharedPreferences(user: User) {
        prefs.edit().apply {
            putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
            putInt(AppConstants.KEY_CURRENT_USER_ID, user.id!!)
            apply()
        }
    }

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