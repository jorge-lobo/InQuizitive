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
    private val _userCoins = MutableLiveData<Int?>()
    private val _userCoinsFormatted = MutableLiveData<String>()
    private val _userAvatar = MutableLiveData<String>()
    private val _userCoinsSpent = MutableLiveData<Int>()
    private val _isHelpAvailable = MutableLiveData<Boolean>()
    private val _isHelpCardTwoOptionsAvailable = MutableLiveData<Boolean>()
    private val _questions = MutableLiveData<List<Question>>()
    private val _totalQuestions = MutableLiveData<Int>().apply { value = 10 }
    private val _currentQuestionIndex = MutableLiveData<Int>().apply { value = 0 }
    private val _questionText = MutableLiveData<String?>()
    private val _category = MutableLiveData<String?>()
    private val _difficulty = MutableLiveData<String?>()
    private val _isLoadComplete = MutableLiveData<Boolean>().apply { value = false }
    private val _timeLeft = MutableLiveData<String>()
    private val _secondsLeft = MutableLiveData<Int>()
    private val _correctAnswer = MutableLiveData<String?>()
    private val _incorrectAnswers = MutableLiveData<ArrayList<String>>()
    private val _options = MutableLiveData<List<String>>()
    private val _countdown = MutableLiveData<Int>()
    private val _isTimeRunningOut = MutableLiveData<Boolean>()
    private val _isTimeOver = MutableLiveData<Boolean>()
    private val _totalPoints = MutableLiveData<Int>().apply { value = 0 }
    private val _totalCoins = MutableLiveData<Int>().apply { value = 0 }
    private val _totalCorrectAnswers = MutableLiveData<Int>().apply { value = 0 }
    private val _totalTime = MutableLiveData<Int>().apply { value = 0 }
    private val _totalTimeSpent = MutableLiveData<Int>().apply { value = 0 }
    private val _isHelpOneOptionUsed = MutableLiveData<Boolean>().apply { value = false }
    private val _isHelpTwoOptionsUsed = MutableLiveData<Boolean>().apply { value = false }

    val username: LiveData<String> get() = _username
    val userCoinsFormatted: LiveData<String> get() = _userCoinsFormatted
    val userAvatar: LiveData<String> get() = _userAvatar
    val isHelpAvailable: LiveData<Boolean> get() = _isHelpAvailable
    val isHelpCardTwoOptionsAvailable: LiveData<Boolean> get() = _isHelpCardTwoOptionsAvailable
    val totalQuestions: LiveData<Int> get() = _totalQuestions
    val currentQuestionIndex: LiveData<Int> get() = _currentQuestionIndex
    val questionText: LiveData<String?> get() = _questionText
    val category: LiveData<String?> get() = _category
    val difficulty: LiveData<String?> get() = _difficulty
    val isLoadComplete: LiveData<Boolean> get() = _isLoadComplete
    val timeLeft: LiveData<String> get() = _timeLeft
    val correctAnswer: LiveData<String?> get() = _correctAnswer
    val options: LiveData<List<String>> get() = _options
    val countdown: LiveData<Int> get() = _countdown
    val isTimeRunningOut: LiveData<Boolean> get() = _isTimeRunningOut
    val isTimeOver: LiveData<Boolean> get() = _isTimeOver
    val totalPoints: LiveData<Int> get() = _totalPoints
    val totalCoins: LiveData<Int> get() = _totalCoins
    val totalCorrectAnswers: LiveData<Int> get() = _totalCorrectAnswers
    val totalTime: LiveData<Int> get() = _totalTime
    val totalTimeSpent: LiveData<Int> get() = _totalTimeSpent

    private var job: Job? = null

    fun initialize() {
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
        _userCoins.value = user.actualCoins ?: 0
        _userCoinsFormatted.value = Utils.formatNumberWithThousandSeparator(user.actualCoins ?: 0)
        _userAvatar.value = user.avatar.orEmpty()
        _userCoinsSpent.value = user.spentCoins ?: 0
    }

    private fun getQuestionsFromAPI() {
        viewModelScope.launch {
            isLoading.value = true
            noDataAvailable.value = false
            QuestionRepository.getQuestions().let { questionsResponse ->
                questionsResponse.result?.let { questions ->
                    handleQuestionsResponse(questions)
                } ?: onError(questionsResponse.error?.message ?: "Failed to load questions")
            }
            isLoading.value = false
        }
    }

    private fun handleQuestionsResponse(questions: List<Question>) {
        _questions.value = questions
        if (questions.isNotEmpty()) {
            updateQuestion(questions[0])
        }
        _isLoadComplete.value = true
    }

    private fun updateQuestion(question: Question) {
        with(question) {
            _questionText.value = questionText?.text
            _category.value = category
            _difficulty.value = difficulty
            _correctAnswer.value = correctAnswer
            _incorrectAnswers.value = incorrectAnswers
            shuffleAndSetOptions(correctAnswer, incorrectAnswers)
        }
    }

    private fun shuffleAndSetOptions(correctAnswer: String?, incorrectAnswers: ArrayList<String>) {
        val allOptions = mutableListOf<String>().apply {
            correctAnswer?.let { add(it) }
            addAll(incorrectAnswers)
            shuffle()
        }
        _options.value = allOptions
    }

    fun checkAnswer(selectedAnswer: String?): Boolean {
        val correctAnswer = _correctAnswer.value
        return selectedAnswer == correctAnswer
    }

    fun proceedToNextQuestion() {
        _questions.value?.let { questionList ->
            _currentQuestionIndex.value?.let { currentIndex ->
                if (currentIndex + 1 < questionList.size) {
                    updateQuestion(questionList[currentIndex + 1])
                    _currentQuestionIndex.value = currentIndex + 1
                    _isHelpOneOptionUsed.value = false
                    _isHelpTwoOptionsUsed.value = false
                    _loggedInUser.value?.let { user ->
                        checkHelpAvailability(user)
                    }
                }
            }
        }
    }

    private fun checkHelpAvailability(user: User) {
        val coins = user.actualCoins ?: 0
        _isHelpAvailable.value = coins > 79
        _isHelpCardTwoOptionsAvailable.value = coins > 199
    }

    fun startCountdown() {
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
                checkTimer(seconds)
                delay(1000L)
            }
        }
    }

    private fun checkTimer(seconds: Int) {
        _secondsLeft.value = seconds
        _isTimeRunningOut.value = seconds < 11
        _isTimeOver.value = seconds == 0
    }

    fun startTimerForCurrentDifficulty() {
        _difficulty.value?.let { difficulty ->
            val time = when (difficulty) {
                "easy" -> 20
                "medium" -> 30
                "hard" -> 40
                else -> 30
            }
            startTimer(time)
        }
    }

    fun stopTimer() {
        job?.cancel()
        job = null
    }

    fun handleCorrectAnswer() {
        updatePoints()
        updateCoins()
        incrementCorrectAnswers()
        _loggedInUser.value?.let { user ->
            user.actualCoins = _userCoins.value
        }
    }

    private fun updatePoints() {
        _difficulty.value?.let { difficulty ->
            _secondsLeft.value?.let { timeLeft ->
                _totalPoints.value?.let { totalPoints ->
                    val pointsPerDifficulty = when (difficulty) {
                        "easy" -> 1
                        "medium" -> 3
                        "hard" -> 9
                        else -> 0
                    }
                    val points = pointsPerDifficulty * timeLeft
                    val pointsEarned = when {
                        _isHelpOneOptionUsed.value == true -> (points * 0.75).toInt()
                        _isHelpTwoOptionsUsed.value == true -> (points * 0.50).toInt()
                        else -> points
                    }
                    val updatedTotalPoints = totalPoints + pointsEarned
                    _totalPoints.value = updatedTotalPoints
                }
            }
        }
    }

    private fun updateCoins() {
        _difficulty.value?.let { difficulty ->
            _totalCoins.value?.let { totalCoins ->
                val coinsPerDifficulty = when (difficulty) {
                    "easy" -> 10
                    "medium" -> 30
                    "hard" -> 90
                    else -> 0
                }
                val coinsEarned = when {
                    _isHelpOneOptionUsed.value == true -> (coinsPerDifficulty * 0.50).toInt()
                    _isHelpTwoOptionsUsed.value == true -> (coinsPerDifficulty * 0.20).toInt()
                    else -> coinsPerDifficulty
                }
                val updatedTotalCoins = totalCoins + coinsEarned
                _totalCoins.value = updatedTotalCoins

                _loggedInUser.value?.let { user ->
                    val updatedCoins = (user.actualCoins ?: 0) + coinsEarned
                    saveUserCoins(updatedCoins)
                }
            }
        }
    }

    fun updateUserCoins(amount: Int) {
        _userCoins.value?.let { currentCoins ->
            val updatedCoins = currentCoins - amount
            saveUserCoins(updatedCoins)
        }
        saveUserSpentCoins(amount)

        _loggedInUser.value?.let { user ->
            user.actualCoins = _userCoins.value
            checkHelpAvailability(user)
        }

        if (amount == 80) {
            _isHelpOneOptionUsed.value = true
        } else if (amount == 200) {
            _isHelpTwoOptionsUsed.value = true
        }
    }

    private fun saveUserCoins(updatedCoins: Int) {
        val userId = getLoggedInUserId() ?: return
        userRepository.updateUserActualCoins(userId, updatedCoins)
        _userCoins.value = updatedCoins
        _userCoinsFormatted.value = Utils.formatNumberWithThousandSeparator(updatedCoins)

        _loggedInUser.value?.let { user ->
            user.actualCoins = updatedCoins
        }
    }

    private fun saveUserSpentCoins(amount: Int) {
        val userId = getLoggedInUserId() ?: return
        _userCoinsSpent.value?.let { spentCoins ->
            val updatedSpentCoins = spentCoins + amount
            userRepository.updateUserSpentCoins(userId, updatedSpentCoins)
            _userCoinsSpent.value = updatedSpentCoins
        }
    }

    fun updateBtnHelpVisibility(isVisible: Boolean) {
        _isHelpAvailable.value = isVisible
    }

    private fun incrementCorrectAnswers() {
        _totalCorrectAnswers.value?.let { totalCorrectAnswers ->
            _totalCorrectAnswers.value = totalCorrectAnswers + 1
        }
    }

    fun getTimeSpent() {
        _difficulty.value?.let { difficulty ->
            _secondsLeft.value?.let { endTime ->
                _totalTime.value?.let { totalTime ->
                    _totalTimeSpent.value?.let { totalTimeSpent ->
                        val startTime = when (difficulty) {
                            "easy" -> 20
                            "medium" -> 30
                            "hard" -> 40
                            else -> 30
                        }
                        val usedTime = startTime - endTime
                        _totalTimeSpent.value = totalTimeSpent + usedTime
                        _totalTime.value = totalTime + startTime
                    }
                }
            }
        }
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