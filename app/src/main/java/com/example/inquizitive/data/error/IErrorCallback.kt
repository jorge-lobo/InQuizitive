package com.example.inquizitive.data.error

interface IErrorCallback {
    //fun onHttpError(message : String?, validationErrors: Map<String, ArrayList<String>>? = null)
    fun onGenericError(message: String?, validationErrors: Map<String, ArrayList<String>>?)
    fun onTimeout()
    fun onNetworkError()
    fun onSessionExpired()
}