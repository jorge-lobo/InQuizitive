package com.example.inquizitive.ui.home.userData

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentUserDataBinding
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.utils.Utils


class UserDataFragment : BaseFragment() {
    private lateinit var binding: FragmentUserDataBinding
    private val mUserDataViewModel by lazy { ViewModelProvider(this)[UserDataViewModel::class.java] }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_data,
            container,
            false
        )
        binding.viewModel = mUserDataViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        mUserDataViewModel.apply {

        }
    }

    private fun setupListeners() {
        binding.apply {
            btnIconUser.setOnClickListener {
                Utils.showToast(requireContext(), "User Profile")
            }
        }
    }



}