package com.example.inquizitive.ui.quiz

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.data.question.QuestionRepository
import com.example.inquizitive.data.question.objects.Question
import com.example.inquizitive.data.user.User
import com.example.inquizitive.data.user.UserRepository
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val userRepository = UserRepository(application)
    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    private val _loggedInUser = MutableLiveData<User?>()
    private val _username = MutableLiveData<String>()
    private val _userCoins = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()
    private val _isHelpAvailable = MutableLiveData<Boolean>()
    private val _questions = MutableLiveData<List<Question>>()
    private val _currentQuestionIndex = MutableLiveData<Int>().apply { value = 0 }
    private val _questionText = MutableLiveData<String?>()
    private val _category = MutableLiveData<String?>()
    private val _difficulty = MutableLiveData<String?>()
    private val _isLoadComplete = MutableLiveData<Boolean>().apply { value = false }
    private val _timeLeft = MutableLiveData<String>()
    private val _correctAnswer = MutableLiveData<String?>()
    private val _incorrectAnswers = MutableLiveData<ArrayList<String>>()
    private val _options = MutableLiveData<List<String>>()
    private val _countdown = MutableLiveData<Int>()

    val username: LiveData<String> get() = _username
    val userCoins: LiveData<String> get() = _userCoins
    val userAvatar: LiveData<String> get() = _userAvatar
    val isHelpAvailable: LiveData<Boolean> get() = _isHelpAvailable
    val questionText: LiveData<String?> get() = _questionText
    val category: LiveData<String?> get() = _category
    val difficulty: LiveData<String?> get() = _difficulty
    val timeLeft: LiveData<String> get() = _timeLeft
    val options: LiveData<List<String>> get() = _options
    val countdown: LiveData<Int> get() = _countdown

    private var job: Job? = null

    fun initialize() {
        startIntro()
        loadLoggedInUser()
    }

    fun onStart() {
        getQuestionsFromAPI()
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
                    user?.let { handleUserSuccess(it) } ?: onError("User not found")
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

    private fun handleUserSuccess(user: User) {
        updateUserLiveData(user)
        checkHelpAvailability(user)
    }

    private fun updateUserLiveData(user: User) {
        _username.value = user.username.orEmpty()
        _userCoins.value = Utils.formatNumberWithThousandSeparator(user.actualCoins ?: 0)
        _userAvatar.value = user.avatar.orEmpty()
    }

    private fun getQuestionsFromAPI() {
        isLoading.value = true
        noDataAvailable.value = false

        viewModelScope.launch {
            val questionsResponse = QuestionRepository.getQuestions()

            questionsResponse.result?.let {
                _questions.value = it
                if (it.isNotEmpty()) {
                    _questionText.value = it[0].questionText?.text
                    _category.value = it[0].category
                    _difficulty.value = it[0].difficulty
                    _correctAnswer.value = it[0].correctAnswer
                    _incorrectAnswers.value = it[0].incorrectAnswers
                    shuffleAndSetOptions(it[0].correctAnswer, it[0].incorrectAnswers)
                }
                _isLoadComplete.value = true
            } ?: run {
                onError(questionsResponse.error?.message ?: "Failed to load questions")
                _isLoadComplete.value = false
            }
            isLoading.value = false
        }
    }

    fun proceedToNextQuestion() {
        val currentIndex = _currentQuestionIndex.value ?: return
        val questionList = _questions.value ?: return

        if (currentIndex + 1 < questionList.size) {
            _currentQuestionIndex.value = currentIndex + 1
            _questionText.value = questionList[currentIndex + 1].questionText?.text
            _category.value = questionList[currentIndex + 1].category
            _difficulty.value = questionList[currentIndex + 1].difficulty
            _correctAnswer.value = questionList[currentIndex + 1].correctAnswer
            _incorrectAnswers.value = questionList[currentIndex + 1].incorrectAnswers
            shuffleAndSetOptions(
                questionList[currentIndex + 1].correctAnswer,
                questionList[currentIndex + 1].incorrectAnswers
            )
        }
    }

    private fun shuffleAndSetOptions(correctAnswer: String?, incorrectAnswers: ArrayList<String>) {
        val allOptions = mutableListOf<String>()
        correctAnswer?.let { allOptions.add(it) }
        allOptions.addAll(incorrectAnswers)
        allOptions.shuffle()
        _options.value = allOptions
    }

    private fun checkHelpAvailability(user: User) {
        _isHelpAvailable.value = (user.actualCoins ?: 0) > 79
    }

    private fun startIntro() {
        job = CoroutineScope(Dispatchers.Main).launch {
            for (i in 3 downTo 0) {
                _countdown.value = i
                delay(1000L)
            }
        }
    }

    private fun startTimer(time: Int) {
        job = CoroutineScope(Dispatchers.Main).launch {
            for (i in time downTo 0) {
                val minutes = i / 60
                val seconds = i % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                _timeLeft.value = formattedTime
                delay(1000L)
            }
        }
    }

    fun stopTimer() {
        job?.cancel()
        job = null
    }

    fun startTimerForCurrentDifficulty() {
        val difficulty = _difficulty.value ?: return
        val time = when (difficulty) {
            "easy" -> 20
            "medium" -> 30
            "hard" -> 40
            else -> 30
        }
        startTimer(time)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
    }
}