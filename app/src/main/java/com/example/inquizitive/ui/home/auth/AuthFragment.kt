package com.example.inquizitive.ui.home.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentAuthBinding
import com.example.inquizitive.ui.auth.AuthActivity
import com.example.inquizitive.utils.AppConstants

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_auth,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            btnHomeLogin.setOnClickListener {
                openAuthActivity(isLogin = true)
            }

            btnHomeSignUp.setOnClickListener {
                openAuthActivity(isLogin = false)
            }
        }
    }

    private fun openAuthActivity(isLogin: Boolean) {
        val i = Intent(activity, AuthActivity::class.java)
            .putExtra(AppConstants.KEY_IS_LOGIN, isLogin)
        startActivity(i)
    }
}