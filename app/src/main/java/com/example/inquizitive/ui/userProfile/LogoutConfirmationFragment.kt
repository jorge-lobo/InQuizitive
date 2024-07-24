package com.example.inquizitive.ui.userProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentLogoutConfirmationBinding
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.utils.AppConstants

class LogoutConfirmationFragment : BaseFragment() {

    private lateinit var binding: FragmentLogoutConfirmationBinding
    private val mLogoutConfirmationViewModel by lazy { ViewModelProvider(this)[LogoutConfirmationViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<FragmentLogoutConfirmationBinding?>(
            inflater,
            R.layout.fragment_logout_confirmation,
            container,
            false
        ).apply {
            viewModel = mLogoutConfirmationViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            btnLogoutCancel.setOnClickListener {
                dismissFragment()
            }

            btnLogoutYes.setOnClickListener {
                handleLogout()
            }
        }
    }

    private fun dismissFragment() {
        parentFragmentManager.popBackStack()
        (activity as UserProfileActivity).updateUI(false)
    }

    private fun handleLogout() {
        mLogoutConfirmationViewModel.logout()
        navigateToHomeActivity()
    }

    private fun navigateToHomeActivity() {
        Intent(requireContext(), HomeActivity::class.java).apply {
            putExtra(AppConstants.KEY_IS_LOGGED_IN, false)
            startActivity(this)
            activity?.finish()
        }
    }
}