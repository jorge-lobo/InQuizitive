package com.example.inquizitive.ui.auth.signUp

import android.content.Context
import android.content.Intent
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
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentSignUpBinding
import com.example.inquizitive.ui.avatar.AvatarActivity
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.utils.AppConstants

class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val mSignUpViewModel by lazy { ViewModelProvider(this)[SignUpViewModel::class.java] }
    private lateinit var avatarResultLauncher: ActivityResultLauncher<Intent>

    private var listener: OnSignUpListener? = null
    private var isPasswordVisible = false
    private var isNewUser = true

    interface OnSignUpListener {
        fun onSignUpDataReady(username: String, password: String, confirmation: String)
        fun onSignUpDataStateChanged(isEnabled: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSignUpListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSignUpListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        avatarResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data = result.data
                    data?.let {
                        binding.etSignUpInputUsername.setText(it.getStringExtra(AppConstants.KEY_NEW_USERNAME))
                        binding.etSignUpInputPassword.setText(it.getStringExtra(AppConstants.KEY_NEW_PASSWORD))
                        binding.etSignUpInputConfirmation.setText(it.getStringExtra(AppConstants.KEY_NEW_CONFIRMATION))
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

        setupListeners()
        setupViews()
    }

    private fun setupListeners() {
        binding.apply {
            btnSignUpReset.setOnClickListener {
                clearInputFields()
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

            cbSignUpRememberMe.setOnClickListener {
                checkCheckboxStatus()
            }

            cvSignUpAvatarContainer.setOnClickListener {
                openAvatarActivity()
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

    fun getSignUpData() {
        binding.apply {
            val username = etSignUpInputUsername.text.toString()
            val password = etSignUpInputPassword.text.toString()
            val confirmation = etSignUpInputConfirmation.text.toString()

            listener?.onSignUpDataReady(username, password, confirmation)
        }
    }

    private fun openAvatarActivity() {
        val i = Intent(requireContext(), AvatarActivity::class.java).apply {
            putExtra(AppConstants.KEY_IS_NEW_USER, isNewUser)
            putExtra(AppConstants.KEY_NEW_USERNAME, binding.etSignUpInputUsername.text.toString())
            putExtra(AppConstants.KEY_NEW_PASSWORD, binding.etSignUpInputPassword.text.toString())
            putExtra(
                AppConstants.KEY_NEW_CONFIRMATION,
                binding.etSignUpInputConfirmation.text.toString()
            )
        }
        avatarResultLauncher.launch(i)
    }

    private fun checkButtonsState() {
        checkResetButtonState()
        checkShowPasswordButtonState()
        checkShowConfirmationButtonState()
        checkSignUpButtonState()
        checkCheckboxStatus()
    }

    private fun checkResetButtonState() {
        binding.apply {
            val username = etSignUpInputUsername.text.toString()
            val password = etSignUpInputPassword.text.toString()
            val confirmation = etSignUpInputConfirmation.text.toString()

            btnSignUpReset.isEnabled =
                username.isNotEmpty() || password.isNotEmpty() || confirmation.isNotEmpty()
        }
    }

    private fun checkShowPasswordButtonState() {
        binding.apply {
            val password = etSignUpInputPassword.text.toString()
            val buttonContainer = flRightBoxSignUpPassword

            buttonContainer.setBackgroundResource(
                if (password.isNotEmpty()) R.drawable.background_common_square_active
                else R.drawable.background_common_square_normal
            )
        }
    }

    private fun checkShowConfirmationButtonState() {
        binding.apply {
            val confirmation = etSignUpInputConfirmation.text.toString()
            val buttonContainer = flRightBoxSignUpConfirmation

            buttonContainer.setBackgroundResource(
                if (confirmation.isNotEmpty()) R.drawable.background_common_square_active
                else R.drawable.background_common_square_normal
            )
        }
    }

    private fun checkSignUpButtonState() {
        binding.apply {
            val username = etSignUpInputUsername.text.toString()
            val password = etSignUpInputPassword.text.toString()
            val confirmation = etSignUpInputConfirmation.text.toString()

            val isButtonEnabled =
                username.isNotEmpty() && password.isNotEmpty() && confirmation.isNotEmpty()
            listener?.onSignUpDataStateChanged(isButtonEnabled)
        }
    }

    private fun checkCheckboxStatus() {
        val checkbox = binding.cbSignUpRememberMe
        if (checkbox.isChecked) {
            mSignUpViewModel.saveRememberMeToSharedPreferences(true)
        } else {
            mSignUpViewModel.clearSharedPreferences()
        }
    }

    fun clearInputFields() {
        binding.apply {
            etSignUpInputUsername.text.clear()
            etSignUpInputPassword.text.clear()
            etSignUpInputConfirmation.text.clear()
        }
    }

    private fun toggleVisibility(editText: EditText, imageView: ImageView) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        editText.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()

        imageView.setImageResource(if (isPasswordVisible) R.drawable.ic_svg_eye_hide else R.drawable.ic_svg_eye_view)

        editText.setSelection(selectionStart, selectionEnd)
    }

    private var inputFilter = InputFilter { source, _, _, _, _, _ ->
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