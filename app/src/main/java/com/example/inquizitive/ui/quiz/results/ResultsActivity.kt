package com.example.inquizitive.ui.quiz.results

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.inquizitive.R
import com.example.inquizitive.databinding.ActivityResultsBinding

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private val mResultsViewModel by lazy { ViewModelProvider(this)[ResultsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityResultsBinding?>(
            this,
            R.layout.activity_results
        ).apply {
            viewModel = mResultsViewModel
            lifecycleOwner = this@ResultsActivity
        }
    }
}