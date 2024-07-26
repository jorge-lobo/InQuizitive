package com.example.inquizitive.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun startActivity(context: Context, activityClass: Class<*>) {
        context.startActivity(Intent(context, activityClass))
    }

    fun formatNumberWithThousandSeparator(number: Int): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
        }
        return DecimalFormat("#,###", symbols).format(number)
    }
}