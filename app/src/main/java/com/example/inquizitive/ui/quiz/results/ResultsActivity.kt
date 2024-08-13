package com.example.inquizitive.ui.quiz.results

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityResultsBinding
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.ui.quiz.QuizActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private val mResultsViewModel by lazy { ViewModelProvider(this)[ResultsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityResultsBinding?>(
            this,
            R.layout.activity_results
        ).apply {
            viewModel = mResultsViewModel
            lifecycleOwner = this@ResultsActivity
        }

        mResultsViewModel.initialize(
            intent.getIntExtra(AppConstants.KEY_TOTAL_POINTS, 0),
            intent.getIntExtra(AppConstants.KEY_TOTAL_COINS, 0),
            intent.getIntExtra(AppConstants.KEY_TOTAL_CORRECT_ANSWERS, 0),
            intent.getIntExtra(AppConstants.KEY_TOTAL_TIME, 0),
            intent.getIntExtra(AppConstants.KEY_TOTAL_TIME_SPENT, 0)
        )
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        mResultsViewModel.apply {
            username.observe(this@ResultsActivity) { binding.tvResultsUsername.text = it }

            avatar.observe(this@ResultsActivity) { it?.let { updateAvatar(it) } }

            userCorrectAnswerRate.observe(this@ResultsActivity) { rate ->
                binding.pbResultsAnswersRate.progress = rate.toInt()
            }

            userTimeRate.observe(this@ResultsActivity) { rate ->
                binding.pbResultsTimeRate.progress = rate.toInt()
            }

            quizCorrectAnswers.observe(this@ResultsActivity) { correctAnswers ->
                binding.tvResultsAnswers.text = getString(R.string.results_answers, correctAnswers)
            }

            quizTimeSpent.observe(this@ResultsActivity) { timeSpent ->
                binding.tvResultsTime.text = getString(R.string.results_time, timeSpent)
            }

            quizCoins.observe(this@ResultsActivity) { coinsEarned ->
                binding.tvResultsCoinsEarned.text = getString(R.string.results_coins, coinsEarned)
            }

            quizPoints.observe(this@ResultsActivity) { pointsEarned ->
                binding.tvResultsPointsEarned.text = getString(R.string.results_points, pointsEarned)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnResultsHome.setOnClickListener {
                openActivity(HomeActivity::class.java)
            }

            btnResultsPlayAgain.setOnClickListener {
                openActivity(QuizActivity::class.java)
            }
        }

    }

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", packageName)
        if (drawableResourceId != 0) {
            binding.ivResultsAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }

    private fun openActivity(activityClass: Class<*>) {
        Utils.startActivity(this, activityClass)
        finish()
    }
}