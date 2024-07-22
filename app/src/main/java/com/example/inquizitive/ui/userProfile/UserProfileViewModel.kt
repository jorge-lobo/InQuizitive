package com.example.inquizitive.ui.userProfile

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

class UserProfileViewModel(application: Application) : BaseViewModel(application),
    LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userTotalQuizzes = MutableLiveData<String>()
    private val _userCorrectAnswers = MutableLiveData<String>()
    private val _userCorrectAnswersRate = MutableLiveData<Double>()
    private val _userCorrectAnswersRateFormatted = MutableLiveData<String>()
    private val _userBestResult = MutableLiveData<String>()
    private val _userTotalPoints = MutableLiveData<String>()
    private val _userCurrentCoins = MutableLiveData<String>()
    private val _userSpentCoins = MutableLiveData<String>()
    private val _userTime = MutableLiveData<String>()
    private val _userTimeRate = MutableLiveData<Double>()
    private val _userTimeRateFormatted = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()

    val loggedInUser: LiveData<User?> get() = _loggedInUser
    val username: LiveData<String> get() = _username
    val userTotalQuizzes: LiveData<String> get() = _userTotalQuizzes
    val userCorrectAnswers: LiveData<String> get() = _userCorrectAnswers
    val userCorrectAnswersRate: LiveData<Double> get() = _userCorrectAnswersRate
    val userCorrectAnswersRateFormatted: LiveData<String> get() = _userCorrectAnswersRateFormatted
    val userBestResult: LiveData<String> get() = _userBestResult
    val userTotalPoints: LiveData<String> get() = _userTotalPoints
    val userCurrentCoins: LiveData<String> get() = _userCurrentCoins
    val userSpentCoins: LiveData<String> get() = _userSpentCoins
    val userTime: LiveData<String> get() = _userTime
    val userTimeRate: LiveData<Double> get() = _userTimeRate
    val userTimeRateFormatted: LiveData<String> get() = _userTimeRateFormatted
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
        val totalQuizzes = user.quizzesPlayed ?: 0
        val totalAnswers = user.quizzesPlayed?.times(10)
        val correctAnswers = user.correctAnswers ?: 0
        val totalPoints = user.totalPoints ?: 0
        val bestResult = user.bestResult ?: 0
        val currentCoins = user.actualCoins ?: 0
        val spentCoins = user.spentCoins ?: 0
        val spentTime = user.spentTime ?: 0

        _username.value = user.username ?: ""
        _userTotalQuizzes.value = formatNumberWithThousandSeparator(totalQuizzes)
        _userCorrectAnswers.value = "${formatNumberWithThousandSeparator(correctAnswers)} / ${
            totalAnswers?.let {
                formatNumberWithThousandSeparator(
                    it
                )
            }
        }"
        _userBestResult.value = "${formatNumberWithThousandSeparator(bestResult)} points"
        _userTotalPoints.value = "${formatNumberWithThousandSeparator(totalPoints)} points"
        _userCurrentCoins.value = "${formatNumberWithThousandSeparator(currentCoins)} coins"
        _userSpentCoins.value = "${formatNumberWithThousandSeparator(spentCoins)} coins"
        _userTime.value = "${formatNumberWithThousandSeparator(spentTime)} seconds"
        _userAvatar.value = user.avatar ?: ""

        val correctAnswersRate =
            calculateCorrectAnswersRate(
                totalAnswers ?: 0,
                user.correctAnswers ?: 0
            )
        _userCorrectAnswersRate.value = correctAnswersRate
        _userCorrectAnswersRateFormatted.value = String.format("%.1f %%", correctAnswersRate)

        val timeRate = calculateTimeRate(user.totalTime ?: 0, user.spentTime ?: 0)
        _userTimeRate.value = timeRate
        _userTimeRateFormatted.value = String.format("%.1f %%", timeRate)
    }

    private fun calculateCorrectAnswersRate(totalAnswers: Int, correctAnswers: Int): Double {
        if (totalAnswers == 0) return 0.0
        return (correctAnswers.toDouble() / totalAnswers) * 100
    }

    private fun calculateTimeRate(totalTime: Int, spentTime: Int): Double {
        if (totalTime == 0) return 0.0
        return (spentTime.toDouble() / totalTime) * 100
    }

    private fun formatNumberWithThousandSeparator(number: Int): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
        }
        val formatter = DecimalFormat("#,###", symbols)
        return formatter.format(number)
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}