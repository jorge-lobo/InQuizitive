package com.example.inquizitive.ui.auth.signUp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentSignUpBinding
import com.example.inquizitive.ui.auth.AuthActivity
import com.example.inquizitive.ui.auth.login.LoginFragment
import com.example.inquizitive.ui.avatar.AvatarActivity
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val mSignUpViewModel by lazy { ViewModelProvider(this)[SignUpViewModel::class.java] }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var avatarResultLauncher: ActivityResultLauncher<Intent>

    private var isPasswordVisible = false
    private var isAvatarSelected = false
    private var selectedAvatar: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        avatarResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data = result.data
                    data?.let {
                        val avatarString = it.getStringExtra(AppConstants.KEY_SELECTED_AVATAR)
                        binding.apply {
                            etSignUpInputUsername.setText(it.getStringExtra(AppConstants.KEY_NEW_USERNAME))
                            etSignUpInputPassword.setText(it.getStringExtra(AppConstants.KEY_NEW_PASSWORD))
                            etSignUpInputConfirmation.setText(it.getStringExtra(AppConstants.KEY_NEW_CONFIRMATION))
                        }
                        updateAvatar(avatarString)
                        selectedAvatar = avatarString ?: ""
                        isAvatarSelected = true
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up,
            container,
            false
        )
        binding.viewModel = mSignUpViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

        setupObservers()
        setupListeners()
        setupViews()
    }

    private fun setupObservers() {
        mSignUpViewModel.apply {
            signUpSuccessEvent.observe(viewLifecycleOwner) {
                navigateToHome()
                Utils.showToast(requireContext(), "New user created successfully!")
            }

            isUsernameTaken.observe(viewLifecycleOwner) {
                clearInputField(binding.etSignUpInputUsername)

                Utils.showToast(
                    requireContext(),
                    "This username is already in use. Please choose another username."
                )
            }

            isPasswordsNotMatching.observe(viewLifecycleOwner) {
                binding.apply {
                    clearInputField(etSignUpInputPassword, etSignUpInputConfirmation)
                }

                Utils.showToast(requireContext(), "Passwords don't match! Please try again.")
            }

            isAvatarNull.observe(viewLifecycleOwner) {
                isAvatarSelected = false
                updateMissingAvatarWarningUI(true)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnSignUpReset.setOnClickListener {
                clearInputField(
                    etSignUpInputUsername,
                    etSignUpInputPassword,
                    etSignUpInputConfirmation
                )
            }

            flRightBoxSignUpPassword.setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                toggleVisibility(etSignUpInputPassword, ivSignUpPasswordIconView)
            }

            flRightBoxSignUpConfirmation.setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                toggleVisibility(etSignUpInputConfirmation, ivSignUpConfirmationIconView)
            }

            etSignUpInputPassword.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        toggleVisibility(etSignUpInputPassword, ivSignUpPasswordIconView)
                    }
                }

            etSignUpInputConfirmation.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        toggleVisibility(etSignUpInputConfirmation, ivSignUpConfirmationIconView)
                    }
                }

            cbSignUpRememberMe.setOnClickListener { updateRememberMeStatus() }

            cvSignUpAvatarContainer.setOnClickListener { openAvatarActivity() }

            btnSignUpLogin.setOnClickListener {
                (activity as AuthActivity).openFragment(LoginFragment())
            }

            btnSignUpSave.setOnClickListener { getSignUpData() }

            btnSignUpCancel.setOnClickListener {
                Utils.startActivity(requireContext(), HomeActivity::class.java)
            }
        }
    }

    private fun setupViews() {
        setupEditTextWithListeners(
            binding.etSignUpInputUsername,
            binding.etSignUpInputPassword,
            binding.etSignUpInputConfirmation
        ) { checkButtonsState() }
    }

    private fun setupEditTextWithListeners(
        vararg editTexts: EditText,
        textChangedListener: () -> Unit
    ) {
        editTexts.forEach { editText ->
            editText.filters = arrayOf(inputFilter)
            editText.addTextChangedListener { textChangedListener.invoke() }
        }
    }

    private fun navigateToHome() {
        Intent(requireContext(), HomeActivity::class.java).apply {
            mSignUpViewModel.getLoggedInUserId()
                ?.let { putExtra(AppConstants.KEY_CURRENT_USER_ID, it) }
            startActivity(this)
        }
    }

    private fun getSignUpData() {
        binding.apply {
            val username = etSignUpInputUsername.text.toString()
            val password = etSignUpInputPassword.text.toString()
            val confirmation = etSignUpInputConfirmation.text.toString()
            val avatarName = selectedAvatar

            mSignUpViewModel.signUp(username, password, confirmation, avatarName)
        }
    }

    private fun openAvatarActivity() {
        val intent = Intent(requireContext(), AvatarActivity::class.java).apply {
            putExtra(AppConstants.KEY_IS_NEW_USER, true)
            putExtra(AppConstants.KEY_NEW_USERNAME, binding.etSignUpInputUsername.text.toString())
            putExtra(AppConstants.KEY_NEW_PASSWORD, binding.etSignUpInputPassword.text.toString())
            putExtra(
                AppConstants.KEY_NEW_CONFIRMATION,
                binding.etSignUpInputConfirmation.text.toString()
            )
        }
        avatarResultLauncher.launch(intent)
    }

    private fun checkButtonsState() {
        binding.apply {
            val username = etSignUpInputUsername.text.toString()
            val password = etSignUpInputPassword.text.toString()
            val confirmation = etSignUpInputConfirmation.text.toString()

            btnSignUpReset.isEnabled =
                username.isNotEmpty() || password.isNotEmpty() || confirmation.isNotEmpty()
            btnSignUpSave.isEnabled =
                username.isNotEmpty() && password.isNotEmpty() && confirmation.isNotEmpty()

            checkShowInputButtonState(etSignUpInputPassword, flRightBoxSignUpPassword)
            checkShowInputButtonState(etSignUpInputConfirmation, flRightBoxSignUpConfirmation)
        }
        updateRememberMeStatus()
    }

    private fun checkShowInputButtonState(editText: EditText, container: View) {
        container.setBackgroundResource(
            if (editText.text.isNotEmpty()) R.drawable.background_common_square_active
            else R.drawable.background_common_square_normal
        )
    }

    private fun updateRememberMeStatus() {
        val checkbox = binding.cbSignUpRememberMe
        if (checkbox.isChecked) {
            mSignUpViewModel.saveRememberMeToSharedPreferences(true)
        } else {
            mSignUpViewModel.clearSharedPreferences()
        }
    }

    private fun clearInputField(vararg editTexts: EditText) {
        editTexts.forEach { it.text.clear() }
    }

    private fun toggleVisibility(editText: EditText, imageView: ImageView) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        editText.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()

        imageView.setImageResource(if (isPasswordVisible) R.drawable.ic_svg_eye_hide else R.drawable.ic_svg_eye_view)

        editText.setSelection(selectionStart, selectionEnd)
    }

    @SuppressLint("DiscouragedApi")
    fun updateAvatar(selectedAvatar: String?) {
        selectedAvatar?.let { avatarName ->
            val drawableResourceId =
                resources.getIdentifier(avatarName, "drawable", requireContext().packageName)
            if (drawableResourceId != 0) {
                binding.ivSignUpAvatar.ivAvatar.setImageResource(drawableResourceId)
                isAvatarSelected = true
                updateMissingAvatarWarningUI(false)
            }
        }
    }

    private fun updateMissingAvatarWarningUI(isAvatarMissing: Boolean) {
        val header = binding.rlSignUpHeader
        val title = binding.tvSignUpTitle
        val message = binding.tvSignUpMessage
        val warningMessage = binding.tvWarningAvatarMissing

        header.setBackgroundResource(if (isAvatarMissing) R.drawable.background_common_header_warning else R.drawable.background_common_header)
        title.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isAvatarMissing) R.color.header_warning_title else R.color.header_default_title
            )
        )
        message.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isAvatarMissing) R.color.header_primary_txt else R.color.header_secondary_txt
            )
        )

        warningMessage.visibility = if (isAvatarSelected) View.INVISIBLE else View.VISIBLE
    }

    private val inputFilter = InputFilter { source, _, _, _, _, _ ->
        if (source.toString().matches("[a-zA-Z0-9_-]+".toRegex())) null else ""
    }
}