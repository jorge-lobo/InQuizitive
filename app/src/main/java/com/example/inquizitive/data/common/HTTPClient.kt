package com.example.inquizitive.data.common

import com.example.inquizitive.MyApplication
import com.example.inquizitive.utils.json.JSONParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

class HTTPClient<T>(service: Class<T>) {

    companion object {
        private const val HEADER_PARAMETER_AUTHORIZATION = "Authorization"
        private const val HEADER_PARAMETER_LANG = "Accept-Language"
        private const val HEADER_PARAMETER_ACCEPT = "Accept"
        private const val HEADER_PARAMETER_CLIENT = "client"
        private const val CALL_TIME_OUT = 10

        var token: String? = null

        //        fun getToken() {
//
//            val loginResponse: ResultWrapper<LoginResponse> = runBlocking {
//                GetLoginDetailsUseCase().invoke()
//            }
//
//            loginResponse.result?.let {
//                token = it.token
//            }
//        }

    }

    var client: T?

    init {
        val httpClientBuilder = OkHttpClient.Builder()

        val defaultInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            request.addHeader(HEADER_PARAMETER_ACCEPT, "application/json")
            request.addHeader(HEADER_PARAMETER_LANG, Locale.getDefault().toString())
            request.addHeader(HEADER_PARAMETER_CLIENT, "mobile")
            chain.proceed(request.build())
        }
        httpClientBuilder.addInterceptor(defaultInterceptor)

        //        if (token == null){
//            getToken()
//        }

        /*if (token != null) {
            val tokenInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                request.addHeader(HEADER_PARAMETER_AUTHORIZATION, "Bearer $token")
                chain.proceed(request.build())
            }
            httpClientBuilder.addInterceptor(tokenInterceptor)
        }*/

        /*if (BuildConfig.DEBUG) {
            //Debugging interceptor should always be the last interceptor to be registered, so it can log all data in the request
            val httpDebuggingInterceptor = HttpLoggingInterceptor()
            httpDebuggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClientBuilder.addInterceptor(httpDebuggingInterceptor)
        }*/

        httpClientBuilder.connectTimeout(CALL_TIME_OUT.toLong(), TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(CALL_TIME_OUT.toLong(), TimeUnit.SECONDS)

        val httpClient = httpClientBuilder.build()
        val gson = JSONParser.getGson()

        val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(MyApplication.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        client = retrofit.create(service)

    }

    fun get(): T {
        return client!!
    }
}