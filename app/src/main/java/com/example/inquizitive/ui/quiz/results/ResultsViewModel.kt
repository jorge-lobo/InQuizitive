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
    private val _quizData = MutableLiveData<QuizData>()
    private val _userCorrectAnswersRate = MutableLiveData<Double>()
    private val _userTimeRate = MutableLiveData<Double>()
    private val _userTimeRateFormatted = MutableLiveData<String>()
    private val _bestScores = MutableLiveData<BestScores>()
    private val _isNewRecord = MutableLiveData<Boolean>().apply { value = false }
    private val _isNewPersonalBest = MutableLiveData<Boolean>().apply { value = false }

    val username: LiveData<String> get() = _username
    val avatar: LiveData<String> get() = _userAvatar
    val quizData: LiveData<QuizData> get() = _quizData
    val userCorrectAnswerRate: LiveData<Double> get() = _userCorrectAnswersRate
    val userTimeRate: LiveData<Double> get() = _userTimeRate
    val userTimeRateFormatted: LiveData<String> get() = _userTimeRateFormatted
    val isNewRecord: LiveData<Boolean> get() = _isNewRecord
    val isNewPersonalBest: LiveData<Boolean> get() = _isNewPersonalBest

    data class QuizData(
        val points: Int,
        val coins: Int,
        val correctAnswers: Int,
        val timeSpent: Int,
        val totalTime: Int
    )

    data class BestScores(
        val globalBest: Int,
        val personalBest: Int
    )

    fun initialize(points: Int, coins: Int, correctAnswers: Int, totalTime: Int, timeSpent: Int) {
        _quizData.value = QuizData(points, coins, correctAnswers, timeSpent, totalTime)
        loadLoggedInUser()
        updateRates()
        loadResults()
    }

    private fun loadLoggedInUser() {
        viewModelScope.launch {
            handleLoadingState {
                val user = getLoggedInUserId()?.let { userRepository.getUserById(it) }
                _loggedInUSer.value = user
                user?.let { updateUserLiveData(it) } ?: onError("User not found")
            }
        }
    }

    private fun loadResults() {
        viewModelScope.launch {
            handleLoadingState {
                resultRepository.getResults().let { results ->
                    val bestScore = results.maxByOrNull { it.score ?: 0 }?.score ?: 0
                    _bestScores.value = BestScores(bestScore, _loggedInUSer.value?.bestResult ?: 0)
                    checkNewRecord(bestScore)
                }
            }
        }
    }

    private fun updateUserLiveData(user: User) {
        _username.value = user.username.orEmpty()
        _userAvatar.value = user.avatar.orEmpty()
    }

    private fun updateRates() {
        _quizData.value?.let { quiz ->
            _userCorrectAnswersRate.value = calculateRate(10, quiz.correctAnswers)
            val timeRate = calculateRate(quiz.totalTime, quiz.timeSpent)
            _userTimeRate.value = timeRate
            _userTimeRateFormatted.value = timeRate.formatPercentage()
        }
    }

    private fun calculateRate(total: Int, value: Int): Double {
        return if (total == 0) 0.0 else (value.toDouble() / total) * 100
    }

    private fun Double.formatPercentage(): String {
        return String.format("%.0f %%", this)
    }

    private fun checkNewRecord(globalBestScore: Int) {
        _quizData.value?.points?.let { score ->
            when {
                score > globalBestScore -> _isNewRecord.value = true
                score > (_bestScores.value?.personalBest ?: 0) -> _isNewPersonalBest.value = true
                else -> {
                    _isNewRecord.value = false
                    _isNewPersonalBest.value = false
                }
            }
        }
    }

    fun saveData() {
        saveUserData()
        saveResultsData()
    }

    private fun saveUserData() {
        val userId = getLoggedInUserId() ?: return
        _loggedInUSer.value?.let { user ->
            _quizData.value?.let { quiz ->
                userRepository.updateUser(
                    userId,
                    user.quizzesPlayed?.inc() ?: 1,
                    user.totalPoints?.plus(quiz.points) ?: quiz.points,
                    maxOf(quiz.points, user.bestResult ?: 0),
                    user.totalTime?.plus(quiz.totalTime) ?: quiz.totalTime,
                    user.spentTime?.plus(quiz.timeSpent) ?: quiz.timeSpent,
                    user.totalAnswers?.plus(10) ?: 10,
                    user.correctAnswers?.plus(quiz.correctAnswers) ?: quiz.correctAnswers
                )
            }
        }
    }

    private fun saveResultsData() {
        val resultId = resultRepository.getNextResultId()
        getLoggedInUserId()?.let { userId ->
            _quizData.value?.let { quiz ->
                resultRepository.saveResult(Result(resultId, userId, quiz.points))
            }
        }
    }

    fun getLoggedInUserId(): Int? {
        return prefs.getInt(AppConstants.KEY_CURRENT_USER_ID, -1).takeIf { it != -1 }
    }

    private fun handleLoadingState(block: suspend () -> Unit) {
        isLoading.value = true
        noDataAvailable.value = false
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                isLoading.value = false
                isRefreshing.value = false
            }
        }
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}