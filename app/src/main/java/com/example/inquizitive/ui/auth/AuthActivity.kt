package com.example.inquizitive.ui.auth

import android.content.Context
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
import com.example.inquizitive.ui.home.auth.AuthFragment
import com.example.inquizitive.ui.home.userData.UserDataFragment
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils
import kotlin.properties.Delegates

class AuthActivity : AppCompatActivity() {

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

        isLogin = intent.getBooleanExtra("isLogin", true)

        setupViews()
        setupObservers()
        setupListeners()
    }

    private fun setupViews() {
        if (isLogin) {
            configureLoginUI()
        } else {
            configureSignUpUI()
        }
    }

    private fun setupObservers() {
        mAuthViewModel.apply {

        }
    }

    private fun setupListeners() {
        binding.apply {
            btnAuthAlternative.setOnClickListener {
                isLogin = if (isLogin) {
                    openFragment(SignUpFragment())
                    configureSignUpUI()
                    false
                } else {
                    openFragment(LoginFragment())
                    configureLoginUI()
                    true
                }
            }

            btnAuthCancel.setOnClickListener {
                Utils.startActivity(this@AuthActivity, HomeActivity::class.java)
                finish()
            }
        }

    }

    private fun configureLoginUI() {
        binding.apply {
            tvAuthQuestion.setText(R.string.auth_question_login)
            btnAuthAlternative.setText(R.string.button_sign_up)
            btnAuthenticate.text = getText(R.string.button_login)
        }
        openFragment(LoginFragment())
    }

    private fun configureSignUpUI() {
        binding.apply {
            tvAuthQuestion.setText(R.string.auth_question_sign_up)
            btnAuthAlternative.setText(R.string.button_login)
            btnAuthenticate.setText(R.string.button_create_account)
        }
        openFragment(SignUpFragment())
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_auth_container, fragment)
            .commit()
    }

}