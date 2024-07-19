package com.example.inquizitive.ui.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityAuthBinding
import com.example.inquizitive.ui.auth.login.LoginFragment
import com.example.inquizitive.ui.auth.signUp.SignUpFragment
import com.example.inquizitive.utils.AppConstants
import kotlin.properties.Delegates

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isLogin by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)

        initPreferences()
        setInitialFragment()
    }

    private fun initPreferences() {
        sharedPreferences =
            application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

        isLogin = intent.getBooleanExtra(AppConstants.KEY_IS_LOGIN, true)
        if (intent.getBooleanExtra(AppConstants.KEY_SHOW_SIGN_UP, false)) {
            isLogin = false
        }
    }

    private fun setInitialFragment() {
        val fragment = if (isLogin) LoginFragment() else SignUpFragment()
        openFragment(fragment)
    }

    fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_auth_container, fragment)
            .commit()
    }
}