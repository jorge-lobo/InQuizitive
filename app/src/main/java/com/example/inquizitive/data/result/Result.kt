package com.example.inquizitive.data.result

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("userId") var userId: Int? = null,
    @SerializedName("score") var score: Int? = null
)