package com.example.inquizitive.data.user

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("username") var username: String? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("avatar") var avatar: String? = null,
    @SerializedName("quizzesPlayed") var quizzesPlayed: Int? = null,
    @SerializedName("totalPoints") var totalPoints: Int? = null,
    @SerializedName("bestResult") var bestResult: Int? = null,
    @SerializedName("totalCoins") var totalCoins: Int? = null,
    @SerializedName("actualCoins") var actualCoins: Int? = null,
    @SerializedName("spentCoins") var spentCoins: Int? = null,
    @SerializedName("totalTime") var totalTime: Int? = null,
    @SerializedName("spentTime") var spentTime: Int? = null,
    @SerializedName("totalAnswers") var totalAnswers: Int? = null,
    @SerializedName("correctAnswers") var correctAnswers: Int? = null
)