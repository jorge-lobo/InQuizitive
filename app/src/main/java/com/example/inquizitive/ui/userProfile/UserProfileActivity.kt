package com.example.inquizitive.ui.userProfile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityUserProfileBinding
import com.example.inquizitive.ui.avatar.AvatarActivity
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private val mUserProfileViewModel by lazy { ViewModelProvider(this)[UserProfileViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        binding.viewModel = mUserProfileViewModel
        binding.lifecycleOwner = this

        mUserProfileViewModel.initialize()
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        mUserProfileViewModel.apply {
            userAvatar.observe(this@UserProfileActivity) { avatarName ->
                avatarName?.let { updateAvatar(it) }
            }

            userCorrectAnswersRate.observe(this@UserProfileActivity) { rate ->
                binding.pbProfileAnswersRate.progress = rate.toInt()
            }

            userTimeRate.observe(this@UserProfileActivity) { rate ->
                binding.pbProfileTimeRate.progress = rate.toInt()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnProfileLogout.setOnClickListener {
                Utils.showToast(this@UserProfileActivity, "Logout")
            }

            btnProfileHome.setOnClickListener {
                navigateToHomeActivity()
            }

            cvProfileAvatarContainer.setOnClickListener {
                openAvatarActivity()
            }
        }
    }

    private fun openAvatarActivity() {
        val intent = Intent(this, AvatarActivity::class.java).apply {
            putExtra(AppConstants.KEY_IS_NEW_USER, false)
            putExtra(AppConstants.KEY_CURRENT_USER_ID, -1)
            putExtra(AppConstants.KEY_SELECTED_AVATAR, "")
        }
        startActivity(intent)
    }

    private fun navigateToHomeActivity() {
        Intent(this, HomeActivity::class.java).apply {
            mUserProfileViewModel.getLoggedInUserId()
                ?.let { putExtra(AppConstants.KEY_CURRENT_USER_ID, it) }
            startActivity(this)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId = resources.getIdentifier(avatar, "drawable", this.packageName)
        if (drawableResourceId != 0) {
            binding.ivProfileAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }
}