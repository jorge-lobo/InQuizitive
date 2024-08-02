package com.example.inquizitive.data.question.remote

import com.example.inquizitive.data.question.objects.Question
import retrofit2.http.GET

interface QuestionAPI {
    @GET("questions")
    suspend fun getQuestions(): List<Question>
}