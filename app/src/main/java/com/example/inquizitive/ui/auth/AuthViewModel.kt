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
    private val _signUpSuccess = MutableLiveData<Boolean>()
    private val _signUpSuccessEvent = MutableLiveData<Unit>()
    private val _signUpFailureEvent = MutableLiveData<Unit>()
    private val _isUsernameTaken = MutableLiveData<Boolean>()
    private val _isPasswordsNotMatching = MutableLiveData<Boolean>()

    val loginSuccessEvent: LiveData<Unit> get() = _loginSuccessEvent
    val loginFailureEvent: LiveData<Unit> get() = _loginFailureEvent
    val signUpSuccessEvent: LiveData<Unit> get() = _signUpSuccessEvent
    val signUpFailureEvent: LiveData<Unit> get() = _signUpFailureEvent
    val isUsernameTaken: LiveData<Boolean> get() = _isUsernameTaken
    val isPasswordsNotMatching: LiveData<Boolean> get() = _isPasswordsNotMatching

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

    fun signUp(username: String, password: String, confirmation: String) {
        viewModelScope.launch {
            val signUpSuccessful =
                if (password == confirmation && userRepository.getUserByUsername(username) == null) {
                    val newUser = User(
                        id = userRepository.getNextUserId(),
                        username = username,
                        password = password,
                        avatar = "",
                        quizzesPlayed = 0,
                        totalPoints = 0,
                        bestResult = 0,
                        totalCoins = 0,
                        actualCoins = 0,
                        spentCoins = 0,
                        totalTime = 0,
                        spentTime = 0,
                        totalAnswers = 0,
                        correctAnswers = 0
                    )
                    handleSignUpSuccess(newUser)
                    true
                } else {
                    false
                }

            if (signUpSuccessful) {
                _signUpSuccess.value = true
            } else {
                _signUpSuccess.value = false
                handleSignUpFailure()
                if (userRepository.getUserByUsername(username) != null) {
                    _isUsernameTaken.value = true
                }
                if (password != confirmation) {
                    _isPasswordsNotMatching.value = true
                }
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

    private fun handleLoginFailure() {
        _loginFailureEvent.value = Unit
    }

    private fun handleSignUpSuccess(newUser: User) {
        userRepository.saveUser(newUser)
        setLoggedInUser(newUser)
        saveUserToSharedPreferences(newUser)
        userRepository.saveCurrentUserId(newUser.id!!)
        _signUpSuccessEvent.value = Unit
    }

    private fun handleSignUpFailure() {
        _signUpFailureEvent.value = Unit
    }

    private fun setLoggedInUser(user: User) {
        user.id?.let { prefs.edit().putInt(AppConstants.KEY_CURRENT_USER_ID, it).apply() }
        _loggedInUser.value = user
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
    }


    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}