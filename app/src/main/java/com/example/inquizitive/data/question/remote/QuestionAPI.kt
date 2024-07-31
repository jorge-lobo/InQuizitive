package com.example.inquizitive.data.question.remote

import com.example.inquizitive.data.question.objects.QuestionListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuestionAPI {
    @GET("questions")
    suspend fun getQuestions(): QuestionListResponse
}