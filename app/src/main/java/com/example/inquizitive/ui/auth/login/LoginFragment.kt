package com.example.inquizitive.ui.auth.login

import android.content.Context
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
import com.example.inquizitive.ui.common.BaseFragment

class LoginFragment : BaseFragment() {

    private lateinit var binding: FragmentLoginBinding
    private val mLoginViewModel by lazy { ViewModelProvider(this)[LoginViewModel::class.java] }

    private var listener: OnLoginDataListener? = null
    private var isPasswordVisible = false

    interface OnLoginDataListener {
        fun onLoginDataReady(username: String, password: String)
        fun onLoginDataStateChanged(isEnabled: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginDataListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnLoginDataReadyListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )
        binding.viewModel = mLoginViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupViews()
    }

    private fun setupListeners() {
        binding.apply {
            btnLoginReset.setOnClickListener {
                clearInputFields()
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

            cbLoginRememberMe.setOnClickListener {
                checkCheckboxStatus()
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

    fun getLoginData() {
        binding.apply {
            val username = etLoginInputUsername.text.toString()
            val password = etLoginInputPassword.text.toString()

            listener?.onLoginDataReady(username, password)
        }
    }

    private fun checkButtonsState() {
        checkResetButtonState()
        checkShowPasswordButtonState()
        checkLoginButtonState()
        checkCheckboxStatus()
    }

    private fun checkResetButtonState() {
        binding.apply {
            val username = etLoginInputUsername.text.toString()
            val password = etLoginInputPassword.text.toString()

            btnLoginReset.isEnabled = username.isNotEmpty() || password.isNotEmpty()
        }
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

    private fun checkLoginButtonState() {
        binding.apply {
            val username = etLoginInputUsername.text.toString()
            val password = etLoginInputPassword.text.toString()

            val isButtonEnabled = username.isNotEmpty() && password.isNotEmpty()
            listener?.onLoginDataStateChanged(isButtonEnabled)
        }
    }

    private fun checkCheckboxStatus() {
        val checkbox = binding.cbLoginRememberMe

        if (checkbox.isChecked) {
            mLoginViewModel.saveRememberMeToSharedPreferences(true)
        } else {
            mLoginViewModel.clearSharedPreferences()
        }
    }

    fun clearInputFields() {
        binding.apply {
            etLoginInputUsername.text.clear()
            etLoginInputPassword.text.clear()
        }
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
        if (source.toString().matches("[a-zA-Z0-9_-]+".toRegex())) {
            null
        } else {
            ""
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}