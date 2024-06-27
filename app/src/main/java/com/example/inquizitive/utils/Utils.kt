package com.example.inquizitive.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast

object Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun startActivity(context: Context, activityClass: Class<*>) {
        context.startActivity(Intent(context, activityClass))
    }
}