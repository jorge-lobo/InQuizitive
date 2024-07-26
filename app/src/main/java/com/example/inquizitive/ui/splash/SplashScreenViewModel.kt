package com.example.inquizitive.ui.splash

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.inquizitive.ui.common.BaseViewModel
import com.example.inquizitive.utils.AppConstants
import com.example.inquizitive.utils.Utils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SplashScreenViewModel(application: Application) : BaseViewModel(application) {

    private val prefs =
        application.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)

    fun uploadJsonToInternalStorage() {
        viewModelScope.launch {
            uploadFile("users.json")
            uploadFile("results.json")
        }
    }

    private fun uploadFile(filename: String) {
        try {
            val outputFile = File(getApplication<Application>().filesDir, filename)
            if (!outputFile.exists()) {
                val assetManager = getApplication<Application>().assets
                val inputStream = assetManager.open(filename)
                val outputStream = FileOutputStream(outputFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            onError("Error uploading file: ${e.message}", null)
        }
    }

    fun checkRememberMeStatus() {
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>()
                    .getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)
                val isRememberMe = sharedPreferences.getBoolean(AppConstants.KEY_REMEMBER_ME, false)

                if (!isRememberMe) {
                    clearSharedPreferences()
                }
            } catch (e: Exception) {
                onError("Error checking remember me status: ${e.message}", null)
            }
        }
    }

    private fun clearSharedPreferences() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            onError("Error clearing shared preferences: ${e.message}", null)
        }
    }

    override fun onError(message: String?, validationErrors: Map<String, ArrayList<String>>?) {
        isLoading.value = false
        isRefreshing.value = false
        message?.let {
            Log.e("SplashScreenViewModel", it)
            Utils.showToast(getApplication(), it)
        }
    }
}