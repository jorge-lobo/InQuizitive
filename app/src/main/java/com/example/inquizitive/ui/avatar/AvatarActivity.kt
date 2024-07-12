package com.example.inquizitive.ui.avatar

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityAvatarBinding
import com.example.inquizitive.ui.auth.AuthActivity
import com.example.inquizitive.ui.auth.signUp.SignUpFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class AvatarActivity : AppCompatActivity(), AvatarGridAdapter.OnItemClickListener {

    private lateinit var binding: ActivityAvatarBinding
    private val mAvatarViewModel: AvatarViewModel by viewModels()
    private lateinit var adapter: AvatarGridAdapter

    private var isNewUser = false
    private var isFemale = true
    private var selectedAvatarPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvatarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapter()
        setupObservers()
        setupListeners()
    }

    override fun onItemClick(position: Int) {
        handleAvatarSelection(position)
        changeBtnUpdateAvatarVisibility(true)
    }

    private fun setupObservers() {
        mAvatarViewModel.apply {
            avatars.observe(this@AvatarActivity) { avatars ->
                adapter.updateAvatars(avatars)
            }

            isFemale.observe(this@AvatarActivity) { isFemale ->
                toggleMode(isFemale)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            tbSwitchFemale.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mAvatarViewModel.selectFemaleAvatars()
                    clearSelection()
                }
            }

            tbSwitchMale.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mAvatarViewModel.selectMaleAvatars()
                    clearSelection()
                }
            }

            ivAvatarCloseButton.setOnClickListener {
                finish()
            }

            btnAvatarUpdate.setOnClickListener {
                updateAvatar()
            }
        }
    }

    private fun setupAdapter() {
        adapter = AvatarGridAdapter(this, mAvatarViewModel.getAvatars(), this)
        binding.gvAvatars.adapter = adapter
    }

    private fun toggleMode(isFemale: Boolean) {
        binding.apply {
            tbSwitchFemale.isChecked = isFemale
            tbSwitchMale.isChecked = !isFemale
        }
    }

    private fun clearSelection() {
        adapter.clearSelection()
        selectedAvatarPosition = -1
        changeBtnUpdateAvatarVisibility(false)
    }

    private fun handleAvatarSelection(position: Int) {
        selectedAvatarPosition = position

    }

    private fun updateAvatar() {
        if (isNewUser) {
            navigateToSignUp()
        } else {
            Utils.startActivity(this, HomeActivity::class.java)
            finish()
        }
    }

    private fun changeBtnUpdateAvatarVisibility(isEnabled: Boolean) {
        binding.btnAvatarUpdate.isEnabled = isEnabled
    }

    private fun navigateToSignUp() {
        val i = Intent(this, AuthActivity::class.java)
            .putExtra(AppConstants.KEY_SHOW_SIGN_UP, true)
        startActivity(i)
        finish()
    }
}