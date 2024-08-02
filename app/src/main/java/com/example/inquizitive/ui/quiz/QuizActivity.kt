package com.example.inquizitive.ui.quiz

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

            isLoadComplete.observe(this@QuizActivity) { isLoadComplete ->
                if (isLoadComplete) {
                    mQuizViewModel.startTimerForCurrentDifficulty()
                }
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

            options.observe(this@QuizActivity) { options->
                if (options.size == 4){
                    binding.apply {
                        tvTextOptionA.text = options[0]
                        tvTextOptionB.text = options[1]
                        tvTextOptionC.text = options[2]
                        tvTextOptionD.text = options[3]
                    }
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
            }
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