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
        _username.value = user.username ?: ""
        _userTotalQuizzes.value = formatNumberWithThousandSeparator(user.quizzesPlayed ?: 0)
        _userCorrectAnswers.value =
            "${formatNumberWithThousandSeparator(user.correctAnswers ?: 0)} / ${
                formatNumberWithThousandSeparator(user.quizzesPlayed?.times(10) ?: 0)
            }"
        _userBestResult.value = "${formatNumberWithThousandSeparator(user.bestResult ?: 0)} points"
        _userTotalPoints.value =
            "${formatNumberWithThousandSeparator(user.totalPoints ?: 0)} points"
        _userCurrentCoins.value =
            "${formatNumberWithThousandSeparator(user.actualCoins ?: 0)} coins"
        _userSpentCoins.value = "${formatNumberWithThousandSeparator(user.spentCoins ?: 0)} coins"
        _userTime.value = "${formatNumberWithThousandSeparator(user.spentTime ?: 0)} seconds"
        _userAvatar.value = user.avatar ?: ""

        val correctAnswersRate =
            calculateRate(user.quizzesPlayed?.times(10) ?: 0, user.correctAnswers ?: 0)
        _userCorrectAnswersRate.value = correctAnswersRate
        _userCorrectAnswersRateFormatted.value = String.format("%.1f %%", correctAnswersRate)

        val timeRate = calculateRate(user.totalTime ?: 0, user.spentTime ?: 0)
        _userTimeRate.value = timeRate
        _userTimeRateFormatted.value = String.format("%.1f %%", timeRate)
    }

    private fun calculateRate(total: Int, value: Int): Double {
        if (total == 0) return 0.0
        return (value.toDouble() / total) * 100
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