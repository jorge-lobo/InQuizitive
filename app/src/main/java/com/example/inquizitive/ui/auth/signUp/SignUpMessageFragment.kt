package com.example.inquizitive.ui.auth.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.inquizitive.R
import com.example.inquizitive.databinding.FragmentSignUpMessageBinding
import com.example.inquizitive.ui.common.BaseFragment

class SignUpMessageFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpMessageBinding
    private var message: String? = null
    private var isSuccess: Boolean = false

    companion object {
        const val ARG_MESSAGE = "message"
        const val ARG_IS_SUCCESS = "isSuccess"

        fun newInstance(message: String, isSuccess: Boolean): SignUpMessageFragment {
            val fragment = SignUpMessageFragment()
            val args = Bundle().apply {
                putString(ARG_MESSAGE, message)
                putBoolean(ARG_IS_SUCCESS, isSuccess)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            message = it.getString(ARG_MESSAGE)
            isSuccess = it.getBoolean(ARG_IS_SUCCESS)
        }

        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            flDialogBody.setBackgroundResource(
                if (isSuccess) R.drawable.background_dialog_success
                else R.drawable.background_dialog_failure
            )
            
            cvDialogIconContainer.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (isSuccess) R.color.dialog_success_secondary else R.color.dialog_failure_secondary
            )

            tvDialogMessage.text = message

            ivDialogIcon.setImageResource(
                if (isSuccess) R.drawable.ic_svg_check_mark
                else R.drawable.ic_svg_close_delete
            )
        }
    }
}