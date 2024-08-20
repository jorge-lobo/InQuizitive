package com.example.inquizitive.data.question

import com.example.inquizitive.data.common.ResultWrapper
import com.example.inquizitive.data.question.objects.Question

class IQuestionDataSource {
    //Interfaces required for all objects in this data source
    interface Common {

    }

    //Interfaces specific to remote data source
    interface Remote : Common {
        suspend fun getQuestions() : ResultWrapper<List<Question>>
    }

    //Interfaces specific to local data source
    interface Local : Common {

    }

    //Interfaces specific to the main repository object. (cache operations, for example). Inherits both Remote and Local as those are accessed by use cases via the repository.
    interface Main : Remote, Local {
        suspend fun getCachedQuestion(questionId: String) : ResultWrapper<Question?>
    }
}