package com.example.inquizitive.data.common.objects

public class ErrorResponse(
    public val message: String?,
    public val tokenExpired: Boolean? = null,
    public val errors: Map<String, ArrayList<String>>? = null
) {
}