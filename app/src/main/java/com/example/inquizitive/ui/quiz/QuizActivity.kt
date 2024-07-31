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

        mQuizViewModel.initialize()
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
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
        }
    }


    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", packageName)
        if (drawableResourceId != 0) {
            binding.ivQuizAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }
}