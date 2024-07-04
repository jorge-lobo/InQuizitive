package com.example.inquizitive.ui.auth.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentSignUpBinding
import com.example.inquizitive.ui.common.BaseFragment

class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val mSignUpViewModel by lazy { ViewModelProvider(this)[SignUpViewModel::class.java] }

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

        setupObservers()
    }

    private fun setupObservers() {

    }


}