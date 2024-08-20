package com.example.inquizitive.data.question.remote

import com.example.inquizitive.data.common.HTTPClient
import com.example.inquizitive.data.common.ResultWrapper
import com.example.inquizitive.data.question.IQuestionDataSource
import com.example.inquizitive.data.question.objects.Question
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object QuestionRemoteDataSource : IQuestionDataSource.Remote {

    private val questionAPI = HTTPClient(QuestionAPI::class.java).get()
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getQuestions(): ResultWrapper<List<Question>> {
        return ResultWrapper.safeApiCall(dispatcher) {
            questionAPI.getQuestions()
        }
    }
}