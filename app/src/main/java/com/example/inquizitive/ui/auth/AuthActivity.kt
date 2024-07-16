package com.example.inquizitive.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityAuthBinding
import com.example.inquizitive.ui.auth.login.LoginFragment
import com.example.inquizitive.ui.auth.signUp.SignUpFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils
import kotlin.properties.Delegates

class AuthActivity : AppCompatActivity(), LoginFragment.OnLoginDataListener,
    SignUpFragment.OnSignUpListener {

    private lateinit var binding: ActivityAuthBinding
    private val mAuthViewModel: AuthViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private var isLogin by Delegates.notNull<Boolean>()
    private var isAvatarSelected = false

    private var username: String = ""
    private var password: String = ""
    private var confirmation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences =
            application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

        isLogin = intent.getBooleanExtra(AppConstants.KEY_IS_LOGIN, true)
        if (intent.getBooleanExtra(AppConstants.KEY_SHOW_SIGN_UP, false)) {
            isLogin = false
        }

        if (intent.hasExtra(AppConstants.KEY_SELECTED_AVATAR)) {
            isAvatarSelected = true
            updateMissingAvatarWarningUI()
        }

        setInitialFragment()
        setupObservers()
        setupListeners()
    }

    private fun setInitialFragment() {
        val showSignUp = intent.getBooleanExtra(AppConstants.KEY_SHOW_SIGN_UP, false)
        openFragment(if (isLogin && !showSignUp) LoginFragment() else SignUpFragment())
        updateUI()
    }

    private fun setupObservers() {
        mAuthViewModel.apply {
            loginSuccessEvent.observe(this@AuthActivity) {
                this@AuthActivity.navigateToHome()
            }

            loginFailureEvent.observe(this@AuthActivity) {
                (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? LoginFragment)?.clearInputFields()
                Utils.showToast(this@AuthActivity, "Login failed. Please check your credentials.")
            }

            signUpSuccessEvent.observe(this@AuthActivity) {
                this@AuthActivity.navigateToHome()
                Utils.showToast(this@AuthActivity, "New user created successfully!")
            }

            isUsernameTaken.observe(this@AuthActivity) {
                (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.clearUsernameInput()
                Utils.showToast(
                    this@AuthActivity,
                    "This username is already in use. Please choose another username."
                )
                val btnSignUp = binding.btnAuthenticate
                applyButtonInactive(btnSignUp)
            }

            isPasswordsNotMatching.observe(this@AuthActivity) {
                (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.clearPasswordInputs()
                Utils.showToast(this@AuthActivity, "Passwords don't match! Please try again.")
                val btnSignUp = binding.btnAuthenticate
                applyButtonInactive(btnSignUp)
            }
        }
    }

    override fun onLoginDataReady(username: String, password: String) {
        this.username = username
        this.password = password

        mAuthViewModel.login(username, password)
    }

    override fun onLoginDataStateChanged(isEnabled: Boolean) {
        binding.btnAuthenticate.isEnabled = isEnabled
    }

    override fun onSignUpDataReady(
        username: String,
        password: String,
        confirmation: String,
        avatarName: String
    ) {
        this.username = username
        this.password = password
        this.confirmation = confirmation

        if (isAvatarSelected) {
            mAuthViewModel.signUp(username, password, confirmation, avatarName)
        } else {
            updateMissingAvatarWarningUI()
        }
    }

    override fun onSignUpDataStateChanged(isEnabled: Boolean) {
        val button = binding.btnAuthenticate
        applyButtonActive(button)
    }

    override fun onAvatarSelected(isAvatarSelected: Boolean) {
        this.isAvatarSelected = isAvatarSelected
        updateMissingAvatarWarningUI()
    }

    private fun setupListeners() {
        binding.apply {
            btnAuthAlternative.setOnClickListener {
                isLogin = !isLogin
                openFragment(if (isLogin) LoginFragment() else SignUpFragment())
                updateUI()
            }

            btnAuthCancel.setOnClickListener {
                Utils.startActivity(this@AuthActivity, HomeActivity::class.java)
                finish()
            }

            btnAuthenticate.setOnClickListener {
                if (isLogin) {
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? LoginFragment
                    fragment?.getLoginData()
                } else {
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment
                    fragment?.getSignUpData()
                }
            }
        }

    }

    private fun updateUI() {
        binding.apply {
            tvAuthQuestion.setText(if (isLogin) R.string.auth_question_login else R.string.auth_question_sign_up)
            btnAuthAlternative.setText(if (isLogin) R.string.button_sign_up else R.string.button_login)
            btnAuthenticate.setText(if (isLogin) R.string.button_login else R.string.button_sign_up)
        }
    }

    private fun updateMissingAvatarWarningUI() {
        val btnSignUp = binding.btnAuthenticate
        changeButtonStyle(btnSignUp)

        binding.tvWarningAvatarMissing.visibility =
            (if (isAvatarSelected) View.INVISIBLE else View.VISIBLE)

    }

    private fun changeButtonStyle(button: Button) {
        val context = button.context

        if (username.isNotEmpty() && password.isNotEmpty() && confirmation.isNotEmpty() && isAvatarSelected) {
            applyButtonActive(button)
            (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.updateMissingAvatarWarningUI(
                false
            )
        } else if (username.isNotEmpty() && password.isNotEmpty() && confirmation.isNotEmpty() && !isAvatarSelected) {
            button.isEnabled = false

            button.setBackgroundResource(R.drawable.shape_button)
            button.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.button_error_bg)
            button.setTextColor(ContextCompat.getColor(context, R.color.button_error_txt))

            val drawable = button.background as GradientDrawable
            drawable.setStroke(
                2,
                ContextCompat.getColor(this, R.color.button_error_stroke)
            )
            (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.updateMissingAvatarWarningUI(
                true
            )

        } else {
            applyButtonInactive(button)
            (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.updateMissingAvatarWarningUI(
                false
            )
        }
    }

    private fun applyButtonActive(button: Button) {
        val context = button.context

        button.isEnabled = true

        button.setBackgroundResource(R.drawable.shape_button)
        button.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.button_primary_bg)
        button.setTextColor(ContextCompat.getColor(context, R.color.button_primary_txt))

        val drawable = button.background as GradientDrawable
        drawable.setStroke(
            2,
            ContextCompat.getColor(this, R.color.button_primary_stroke)
        )
    }

    private fun applyButtonInactive(button: Button) {
        val context = button.context

        button.isEnabled = false

        button.setBackgroundResource(R.drawable.shape_button)
        button.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.button_inactive_bg)
        button.setTextColor(ContextCompat.getColor(context, R.color.button_inactive_txt))

        val drawable = button.background as GradientDrawable
        drawable.setStroke(
            2,
            ContextCompat.getColor(this, R.color.button_inactive_stroke)
        )
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_auth_container, fragment)
            .commit()
    }

    private fun navigateToHome() {
        Intent(this, HomeActivity::class.java).apply {
            mAuthViewModel.getLoggedInUserId()
                ?.let { putExtra(AppConstants.KEY_CURRENT_USER_ID, it) }
            startActivity(this)
        }
        finish()
    }
}