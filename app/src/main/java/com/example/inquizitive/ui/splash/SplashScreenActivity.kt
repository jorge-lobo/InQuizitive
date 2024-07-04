package com.example.inquizitive.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.inquizitive.databinding.ActivitySplashScreenBinding
import com.example.inquizitive.ui.home.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val mSplashScreenViewModel by lazy { ViewModelProvider(this)[SplashScreenViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            mSplashScreenViewModel.uploadJsonToInternalStorage()

            delay(2000)
            startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
            finish()
        }
    }
}