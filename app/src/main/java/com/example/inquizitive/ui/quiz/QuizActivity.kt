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

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private val mQuizViewModel by lazy { ViewModelProvider(this)[QuizViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityQuizBinding?>(
            this,
            R.layout.activity_quiz
        ).apply {
            viewModel = mQuizViewModel
            lifecycleOwner = this@QuizActivity
        }

        mQuizViewModel.apply {
            initialize()
            onStart()
        }
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        mQuizViewModel.apply {
            userAvatar.observe(this@QuizActivity) { avatarName ->
                avatarName?.let { updateAvatar(it) }
            }

            userCoins.observe(this@QuizActivity) { actualCoins ->
                binding.quizCoinsDisplay.tvUserCoins.text = actualCoins
            }

            isHelpAvailable.observe(this@QuizActivity) { isAvailable ->
                binding.flBtnHelpContainer.visibility =
                    if (isAvailable) View.VISIBLE else View.INVISIBLE
            }

            questionText.observe(this@QuizActivity) { question ->
                binding.tvQuizQuestion.text = question
            }

            category.observe(this@QuizActivity) { category ->
                if (category != null) {
                    updateCategoryName(category)
                    updateCategoryIcon(category)
                }
            }

            difficulty.observe(this@QuizActivity) { difficulty ->
                if (difficulty != null) {
                    updateDifficulty(difficulty)
                }
            }

            timeLeft.observe(this@QuizActivity) { timeLeft ->
                binding.tvQuizTimer.text = timeLeft
            }

            options.observe(this@QuizActivity) { options ->
                if (options.size == 4) {
                    binding.apply {
                        tvTextOptionA.text = options[0]
                        tvTextOptionB.text = options[1]
                        tvTextOptionC.text = options[2]
                        tvTextOptionD.text = options[3]
                    }
                }
            }

            countdown.observe(this@QuizActivity) { countdown ->
                binding.tvCountdown.text = countdown.toString()
                if (countdown == 0) {
                    binding.flQuizIntroScreen.visibility = View.GONE
                    binding.clQuizMain.visibility = View.VISIBLE
                    mQuizViewModel.startTimerForCurrentDifficulty()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnSubmit.setOnClickListener {
                mQuizViewModel.apply {
                    stopTimer()
                    proceedToNextQuestion()
                    startTimerForCurrentDifficulty()
                }
                resetOptions()
            }

            rlOptionA.setOnClickListener { onOptionSelected(it as RelativeLayout) }
            rlOptionB.setOnClickListener { onOptionSelected(it as RelativeLayout) }
            rlOptionC.setOnClickListener { onOptionSelected(it as RelativeLayout) }
            rlOptionD.setOnClickListener { onOptionSelected(it as RelativeLayout) }
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
            } else {
                setupDefaultOptionUI(option)
            }
        }
    }

    private fun resetOptions() {
        val options = listOf(
            binding.rlOptionA,
            binding.rlOptionB,
            binding.rlOptionC,
            binding.rlOptionD
        )

        options.forEach { option ->
            setupDefaultOptionUI(option)
        }
    }

    private fun setupDefaultOptionUI(option: RelativeLayout) {
        applyStyleToOption(
            option,
            R.drawable.background_common_rectangular_normal,
            ContextCompat.getColor(this, R.color.option_default_txt),
            R.drawable.background_common_square_normal,
            ContextCompat.getColor(this, R.color.option_default_letter),
            R.drawable.background_common_square_normal
        )

        listOf(
            R.id.fl_right_box_option_a,
            R.id.fl_right_box_option_b,
            R.id.fl_right_box_option_c,
            R.id.fl_right_box_option_d
        ).forEach { id ->
            option.findViewById<FrameLayout>(id)?.visibility = View.INVISIBLE
        }
    }

    private fun setupSelectedOptionUI(option: RelativeLayout) {
        applyStyleToOption(
            option,
            R.drawable.background_common_rectangular_selected,
            ContextCompat.getColor(this, R.color.option_selected_txt),
            R.drawable.background_common_square_selected,
            ContextCompat.getColor(this, R.color.option_selected_letter),
            R.drawable.background_common_square_selected
        )
    }

    private fun setupInactiveOptionUI(option: RelativeLayout) {
        applyStyleToOption(
            option,
            R.drawable.background_common_rectangular_inactive,
            ContextCompat.getColor(this, R.color.option_inactive_txt),
            R.drawable.background_common_square_inactive,
            ContextCompat.getColor(this, R.color.option_inactive_letter),
            R.drawable.background_common_square_inactive
        )
    }

    private fun setupCorrectOptionUI(option: RelativeLayout) {
        applyStyleToOption(
            option,
            R.drawable.background_common_rectangular_correct,
            ContextCompat.getColor(this, R.color.option_correct_txt),
            R.drawable.background_common_square_correct,
            ContextCompat.getColor(this, R.color.option_correct_letter),
            R.drawable.background_common_square_correct
        )

        listOf(
            R.id.fl_right_box_option_a,
            R.id.fl_right_box_option_b,
            R.id.fl_right_box_option_c,
            R.id.fl_right_box_option_d
        ).forEach { id ->
            option.findViewById<FrameLayout>(id)?.visibility = View.VISIBLE
        }

        listOf(
            R.id.iv_icon_option_a,
            R.id.iv_icon_option_b,
            R.id.iv_icon_option_c,
            R.id.iv_icon_option_d
        ).forEach { id ->
            option.findViewById<ImageView>(id)?.setImageResource(R.drawable.ic_svg_check_mark)
        }
    }

    private fun setupIncorrectOptionUI(option: RelativeLayout) {
        applyStyleToOption(
            option,
            R.drawable.background_common_rectangular_wrong,
            ContextCompat.getColor(this, R.color.option_wrong_txt),
            R.drawable.background_common_square_wrong,
            ContextCompat.getColor(this, R.color.option_wrong_letter),
            R.drawable.background_common_square_wrong
        )

        listOf(
            R.id.fl_right_box_option_a,
            R.id.fl_right_box_option_b,
            R.id.fl_right_box_option_c,
            R.id.fl_right_box_option_d
        ).forEach { id ->
            option.findViewById<FrameLayout>(id)?.visibility = View.VISIBLE
        }

        listOf(
            R.id.iv_icon_option_a,
            R.id.iv_icon_option_b,
            R.id.iv_icon_option_c,
            R.id.iv_icon_option_d
        ).forEach { id ->
            option.findViewById<ImageView>(id)?.setImageResource(R.drawable.ic_svg_close_delete)
        }
    }

    private fun applyStyleToOption(
        option: RelativeLayout,
        backgroundResource: Int,
        textColor: Int,
        leftFrameLayoutResource: Int,
        charColor: Int,
        rightFrameLayoutResource: Int
    ) {
        option.setBackgroundResource(backgroundResource)

        listOf(
            R.id.tv_text_option_a,
            R.id.tv_text_option_b,
            R.id.tv_text_option_c,
            R.id.tv_text_option_d
        ).forEach { id ->
            option.findViewById<TextView>(id)?.setTextColor(textColor)
        }

        listOf(
            R.id.fl_left_box_option_a,
            R.id.fl_left_box_option_b,
            R.id.fl_left_box_option_c,
            R.id.fl_left_box_option_d
        ).forEach { id ->
            option.findViewById<FrameLayout>(id)?.setBackgroundResource(leftFrameLayoutResource)
        }

        listOf(
            R.id.tv_a,
            R.id.tv_b,
            R.id.tv_c,
            R.id.tv_d
        ).forEach { id ->
            option.findViewById<TextView>(id)?.setTextColor(charColor)
        }

        listOf(
            R.id.fl_right_box_option_a,
            R.id.fl_right_box_option_b,
            R.id.fl_right_box_option_c,
            R.id.fl_right_box_option_d
        ).forEach { id ->
            option.findViewById<FrameLayout>(id)?.setBackgroundResource(rightFrameLayoutResource)
        }
    }

    private fun updateCategoryName(category: String) {
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
    }

    private fun updateCategoryIcon(category: String) {
        val drawableResource = when (category) {
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
        binding.ivCategoryIcon.setImageResource(drawableResource)
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

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", packageName)
        if (drawableResourceId != 0) {
            binding.ivQuizAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }
}