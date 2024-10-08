package com.example.inquizitive.ui.quiz

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityQuizBinding
import com.example.inquizitive.ui.quiz.exitConfirmation.ExitConfirmationFragment
import com.example.inquizitive.ui.quiz.results.ResultsActivity
import com.example.inquizitive.utils.AppConstants

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private val mQuizViewModel by lazy { ViewModelProvider(this)[QuizViewModel::class.java] }

    private var isOptionSelected: Boolean = false
    private var isAnswerSubmitted: Boolean = false
    private var isTimeOver: Boolean = false
    private var isQuizFinished: Boolean = false
    private var selectedAnswer: String? = null
    private var disabledOptions = mutableListOf<RelativeLayout>()
    private var mediaPlayerIntro: MediaPlayer? = null
    private var mediaPlayerCorrectAnswer: MediaPlayer? = null
    private var mediaPlayerWrongAnswer: MediaPlayer? = null
    private var mediaPlayerTimer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityQuizBinding?>(
            this,
            R.layout.activity_quiz
        ).apply {
            viewModel = mQuizViewModel
            lifecycleOwner = this@QuizActivity
        }

        initializeViewModel()
        setupObservers()
        setupListeners()
        setupHelpCardsUI()
    }

    private fun initializeViewModel() {
        mQuizViewModel.apply {
            initialize()
            onStart()
        }
    }

    private fun setupObservers() {
        mQuizViewModel.apply {
            userAvatar.observe(this@QuizActivity) { it?.let { updateAvatar(it) } }

            userCoinsFormatted.observe(this@QuizActivity) {
                binding.quizCoinsDisplay.tvUserCoins.text = it
            }

            isHelpAvailable.observe(this@QuizActivity) {
                binding.flBtnHelpContainer.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }

            isHelpCardTwoOptionsAvailable.observe(this@QuizActivity) { isVisible ->
                val helpCardTwoOptions = findViewById<View>(R.id.help_card_two_options)
                helpCardTwoOptions.visibility = if (isVisible) View.VISIBLE else View.GONE
            }

            questionText.observe(this@QuizActivity) { binding.tvQuizQuestion.text = it }

            category.observe(this@QuizActivity) { it?.let { updateCategory(it) } }

            difficulty.observe(this@QuizActivity) { it?.let { updateDifficulty(it) } }

            timeLeft.observe(this@QuizActivity) { binding.tvQuizTimer.text = it }

            options.observe(this@QuizActivity) { updateOptions(it) }

            countdown.observe(this@QuizActivity) { handleCountdown(it) }

            currentQuestionIndex.observe(this@QuizActivity) { updateQuizProgress(it) }

            isTimeRunningOut.observe(this@QuizActivity) { updateTimerUI(it) }

            isTimeOver.observe(this@QuizActivity) { handleTimeOver(it) }

            isLoadComplete.observe(this@QuizActivity) { handleLoadComplete(it) }
        }
    }

    private fun setupListeners() {
        val btnHelpCardOneOption = findViewById<View>(R.id.help_card_one_option)
        val btnHelpCardTwoOptions = findViewById<View>(R.id.help_card_two_options)

        btnHelpCardOneOption.setOnClickListener {
            handleOneOptionHelp()
        }

        btnHelpCardTwoOptions.setOnClickListener {
            handleTwoOptionsHelp()
        }

        binding.apply {
            btnBack.setOnClickListener {
                updateUI(true)
                openExitConfirmationFragment()
            }

            tvBtnHelp.setOnClickListener {
                handleHelpContainerVisibility(true)
            }

            flBtnCloseHelpContainer.setOnClickListener {
                handleHelpContainerVisibility(false)
            }

            btnSubmit.setOnClickListener {
                handleBtnSubmitClick()
            }

            listOf(rlOptionA, rlOptionB, rlOptionC, rlOptionD).forEach { option ->
                option.setOnClickListener { onOptionSelected(it as RelativeLayout) }
            }
        }
    }

    private fun setupHelpCardsUI() {
        binding.apply {
            helpCardOneOption.apply {
                tvNameHelpCard.setText(R.string.help_card_dismiss_one)
                tvCoinsHelpCard.setText(R.string.help_card_80)
                ivIconHelpCard.setImageResource(R.drawable.ic_svg_quarter_circle)
            }
            helpCardTwoOptions.apply {
                tvNameHelpCard.setText(R.string.help_card_dismiss_two)
                tvCoinsHelpCard.setText(R.string.help_card_200)
                ivIconHelpCard.setImageResource(R.drawable.ic_svg_half_circle)
            }
        }
    }

    fun updateUI(exit: Boolean) {
        binding.apply {
            body.visibility = if (exit) View.INVISIBLE else View.VISIBLE
            exitFragmentContainer.visibility = if (exit) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateOptionUI() {
        val correctOption = mQuizViewModel.correctAnswer.value
        listOf(
            binding.rlOptionA to binding.tvTextOptionA.text.toString(),
            binding.rlOptionB to binding.tvTextOptionB.text.toString(),
            binding.rlOptionC to binding.tvTextOptionC.text.toString(),
            binding.rlOptionD to binding.tvTextOptionD.text.toString()
        ).forEach { (optionLayout, optionText) ->
            when (optionText) {
                correctOption -> setupCorrectOptionUI(optionLayout)
                selectedAnswer -> setupIncorrectOptionUI(optionLayout)
                else -> setupDefaultOptionUI(optionLayout)
            }
        }
    }

    private fun updateTimerUI(isTimerRunningOut: Boolean) {
        val backgroundRes =
            if (isTimerRunningOut) R.drawable.background_timer_warning else R.drawable.background_timer_normal

        val iconColor = if (isTimerRunningOut) R.color.icon_primary else R.color.icon_secondary

        val textColor =
            if (isTimerRunningOut) R.color.timer_warning_txt else R.color.timer_default_txt

        binding.apply {
            rlTimerBackground.setBackgroundResource(backgroundRes)
            ivIconClock.setColorFilter(ContextCompat.getColor(this@QuizActivity, iconColor))
            tvQuizTimer.setTextColor(ContextCompat.getColor(this@QuizActivity, textColor))
        }

        if (isTimerRunningOut && mediaPlayerTimer == null) {
            mediaPlayerTimer = MediaPlayer.create(applicationContext, R.raw.timer_beat).apply {
                isLooping = true
                start()
            }
        }
    }

    private fun updateTimerVisibility(isTimerVisible: Boolean) {
        binding.rlTimerBackground.visibility = if (isTimerVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun updateCategory(category: String) {
        val textResId = when (category) {
            "film_and_tv" -> R.string.film_and_tv
            "geography" -> R.string.geography
            "society_and_culture" -> R.string.society_and_culture
            "food_and_drink" -> R.string.food_and_drink
            "arts_and_literature" -> R.string.arts_and_literature
            "general_knowledge" -> R.string.general_knowledge
            "history" -> R.string.history
            "science" -> R.string.science
            "sport_and_leisure" -> R.string.sport_and_leisure
            "music" -> R.string.music
            else -> R.string.general_knowledge
        }
        binding.tvCategoryName.text = getText(textResId)

        val drawableResId = when (category) {
            "film_and_tv" -> R.drawable.ic_cat_film_tv
            "geography" -> R.drawable.ic_cat_geography
            "society_and_culture" -> R.drawable.ic_cat_society_culture
            "food_and_drink" -> R.drawable.ic_cat_food_drink
            "arts_and_literature" -> R.drawable.ic_cat_art_literature
            "general_knowledge" -> R.drawable.ic_cat_general_knowlegde
            "history" -> R.drawable.ic_cat_history
            "science" -> R.drawable.ic_cat_science
            "sport_and_leisure" -> R.drawable.ic_cat_sport_leisure
            "music" -> R.drawable.ic_cat_music
            else -> R.drawable.ic_cat_general_knowlegde
        }
        binding.ivCategoryIcon.setImageResource(drawableResId)
    }

    private fun updateDifficulty(difficulty: String) {
        val textResId = when (difficulty) {
            "easy" -> R.string.level_easy
            "medium" -> R.string.level_medium
            "hard" -> R.string.level_hard
            else -> R.string.level_unknown
        }
        binding.tvDifficulty.text = getText(textResId)
    }

    private fun updateQuizProgress(currentQuestionIndex: Int) {
        val currentQuestion = currentQuestionIndex + 1

        binding.apply {
            pbQuizProgress.progress = currentQuestion * 10
            tvQuizProgress.text = getString(R.string.quiz_progress, currentQuestion)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", packageName)
        if (drawableResourceId != 0) {
            binding.ivQuizAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }

    private fun updateOptions(options: List<String>) {
        if (options.size == 4) {
            binding.apply {
                tvTextOptionA.text = options[0]
                tvTextOptionB.text = options[1]
                tvTextOptionC.text = options[2]
                tvTextOptionD.text = options[3]
            }
        }
    }

    private fun updateBtnSubmit() {
        binding.btnSubmit.isEnabled = isOptionSelected
    }

    private fun updateOptionsAvailability(isClickable: Boolean) {
        listOf(binding.rlOptionA, binding.rlOptionB, binding.rlOptionC, binding.rlOptionD).forEach {
            it.isClickable = isClickable
        }
    }

    private fun resetOptions() {
        listOf(binding.rlOptionA, binding.rlOptionB, binding.rlOptionC, binding.rlOptionD).forEach {
            setupDefaultOptionUI(it)
        }
    }

    private fun handleHelpContainerVisibility(isHelpOpen: Boolean) {
        binding.apply {
            rlQuizHeaderContainer.visibility = if (isHelpOpen) View.INVISIBLE else View.VISIBLE
            rlQuizHelpContainer.visibility = if (isHelpOpen) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun onOptionSelected(selectedOption: RelativeLayout) {
        listOf(
            binding.rlOptionA,
            binding.rlOptionB,
            binding.rlOptionC,
            binding.rlOptionD
        ).forEach { option ->
            if (option == selectedOption) {
                setupSelectedOptionUI(option)
                isOptionSelected = true
                updateBtnSubmit()

                selectedAnswer = when (option) {
                    binding.rlOptionA -> binding.tvTextOptionA.text.toString()
                    binding.rlOptionB -> binding.tvTextOptionB.text.toString()
                    binding.rlOptionC -> binding.tvTextOptionC.text.toString()
                    binding.rlOptionD -> binding.tvTextOptionD.text.toString()
                    else -> null
                }
            } else {
                if (option !in disabledOptions) {
                    setupDefaultOptionUI(option)
                }
            }
        }
    }

    private fun handleTimeOver(isTimeOver: Boolean) {
        if (isTimeOver) {
            lockOptions()
            updateTimerVisibility(false)
            stopMediaPlayerTimer()
        }
    }

    private fun handleLoadComplete(isLoadComplete: Boolean) {
        if (isLoadComplete) {
            mQuizViewModel.startCountdown()
        }
    }

    private fun handleBtnSubmitClick() {
        val btnSubmit = binding.btnSubmit
        val currentQuestionIndex = mQuizViewModel.currentQuestionIndex.value ?: 0
        val totalQuestions = mQuizViewModel.totalQuestions.value ?: 0
        isQuizFinished = currentQuestionIndex == totalQuestions - 1

        if (isOptionSelected && !isAnswerSubmitted) {
            isAnswerSubmitted = true
            updateOptionsAvailability(false)
            updateTimerVisibility(false)
            mQuizViewModel.updateBtnHelpVisibility(false)
            stopMediaPlayerTimer()

            btnSubmit.text = if (isQuizFinished) {
                getString(R.string.button_finish)
            } else {
                getString(R.string.button_next)
            }

            mQuizViewModel.apply {
                stopTimer()
                getTimeSpent()
                val isCorrect = checkAnswer(selectedAnswer)
                if (isCorrect) {
                    this@QuizActivity.handleCorrectAnswer()
                } else {
                    handleIncorrectAnswer()
                }
            }
        } else if (isAnswerSubmitted) {
            resetQuizState()

            if (isQuizFinished) {
                navigateToResultsActivity()
            } else {
                proceedToNextQuestion()
            }
        }
    }

    private fun handleCorrectAnswer() {
        updateOptionUI()
        mQuizViewModel.handleCorrectAnswer()
        mediaPlayerCorrectAnswer = playSoundEffect(mediaPlayerCorrectAnswer, R.raw.correct_answer)
    }

    private fun handleIncorrectAnswer() {
        updateOptionUI()
        mediaPlayerWrongAnswer = playSoundEffect(mediaPlayerWrongAnswer, R.raw.wrong_answer)
    }

    private fun handleOneOptionHelp() {
        val correctAnswer = mQuizViewModel.correctAnswer.value
        val options = listOf(
            binding.rlOptionA to binding.tvTextOptionA.text.toString(),
            binding.rlOptionB to binding.tvTextOptionB.text.toString(),
            binding.rlOptionC to binding.tvTextOptionC.text.toString(),
            binding.rlOptionD to binding.tvTextOptionD.text.toString()
        )

        val incorrectOptions = options.filter { it.second != correctAnswer }
        if (incorrectOptions.isNotEmpty()) {
            val optionToDisable = incorrectOptions.random()
            setupInactiveOptionUI(optionToDisable.first)
            optionToDisable.first.isClickable = false
            disabledOptions.add(optionToDisable.first)
        }
        mQuizViewModel.apply {
            updateBtnHelpVisibility(false)
            updateUserCoins(80)
        }
        handleHelpContainerVisibility(false)
    }

    private fun handleTwoOptionsHelp() {
        val correctAnswer = mQuizViewModel.correctAnswer.value
        val options = listOf(
            binding.rlOptionA to binding.tvTextOptionA.text.toString(),
            binding.rlOptionB to binding.tvTextOptionB.text.toString(),
            binding.rlOptionC to binding.tvTextOptionC.text.toString(),
            binding.rlOptionD to binding.tvTextOptionD.text.toString()
        )

        val incorrectOptions = options.filter { it.second != correctAnswer }
        if (incorrectOptions.size >= 2) {
            val optionsToDisable = incorrectOptions.shuffled().take(2)
            optionsToDisable.forEach { optionToDisable ->
                setupInactiveOptionUI(optionToDisable.first)
                optionToDisable.first.isClickable = false
                disabledOptions.add(optionToDisable.first)
            }
        }
        mQuizViewModel.apply {
            updateBtnHelpVisibility(false)
            updateUserCoins(200)
        }
        handleHelpContainerVisibility(false)
    }

    private fun handleCountdown(countdown: Int) {
        binding.tvCountdown.text = countdown.toString()

        if (countdown == 0) {
            binding.flQuizIntroScreen.visibility = View.GONE
            binding.clQuizMain.visibility = View.VISIBLE
            mQuizViewModel.startTimerForCurrentDifficulty()

            mediaPlayerIntro?.release()
            mediaPlayerIntro = null
        } else if (mediaPlayerIntro == null) {
            mediaPlayerIntro = MediaPlayer.create(applicationContext, R.raw.countdown).apply {
                start()
            }
        }
    }

    private fun lockOptions() {
        updateOptionsAvailability(false)
        binding.btnSubmit.apply {
            isEnabled = true
            text = if (isQuizFinished) {
                getString(R.string.button_finish)
            } else {
                getString(R.string.button_next)
            }
        }
        handleIncorrectAnswer()
        isAnswerSubmitted = true
    }

    private fun resetQuizState() {
        isAnswerSubmitted = false
        isTimeOver = false
        isOptionSelected = false
        binding.btnSubmit.apply {
            isEnabled = false
            text = getString(R.string.button_submit_answer)
        }
        updateOptionsAvailability(true)
        resetOptions()
    }

    private fun proceedToNextQuestion() {
        mQuizViewModel.apply {
            proceedToNextQuestion()
            startTimerForCurrentDifficulty()
        }
        updateOptionsAvailability(true)
        updateTimerVisibility(true)
        disabledOptions.clear()
    }

    private fun openExitConfirmationFragment() {
        val fragment = ExitConfirmationFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.exit_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToResultsActivity() {
        val intent = Intent(this, ResultsActivity::class.java)

        intent.apply {
            putExtra(AppConstants.KEY_TOTAL_POINTS, mQuizViewModel.totalPoints.value)
            putExtra(AppConstants.KEY_TOTAL_COINS, mQuizViewModel.totalCoins.value)
            putExtra(
                AppConstants.KEY_TOTAL_CORRECT_ANSWERS,
                mQuizViewModel.totalCorrectAnswers.value
            )
            putExtra(AppConstants.KEY_TOTAL_TIME, mQuizViewModel.totalTime.value)
            putExtra(AppConstants.KEY_TOTAL_TIME_SPENT, mQuizViewModel.totalTimeSpent.value)
        }

        startActivity(intent)
        finish()
    }

    private fun playSoundEffect(mediaPlayer: MediaPlayer?, soundResId: Int): MediaPlayer {
        mediaPlayer?.release()
        return MediaPlayer.create(applicationContext, soundResId).apply {
            start()
        }
    }

    private fun stopMediaPlayer(mediaPlayer: MediaPlayer?) {
        mediaPlayer?.apply {
            stop()
            release()
        }
    }

    private fun stopMediaPlayerTimer() {
        stopMediaPlayer(mediaPlayerTimer)
        mediaPlayerTimer = null
    }

    private fun setupDefaultOptionUI(option: RelativeLayout) = applyStyleToOption(
        option,
        R.drawable.background_common_rectangular_normal,
        R.color.option_default_txt,
        R.drawable.background_common_square_normal,
        R.color.option_default_letter,
        R.drawable.background_common_square_normal
    )

    private fun setupSelectedOptionUI(option: RelativeLayout) = applyStyleToOption(
        option,
        R.drawable.background_common_rectangular_selected,
        R.color.option_selected_txt,
        R.drawable.background_common_square_selected,
        R.color.option_selected_letter,
        R.drawable.background_common_square_selected
    )

    private fun setupInactiveOptionUI(option: RelativeLayout) = applyStyleToOption(
        option,
        R.drawable.background_common_rectangular_inactive,
        R.color.option_inactive_txt,
        R.drawable.background_common_square_inactive,
        R.color.option_inactive_letter,
        R.drawable.background_common_square_inactive
    )

    private fun setupCorrectOptionUI(option: RelativeLayout) = applyStyleToOption(
        option,
        R.drawable.background_common_rectangular_correct,
        R.color.option_correct_txt,
        R.drawable.background_common_square_correct,
        R.color.option_correct_letter,
        R.drawable.background_common_square_correct,
        true,
        R.drawable.ic_svg_check_mark
    )

    private fun setupIncorrectOptionUI(option: RelativeLayout) = applyStyleToOption(
        option,
        R.drawable.background_common_rectangular_wrong,
        R.color.option_wrong_txt,
        R.drawable.background_common_square_wrong,
        R.color.option_wrong_letter,
        R.drawable.background_common_square_wrong,
        true,
        R.drawable.ic_svg_close_delete
    )

    private fun applyStyleToOption(
        option: RelativeLayout,
        backgroundResource: Int,
        textColorRes: Int,
        leftFrameLayoutResource: Int,
        charColorRes: Int,
        rightFrameLayoutResource: Int,
        showRightBox: Boolean = false,
        rightBoxIcon: Int? = null
    ) {
        option.apply {
            setBackgroundResource(backgroundResource)

            updateChildren<TextView>(
                R.id.tv_text_option_a,
                R.id.tv_text_option_b,
                R.id.tv_text_option_c,
                R.id.tv_text_option_d
            ) {
                setTextColor(ContextCompat.getColor(context, textColorRes))
            }

            updateChildren<FrameLayout>(
                R.id.fl_left_box_option_a,
                R.id.fl_left_box_option_b,
                R.id.fl_left_box_option_c,
                R.id.fl_left_box_option_d
            ) {
                setBackgroundResource(leftFrameLayoutResource)
            }

            updateChildren<TextView>(R.id.tv_a, R.id.tv_b, R.id.tv_c, R.id.tv_d) {
                setTextColor(ContextCompat.getColor(context, charColorRes))
            }

            updateChildren<FrameLayout>(
                R.id.fl_right_box_option_a,
                R.id.fl_right_box_option_b,
                R.id.fl_right_box_option_c,
                R.id.fl_right_box_option_d
            ) {
                setBackgroundResource(rightFrameLayoutResource)
                visibility = if (showRightBox) View.VISIBLE else View.INVISIBLE
            }

            rightBoxIcon?.let { icon ->
                updateChildren<ImageView>(
                    R.id.iv_icon_option_a,
                    R.id.iv_icon_option_b,
                    R.id.iv_icon_option_c,
                    R.id.iv_icon_option_d
                ) {
                    setImageResource(icon)
                }
            }
        }
    }

    private inline fun <reified T : View> RelativeLayout.updateChildren(
        vararg ids: Int,
        update: T.() -> Unit
    ) {
        ids.forEach { id ->
            findViewById<T>(id)?.update()
        }
    }
}