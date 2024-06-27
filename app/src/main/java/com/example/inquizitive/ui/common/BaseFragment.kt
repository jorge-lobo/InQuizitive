package com.example.inquizitive.ui.common

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.inquizitive.R

abstract class BaseFragment : Fragment() {
    protected lateinit var mViewModel: BaseViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Show error message
        context?.let { observerContext ->
            mViewModel.errorMessage.observe(
                viewLifecycleOwner,
                Observer { message ->
                    if (message != null){
                        displayErrorMessage(observerContext, message)
                    } else {
                        displayErrorMessage(observerContext, getString(R.string.error_internal_server))
                    }
                }
            )
        }
    }

    // Fragments can override this to individually customize how they display error messages.
    protected open fun displayErrorMessage(context: Context, message: String) {
        /*val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(getString(R.string.dialog_error_title))
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.label_ok)) { dialog, which -> dialog.dismiss()}
        alertDialog.show()*/

        // In some fragments it may be more appropriate to show a toast instead, so an override of this method would look like:
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}