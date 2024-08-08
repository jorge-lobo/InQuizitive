package com.example.inquizitive.ui.quiz.exitConfirmation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentExitConfirmationBinding
import com.example.inquizitive.ui.common.BaseFragment
import com.example.inquizitive.ui.home.HomeActivity
import com.example.inquizitive.ui.quiz.QuizActivity

class ExitConfirmationFragment : BaseFragment() {

    private lateinit var binding: FragmentExitConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_exit_confirmation,
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
            btnExitCancel.setOnClickListener {
                dismissFragment()
            }

            btnExitYes.setOnClickListener {
                navigateToHomeActivity()
            }
        }
    }

    private fun dismissFragment() {
        parentFragmentManager.popBackStack()
        (activity as QuizActivity).updateUI(false)
    }

    private fun navigateToHomeActivity() {
        Intent(requireContext(), HomeActivity::class.java).apply {
            startActivity(this)
            activity?.finish()
        }
    }
}