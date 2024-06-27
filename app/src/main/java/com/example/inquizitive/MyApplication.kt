package com.example.inquizitive

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen

class MyApplication : Application() {

    companion object {
        private lateinit var sInstance: MyApplication
        //var database: AppDataBase? = null

        fun getAppContext(): Context {
            return sInstance.applicationContext
        }

        var userPermission = ""

        const val BASE_URL = "https://the-trivia-api.com/v2/"
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        AndroidThreeTen.init(this);
    }
}