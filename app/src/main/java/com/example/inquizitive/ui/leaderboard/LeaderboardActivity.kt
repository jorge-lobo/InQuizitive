package com.example.inquizitive.ui.leaderboard

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityLeaderboardBinding

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private val mLeaderboardViewModel by lazy { ViewModelProvider(this)[LeaderboardViewModel::class.java] }
    private val leaderboardAdapter = LeaderboardAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityLeaderboardBinding?>(
            this,
            R.layout.activity_leaderboard
        ).apply {
            viewModel = mLeaderboardViewModel
            lifecycleOwner = this@LeaderboardActivity
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvLeaderboard.apply {
            adapter = leaderboardAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        mLeaderboardViewModel.apply {
            sortedResults.observe(this@LeaderboardActivity) { results ->
                results?.let {
                    leaderboardAdapter.submitList(it)
                }
            }

            bestUsername.observe(this@LeaderboardActivity) { username ->
                binding.tvLeaderboardHeaderUsername.text = username
            }

            bestAvatar.observe(this@LeaderboardActivity) { avatarName ->
                avatarName?.let { updateAvatar(it) }
            }

            bestScore.observe(this@LeaderboardActivity) { score ->
                binding.tvLeaderboardHeaderScore.text = score.toString()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnLeaderboardHome.setOnClickListener {
                finish()
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", this.packageName)
        if (drawableResourceId != 0) {
            binding.ivLeaderboardAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }
}