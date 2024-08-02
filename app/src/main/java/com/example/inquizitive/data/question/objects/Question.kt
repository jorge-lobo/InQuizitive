package com.example.inquizitive.data.question.objects

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("category") var category: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("correctAnswer") var correctAnswer: String? = null,
    @SerializedName("incorrectAnswers") var incorrectAnswers: ArrayList<String> = arrayListOf(),
    @SerializedName("question") var questionText: QuestionText? = QuestionText(),
    @SerializedName("tags") var tags: ArrayList<String> = arrayListOf(),
    @SerializedName("type") var type: String? = null,
    @SerializedName("difficulty") var difficulty: String? = null,
    @SerializedName("regions") var regions: ArrayList<String> = arrayListOf(),
    @SerializedName("isNiche") var isNiche: Boolean? = null
)

data class QuestionText(
    @SerializedName("text") var text: String? = null
)