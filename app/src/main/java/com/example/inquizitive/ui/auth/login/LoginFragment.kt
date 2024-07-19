package com.example.inquizitive.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentLoginBinding
import com.example.inquizitive.ui.auth.AuthActivity
import com.example.inquizitive.ui.auth.signUp.SignUpFragment
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils

class LoginFragment : BaseFragment() {

    private lateinit var binding: FragmentLoginBinding
    private val mLoginViewModel by lazy { ViewModelProvider(this)[LoginViewModel::class.java] }
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<FragmentLoginBinding?>(
            inflater,
            R.layout.fragment_login,
            container,
            false
        ).apply {
            viewModel = mLoginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
        setupViews()
    }

    private fun setupListeners() {
        binding.apply {
            btnLoginReset.setOnClickListener {
                val inputUsername = binding.etLoginInputUsername
                val inputPassword = binding.etLoginInputPassword

                clearInputField(inputUsername)
                clearInputField(inputPassword)
            }

            flRightBoxLoginPassword.setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                togglePasswordVisibility(etLoginInputPassword, ivLoginIconView)
            }

            etLoginInputPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    togglePasswordVisibility(etLoginInputPassword, ivLoginIconView)
                }
            }

            cbLoginRememberMe.setOnClickListener { updateRememberMeStatus() }

            btnLoginNewUser.setOnClickListener {
                (activity as AuthActivity).openFragment(SignUpFragment())
            }

            btnLoginEnter.setOnClickListener { getLoginData() }

            btnLoginCancel.setOnClickListener {
                Utils.startActivity(requireContext(), HomeActivity::class.java)
            }
        }
    }

    private fun setupObservers() {
        mLoginViewModel.apply {
            loginSuccessEvent.observe(viewLifecycleOwner) { navigateToHomeActivity() }

            isUsernameNull.observe(viewLifecycleOwner) {
                val inputUsername = binding.etLoginInputUsername
                clearInputField(inputUsername)
                showErrorMessage(isUsernameNull = true)
            }

            isPasswordIncorrect.observe(viewLifecycleOwner) {
                val inputPassword = binding.etLoginInputPassword
                clearInputField(inputPassword)
                showErrorMessage(isUsernameNull = false)
            }
        }
    }

    private fun setupViews() {
        setupEditTextWithListeners(
            binding.etLoginInputUsername,
            binding.etLoginInputPassword
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

    private fun showErrorMessage(isUsernameNull: Boolean) {
        val message =
            if (isUsernameNull) getString(R.string.login_error_username)
            else getString(R.string.login_error_password)

        binding.apply {
            llLoginHeader.visibility = View.INVISIBLE
            llLoginErrorMessageContainer.visibility = View.VISIBLE
            tvLoginErrorMessage.text = message
        }

        binding.root.postDelayed({
            binding.apply {
                llLoginHeader.visibility = View.VISIBLE
                llLoginErrorMessageContainer.visibility = View.GONE
                tvLoginErrorMessage.text = ""
            }
        }, 2000)
    }

    private fun navigateToHomeActivity() {
        Intent(requireContext(), HomeActivity::class.java).apply {
            mLoginViewModel.getLoggedInUserId()
                ?.let { putExtra(AppConstants.KEY_CURRENT_USER_ID, it) }
            startActivity(this)
        }
    }

    private fun getLoginData() {
        binding.apply {
            val username = etLoginInputUsername.text.toString()
            val password = etLoginInputPassword.text.toString()

            mLoginViewModel.login(username, password)
        }
    }

    private fun checkButtonsState() {
        binding.apply {
            val username = etLoginInputUsername.text.toString()
            val password = etLoginInputPassword.text.toString()

            btnLoginReset.isEnabled = username.isNotEmpty() || password.isNotEmpty()
            btnLoginEnter.isEnabled = username.isNotEmpty() && password.isNotEmpty()
        }

        checkShowPasswordButtonState()
        updateRememberMeStatus()
    }

    private fun checkShowPasswordButtonState() {
        binding.apply {
            val password = etLoginInputPassword.text.toString()
            val buttonContainer = binding.flRightBoxLoginPassword

            buttonContainer.setBackgroundResource(
                if (password.isNotEmpty()) R.drawable.background_common_square_active
                else R.drawable.background_common_square_normal
            )
        }
    }

    private fun updateRememberMeStatus() {
        val checkbox = binding.cbLoginRememberMe

        if (checkbox.isChecked) {
            mLoginViewModel.saveRememberMeToSharedPreferences(true)
        } else {
            mLoginViewModel.clearSharedPreferences()
        }
    }

    private fun clearInputField(editText: EditText) {
        editText.text.clear()
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        editText.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()

        imageView.setImageResource(
            if (isPasswordVisible) R.drawable.ic_svg_eye_hide else R.drawable.ic_svg_eye_view
        )

        editText.setSelection(selectionStart, selectionEnd)
    }

    private val inputFilter = InputFilter { source, _, _, _, _, _ ->
        if (source.toString().matches("[a-zA-Z0-9_-]+".toRegex())) null else ""
    }
}