package com.example.inquizitive.data.question.objects

import com.google.gson.annotations.SerializedName

data class QuestionListResponse (
    @SerializedName("results") val results : List<Question>,
    @SerializedName("count") val count : Int
)