package com.example.inquizitive.ui.avatar

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inquizitive.databinding.ActivityAvatarBinding
import com.example.inquizitive.ui.auth.AuthActivity
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class AvatarActivity : AppCompatActivity(), AvatarGridAdapter.OnItemClickListener {

    private lateinit var binding: ActivityAvatarBinding
    private val mAvatarViewModel: AvatarViewModel by viewModels()
    private lateinit var adapter: AvatarGridAdapter

    private var isNewUser = false
    /*private var isFemale = true*/
    private var selectedAvatarPosition: Int = -1
    private var username = ""
    private var password = ""
    private var confirmation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvatarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isNewUser = intent.getBooleanExtra(AppConstants.KEY_IS_NEW_USER, true)
        username = intent.getStringExtra(AppConstants.KEY_NEW_USERNAME).toString()
        password = intent.getStringExtra(AppConstants.KEY_NEW_PASSWORD).toString()
        confirmation = intent.getStringExtra(AppConstants.KEY_NEW_CONFIRMATION).toString()

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
        val intent = Intent().apply {
            putExtra(AppConstants.KEY_NEW_USERNAME, username)
            putExtra(AppConstants.KEY_NEW_PASSWORD, password)
            putExtra(AppConstants.KEY_NEW_CONFIRMATION, confirmation)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}