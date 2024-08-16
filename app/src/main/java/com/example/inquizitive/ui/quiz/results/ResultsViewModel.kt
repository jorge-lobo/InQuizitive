package com.example.inquizitive.ui.quiz.results

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.data.result.Result
import com.example.inquizitive.data.result.ResultRepository
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants
import kotlinx.coroutines.launch

class ResultsViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val resultRepository = ResultRepository(application)
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
    private val _globalBestScore = MutableLiveData<Int>()
    private val _userBestScore = MutableLiveData<Int>()
    private val _isNewRecord = MutableLiveData<Boolean>().apply { value = false }
    private val _isNewPersonalBest = MutableLiveData<Boolean>().apply { value = false }

    val username: LiveData<String> get() = _username
    val avatar: LiveData<String> get() = _userAvatar
    val quizCoins: LiveData<Int> get() = _quizCoins
    val quizPoints: LiveData<Int> get() = _quizPoints
    val quizCorrectAnswers: LiveData<Int> get() = _quizCorrectAnswers
    val quizTimeSpent: LiveData<Int> get() = _quizTimeSpent
    val userCorrectAnswerRate: LiveData<Double> get() = _userCorrectAnswersRate
    val userTimeRate: LiveData<Double> get() = _userTimeRate
    val userTimeRateFormatted: LiveData<String> get() = _userTimeRateFormatted
    val isNewRecord: LiveData<Boolean> get() = _isNewRecord
    val isNewPersonalBest: LiveData<Boolean> get() = _isNewPersonalBest

    fun initialize(points: Int, coins: Int, correctAnswers: Int, totalTime: Int, timeSpent: Int) {
        loadLoggedInUser()
        updateQuizLiveData(points, coins, correctAnswers, totalTime, timeSpent)
        updateCorrectAnswerRate(correctAnswers)
        updateTimeRate(totalTime, timeSpent)
        loadResults()
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

    private fun loadResults() {
        viewModelScope.launch {
            isLoading.value = true
            noDataAvailable.value = false
            try {
                val results = resultRepository.getResults()
                updateBestScore(results)
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                isLoading.value = false
                isRefreshing.value = false
            }
        }
    }

    private fun updateBestScore(results: List<Result>) {
        val bestResult = results.maxByOrNull { it.score ?: 0 }
        _globalBestScore.value = bestResult?.score ?: 0

        checkNewRecord()
    }

    private fun updateUserLiveData(user: User) {
        _username.value = user.username.orEmpty()
        _userAvatar.value = user.avatar.orEmpty()
        _userBestScore.value = user.bestResult ?: 0
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

    private fun updateCorrectAnswerRate(correctAnswers: Int) {
        _userCorrectAnswersRate.value = calculateRate(10, correctAnswers)
    }

    private fun updateTimeRate(totalTime: Int, timeSpent: Int) {
        val timeRate = calculateRate(totalTime, timeSpent)
        _userTimeRate.value = timeRate
        _userTimeRateFormatted.value = String.format("%.0f %%", timeRate)
    }

    private fun calculateRate(total: Int, value: Int): Double {
        if (total == 0) return 0.0
        return (value.toDouble() / total) * 100
    }

    private fun checkNewRecord() {
        val score = _quizPoints.value ?: 0
        val personalBest = _userBestScore.value ?: 0
        val bestScore = _globalBestScore.value ?: 0

        when {
            score > bestScore -> _isNewRecord.value = true
            score in (personalBest + 1)..<bestScore -> _isNewPersonalBest.value = true
            else -> {
                _isNewRecord.value = false
                _isNewPersonalBest.value = false
            }
        }
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}