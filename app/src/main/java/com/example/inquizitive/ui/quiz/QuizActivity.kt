package com.example.inquizitive.ui.quiz

import android.annotation.SuppressLint
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
import com.example.inquizitive.utils.Utils

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private val mQuizViewModel by lazy { ViewModelProvider(this)[QuizViewModel::class.java] }

    private var isOptionSelected: Boolean = false
    private var isAnswerSubmitted: Boolean = false
    private var isQuizFinished: Boolean = false
    private var selectedAnswer: String? = null

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

            userCoins.observe(this@QuizActivity) { binding.quizCoinsDisplay.tvUserCoins.text = it }

            isHelpAvailable.observe(this@QuizActivity) {
                binding.flBtnHelpContainer.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }

            questionText.observe(this@QuizActivity) { binding.tvQuizQuestion.text = it }

            category.observe(this@QuizActivity) { it?.let { updateCategory(it) } }

            difficulty.observe(this@QuizActivity) { it?.let { updateDifficulty(it) } }

            timeLeft.observe(this@QuizActivity) { binding.tvQuizTimer.text = it }

            options.observe(this@QuizActivity) { updateOptions(it) }

            countdown.observe(this@QuizActivity) { handleCountdown(it) }

            currentQuestionIndex.observe(this@QuizActivity) {updateQuizProgress(it)}
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnSubmit.setOnClickListener {
                handleBtnSubmitClick()
            }

            listOf(rlOptionA, rlOptionB, rlOptionC, rlOptionD).forEach { option ->
                option.setOnClickListener { onOptionSelected(it as RelativeLayout) }
            }
        }
    }

    private fun onOptionSelected(selectedOption: RelativeLayout) {
        val options = listOf(
            binding.rlOptionA,
            binding.rlOptionB,
            binding.rlOptionC,
            binding.rlOptionD
        )

        options.forEach { option ->
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
                setupDefaultOptionUI(option)
            }
        }
    }

    private fun updateOptionUI() {
        val correctOption = mQuizViewModel.correctAnswer.value
        val options = listOf(
            binding.rlOptionA to binding.tvTextOptionA.text.toString(),
            binding.rlOptionB to binding.tvTextOptionB.text.toString(),
            binding.rlOptionC to binding.tvTextOptionC.text.toString(),
            binding.rlOptionD to binding.tvTextOptionD.text.toString()
        )

        options.forEach { (optionLayout, optionText) ->
            when (optionText) {
                correctOption -> setupCorrectOptionUI(optionLayout)
                selectedAnswer -> setupIncorrectOptionUI(optionLayout)
                else -> setupDefaultOptionUI(optionLayout)
            }
        }
    }

    private fun handleBtnSubmitClick() {
        val btnSubmit = binding.btnSubmit
        val currentQuestionIndex = mQuizViewModel.currentQuestionIndex.value ?: 0
        val totalQuestions = mQuizViewModel.totalQuestions.value ?: 0
        if (currentQuestionIndex == totalQuestions - 1) {
            isQuizFinished = true
        }

        if (isOptionSelected && !isAnswerSubmitted) {
            isAnswerSubmitted = true

            btnSubmit.text = if (isQuizFinished) {
                getString(R.string.button_finish)
            } else {
                getString(R.string.button_next)
            }

            mQuizViewModel.apply {
                stopTimer()
                val isCorrect = checkAnswer(selectedAnswer)
                if (isCorrect) {
                    handleCorrectAnswer()
                } else {
                    handleIncorrectAnswer()
                }
            }
        } else if (isAnswerSubmitted) {
            isAnswerSubmitted = false
            isOptionSelected = false
            btnSubmit.apply {
                isEnabled = false
                text = getString(R.string.button_submit_answer)
            }

            if (isQuizFinished) {
                // showResult()
                Utils.showToast(this@QuizActivity, "Show results")
            } else {
                mQuizViewModel.apply {
                    proceedToNextQuestion()
                    startTimerForCurrentDifficulty()
                }
            }
            resetOptions()
        }
    }

    private fun updateBtnSubmit() {
        binding.btnSubmit.isEnabled = isOptionSelected
    }

    private fun handleCorrectAnswer() {
        updateOptionUI()
    }

    private fun handleIncorrectAnswer() {
        updateOptionUI()
    }

    private fun resetOptions() {
        val options = listOf(
            binding.rlOptionA,
            binding.rlOptionB,
            binding.rlOptionC,
            binding.rlOptionD
        )
        options.forEach { setupDefaultOptionUI(it) }
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

    private fun handleCountdown(countdown: Int) {
        binding.tvCountdown.text = countdown.toString()
        if (countdown == 0) {
            binding.flQuizIntroScreen.visibility = View.GONE
            binding.clQuizMain.visibility = View.VISIBLE
            mQuizViewModel.startTimerForCurrentDifficulty()
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