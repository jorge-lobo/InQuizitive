package com.example.inquizitive.ui.avatar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityAvatarBinding
import com.example.inquizitive.ui.userProfile.UserProfileActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class AvatarActivity : AppCompatActivity(), AvatarGridAdapter.OnItemClickListener {

    private lateinit var binding: ActivityAvatarBinding
    private val mAvatarViewModel: AvatarViewModel by viewModels()
    private lateinit var adapter: AvatarGridAdapter

    private var isNewUser = false
    private var selectedAvatarPosition: Int = -1
    private var userId: Int = -1
    private var username = ""
    private var password = ""
    private var confirmation = ""
    private var avatarName = ""
    private var isFemale = false
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_avatar)

        retrieveIntentData()
        setupAdapter()
        setupObservers()
        setupListeners()
        initializeAvatarSelection()
    }

    override fun onItemClick(position: Int) {
        handleAvatarSelection(position)
        changeBtnUpdateAvatarVisibility(true)
    }

    private fun initializeAvatarSelection() {
        if (!isNewUser) {
            checkAvatarGender()
        } else {
            selectInitialFemaleAvatars()
        }
    }

    private fun retrieveIntentData() {
        intent.apply {
            isNewUser = getBooleanExtra(AppConstants.KEY_IS_NEW_USER, true)
            userId = getIntExtra(AppConstants.KEY_CURRENT_USER_ID, -1)
            username = getStringExtra(AppConstants.KEY_NEW_USERNAME).orEmpty()
            password = getStringExtra(AppConstants.KEY_NEW_PASSWORD).orEmpty()
            confirmation = getStringExtra(AppConstants.KEY_NEW_CONFIRMATION).orEmpty()
            avatarName = getStringExtra(AppConstants.KEY_SELECTED_AVATAR).orEmpty()
        }
    }

    private fun setupAdapter() {
        adapter = AvatarGridAdapter(this, mAvatarViewModel.getAvatars(), this)
        binding.gvAvatars.adapter = adapter
    }

    private fun setupObservers() {
        mAvatarViewModel.apply {
            avatars.observe(this@AvatarActivity) { avatars ->
                adapter.updateAvatars(avatars)
                if (isInitialLoad && selectedAvatarPosition != -1) {
                    adapter.selectAvatar(selectedAvatarPosition)
                    changeBtnUpdateAvatarVisibility(true)
                    isInitialLoad = false
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            tbSwitchFemale.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    switchGender(true)
                }
            }

            tbSwitchMale.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    switchGender(false)
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

    private fun switchGender(female: Boolean) {
        isFemale = female
        mAvatarViewModel.setGender(female)
        clearSelection()
        showAvatars(isFemale)
    }

    private fun checkAvatarGender() {
        isFemale = avatarName.startsWith("avatar_f")
        showAvatars(isFemale)
        toggleGenderSwitches(isFemale)
        selectedAvatarPosition = avatarName.filter { it.isDigit() }.toInt() - 1
    }

    private fun showAvatars(isFemale: Boolean) {
        if (isFemale) {
            mAvatarViewModel.selectFemaleAvatars()
            binding.tbSwitchMale.isChecked = false
        } else {
            mAvatarViewModel.selectMaleAvatars()
            binding.tbSwitchFemale.isChecked = false
        }
    }

    private fun toggleGenderSwitches(isFemale: Boolean) {
        binding.apply {
            tbSwitchFemale.isChecked = isFemale
            tbSwitchMale.isChecked = !isFemale
        }
    }

    private fun selectInitialFemaleAvatars() {
        mAvatarViewModel.selectFemaleAvatars()
        isFemale = true
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
        val avatarType = if (isFemale) "f" else "m"
        val avatarString = "avatar_${avatarType}${selectedAvatarPosition + 1}"

        if (isNewUser) {
            navigateToSignUp(avatarString)
        } else {
            mAvatarViewModel.updateUserAvatar(userId, avatarString)
            navigateToUserProfileActivity()
        }
    }

    private fun changeBtnUpdateAvatarVisibility(isEnabled: Boolean) {
        binding.btnAvatarUpdate.isEnabled = isEnabled
    }

    private fun navigateToSignUp(avatarString: String) {
        val intent = Intent().apply {
            putExtra(AppConstants.KEY_NEW_USERNAME, username)
            putExtra(AppConstants.KEY_NEW_PASSWORD, password)
            putExtra(AppConstants.KEY_NEW_CONFIRMATION, confirmation)
            putExtra(AppConstants.KEY_SELECTED_AVATAR, avatarString)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun navigateToUserProfileActivity() {
        Utils.startActivity(this, UserProfileActivity::class.java)
        finish()
    }
}