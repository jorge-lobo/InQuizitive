package com.example.inquizitive.ui.auth.signUp

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

class SignUpViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private var userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _signUpSuccess = MutableLiveData<Boolean>()
    private val _signUpSuccessEvent = MutableLiveData<Unit>()
    private val _signUpFailureEvent = MutableLiveData<Unit>()
    private val _isUsernameTaken = MutableLiveData<Boolean>()
    private val _isPasswordsNotMatching = MutableLiveData<Boolean>()
    private val _isAvatarNull = MutableLiveData<Boolean>()

    val signUpSuccessEvent: LiveData<Unit> get() = _signUpSuccessEvent
    val isUsernameTaken: LiveData<Boolean> get() = _isUsernameTaken
    val isPasswordsNotMatching: LiveData<Boolean> get() = _isPasswordsNotMatching
    val isAvatarNull: LiveData<Boolean> get() = _isAvatarNull

    /*fun signUp(username: String, password: String, confirmation: String, avatarName: String) {
        viewModelScope.launch {
            val signUpSuccessful =
                if (password.isNotEmpty() && confirmation.isNotEmpty() && password == confirmation
                    && userRepository.getUserByUsername(username) == null && avatarName.isNotEmpty()
                ) {
                    val newUser = User(
                        id = userRepository.getNextUserId(),
                        username = username,
                        password = password,
                        avatar = avatarName,
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
                handleSignUpFailure(username, password, confirmation, avatarName)
            }
        }
    }*/

    fun signUp(username: String, password: String, confirmation: String, avatarName: String) {
        viewModelScope.launch {
            if (validateSignUpInputs(username, password, confirmation, avatarName)) {
                val newUser = createNewUser(username, password, avatarName)
                handleSignUpSuccess(newUser)
                _signUpSuccess.value = true
            } else {
                handleSignUpFailure(username, password, confirmation, avatarName)
                _signUpSuccess.value = false
            }
        }
    }

    private fun validateSignUpInputs(username: String, password: String, confirmation: String, avatarName: String): Boolean {
        return password.isNotEmpty() && confirmation.isNotEmpty() && password == confirmation
                && userRepository.getUserByUsername(username) == null && avatarName.isNotEmpty()
    }

    private fun createNewUser(username: String, password: String, avatarName: String): User {
        return User(
            id = userRepository.getNextUserId(),
            username = username,
            password = password,
            avatar = avatarName,
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
    }

    private fun handleSignUpSuccess(newUser: User) {
        userRepository.saveUser(newUser)
        setLoggedInUser(newUser)
        saveUserToSharedPreferences(newUser)
        userRepository.saveCurrentUserId(newUser.id!!)
        _signUpSuccessEvent.value = Unit
    }

    private fun handleSignUpFailure(username: String, password: String, confirmation: String, avatarName: String) {
        _signUpFailureEvent.value = Unit

        if (userRepository.getUserByUsername(username) != null) {
            _isUsernameTaken.value = true
        }
        if (password != confirmation) {
            _isPasswordsNotMatching.value = true
        }
        if (avatarName == "") {
            _isAvatarNull.value = true
        }
    }

    private fun saveUserToSharedPreferences(user: User) {
        prefs.edit().apply {
            putBoolean(AppConstants.KEY_IS_LOGGED_IN, true)
            putInt(AppConstants.KEY_CURRENT_USER_ID, user.id!!)
            apply()
        }
    }

    private fun setLoggedInUser(user: User) {
        prefs.edit().putInt(AppConstants.KEY_CURRENT_USER_ID, user.id!!).apply()
        _loggedInUser.value = user
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
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