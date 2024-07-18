package com.example.inquizitive.ui.home.userData

import android.annotation.SuppressLint
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
        binding = DataBindingUtil.inflate<FragmentUserDataBinding?>(
            inflater,
            R.layout.fragment_user_data,
            container,
            false
        ).apply {
            viewModel = mUserDataViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUserDataViewModel.initialize()
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        mUserDataViewModel.apply {
            userAvatar.observe(viewLifecycleOwner) { avatarName ->
                avatarName?.let { updateAvatar(it) }
            }

            userCoins.observe(viewLifecycleOwner) { actualCoins ->
                binding.userCoinsDisplay.tvUserCoins.text = actualCoins.toString()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnIconUser.setOnClickListener {
                Utils.showToast(requireContext(), "User Profile")
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateAvatar(avatar: String) {
        val drawableResourceId =
            resources.getIdentifier(avatar, "drawable", requireContext().packageName)
        if (drawableResourceId != 0) {
            binding.ivHomeAvatar.ivAvatar.setImageResource(drawableResourceId)
        }
    }
}