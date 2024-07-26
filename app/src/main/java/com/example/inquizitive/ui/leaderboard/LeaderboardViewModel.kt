package com.example.inquizitive.ui.leaderboard

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.R
import com.example.inquizitive.data.result.Result
import com.example.inquizitive.data.result.ResultRepository
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.launch

class LeaderboardViewModel(application: Application) : BaseViewModel(application) {

    private val resultRepository = ResultRepository(application)
    private val userRepository = UserRepository(application)

    private val _sortedResults = MutableLiveData<List<LeaderboardBindingItem>>()
    private val _userId = MutableLiveData<Int>()
    private val _username = MutableLiveData<String>()
    private val _score = MutableLiveData<Int>()
    private val _bestUsername = MutableLiveData<String>()
    private val _bestAvatar = MutableLiveData<String>()
    private val _bestScore = MutableLiveData<String>()

    val sortedResults: LiveData<List<LeaderboardBindingItem>> get() = _sortedResults
    val userId: LiveData<Int> get() = _userId
    val username: LiveData<String> get() = _username
    val score: LiveData<Int> get() = _score
    val bestUsername: LiveData<String> get() = _bestUsername
    val bestAvatar: LiveData<String> get() = _bestAvatar
    val bestScore: LiveData<String> get() = _bestScore

    init {
        getResults()
    }

    private fun getResults() {
        viewModelScope.launch {
            isLoading.value = true
            noDataAvailable.value = false
            try {
                val results = resultRepository.getResults()
                processResults(results)
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                isLoading.value = false
                isRefreshing.value = false
            }
        }
    }

    private fun processResults(results: List<Result>) {
        val sortedResults = results.sortedByDescending { it.score }

        extractBestResultDetails(sortedResults)
        updateLeaderboardItems(sortedResults)
    }

    private fun extractBestResultDetails(sortedResults: List<Result>) {
        if (sortedResults.isNotEmpty()) {
            val bestResult = sortedResults.first()
            val bestUser = userRepository.getUserById(bestResult.userId ?: 0)

            _bestUsername.value = bestUser?.username ?: "Unknown"
            _bestAvatar.value = bestUser?.avatar ?: ""

            val formattedScore =
                bestResult.score?.let { Utils.formatNumberWithThousandSeparator(it) }
            _bestScore.value = getApplication<Application>().getString(
                R.string.leaderboard_score_format,
                formattedScore
            )
        }
    }

    private fun updateLeaderboardItems(sortedResults: List<Result>) {
        val topResults = sortedResults.drop(1).take(8)
        val leaderboardItems = topResults.map { result ->
            val user = userRepository.getUserById(result.userId ?: 0)
            LeaderboardBindingItem(result, user ?: User())
        }

        _sortedResults.value = leaderboardItems
        noDataAvailable.value = leaderboardItems.isEmpty()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}