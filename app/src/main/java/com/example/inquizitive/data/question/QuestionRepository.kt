package com.example.inquizitive.data.question

import com.example.inquizitive.data.common.ResultWrapper
import com.example.inquizitive.data.question.objects.Question
import com.example.inquizitive.data.question.remote.QuestionRemoteDataSource

object QuestionRepository : IQuestionDataSource.Main {

    private var cachedQuestionResponse = mutableListOf<Question>()

    override suspend fun getQuestions(): ResultWrapper<List<Question>> {
        val result = QuestionRemoteDataSource.getQuestions()

        result.result?.let {
            cachedQuestionResponse.addAll(it)
        }
        return result
    }

    override suspend fun getCachedQuestion(questionId: String): ResultWrapper<Question?> {
        for (item in cachedQuestionResponse) {
            if (item.id == questionId) {
                return ResultWrapper(item, null)
            }
        }
        return ResultWrapper(null, null)
    }
}