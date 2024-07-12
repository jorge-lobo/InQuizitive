package com.example.inquizitive.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

        setInitialFragment()
        setupObservers()
        setupListeners()
    }

    private fun setInitialFragment() {
        openFragment(if (isLogin) LoginFragment() else SignUpFragment())
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

            signUpFailureEvent.observe(this@AuthActivity) {
                (supportFragmentManager.findFragmentById(R.id.fl_auth_container) as? SignUpFragment)?.clearInputFields()
            }

            isUsernameTaken.observe(this@AuthActivity) {
                Utils.showToast(
                    this@AuthActivity,
                    "This username is already in use. Please choose another username."
                )
            }

            isPasswordsNotMatching.observe(this@AuthActivity) {
                Utils.showToast(this@AuthActivity, "Passwords don't match! Please try again.")
            }
        }
    }

    override fun onLoginDataReady(username: String, password: String) {
        mAuthViewModel.login(username, password)
    }

    override fun onLoginDataStateChanged(isEnabled: Boolean) {
        binding.btnAuthenticate.isEnabled = isEnabled
    }

    override fun onSignUpDataReady(username: String, password: String, confirmation: String) {
        mAuthViewModel.signUp(username, password, confirmation)
    }

    override fun onSignUpDataStateChanged(isEnabled: Boolean) {
        binding.btnAuthenticate.isEnabled = isEnabled
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