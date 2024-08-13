package com.example.inquizitive.ui.quiz.results

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

class ResultsViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUSer = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()
    private val _quizCoins = MutableLiveData<Int>()
    private val _quizPoints = MutableLiveData<Int>()
    private val _quizCorrectAnswers = MutableLiveData<Int>()
    private val _quizTimeSpent = MutableLiveData<Int>()
    private val _quizTotalTime = MutableLiveData<Int>()
    private val _userCorrectAnswersRate = MutableLiveData<Double>()
    private val _userTimeRate = MutableLiveData<Double>()
    private val _userTimeRateFormatted = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val avatar: LiveData<String> get() = _userAvatar
    val quizCoins: LiveData<Int> get() = _quizCoins
    val quizPoints: LiveData<Int> get() = _quizPoints
    val quizCorrectAnswers: LiveData<Int> get() = _quizCorrectAnswers
    val quizTimeSpent: LiveData<Int> get() = _quizTimeSpent
    val quizTotalTime: LiveData<Int> get() = _quizTotalTime
    val userCorrectAnswerRate: LiveData<Double> get() = _userCorrectAnswersRate
    val userTimeRate: LiveData<Double> get() = _userTimeRate
    val userTimeRateFormatted: LiveData<String> get() = _userTimeRateFormatted

    fun initialize(points: Int, coins: Int, correctAnswers: Int, totalTime: Int, timeSpent: Int) {
        loadLoggedInUser()
        getCorrectAnswerRate(correctAnswers)
        getTimeRate(totalTime, timeSpent)
        updateQuizLiveData(points, coins, correctAnswers, totalTime, timeSpent)
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
                    _loggedInUSer.value = user
                    user?.let { updateUserLiveData(it) } ?: onError("User not found")
                } else {
                    _loggedInUSer.value = null
                }
            } catch (e: Exception) {
                onError("Failed to load user: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun updateUserLiveData(user: User) {
        _username.value = user.username.orEmpty()
        _userAvatar.value = user.avatar.orEmpty()
    }

    private fun updateQuizLiveData(
        points: Int,
        coins: Int,
        correctAnswers: Int,
        totalTime: Int,
        timeSpent: Int
    ) {
        _quizCoins.value = coins
        _quizPoints.value = points
        _quizCorrectAnswers.value = correctAnswers
        _quizTimeSpent.value = timeSpent
        _quizTotalTime.value = totalTime
    }

    private fun getCorrectAnswerRate(correctAnswers: Int) {
        _userCorrectAnswersRate.value = calculateRate(10, correctAnswers)
    }

    private fun getTimeRate(totalTime: Int, timeSpent: Int) {
        val timeRate = calculateRate(totalTime, timeSpent)
        _userTimeRate.value = timeRate
        _userTimeRateFormatted.value = String.format("%.0f %%", timeRate)
    }

    private fun calculateRate(total: Int, value: Int): Double {
        if (total == 0) return 0.0
        return (value.toDouble() / total) * 100
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}