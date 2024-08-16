package com.example.inquizitive.data.result

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class ResultRepository(private val context: Context) {

    private val results = mutableListOf<Result>()
    private val gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .registerTypeAdapter(Result::class.java, ResultTypeAdapter())
        .create()

    init {
        loadResultsFromInternalStorage()
    }

    private fun loadResultsFromInternalStorage() {
        val jsonString = loadResultFileContent()
        if (jsonString.isNotEmpty()) {
            val resultsType: Type = object : TypeToken<List<Result>>() {}.type
            try {
                results.addAll(gson.fromJson(jsonString, resultsType))
            } catch (e: Exception) {
                val resultsObject = JsonParser.parseString(jsonString).asJsonObject
                val resultsArray = resultsObject.getAsJsonArray("results")
                results.addAll(gson.fromJson(resultsArray, resultsType))
            }
        }
    }

    private fun loadResultFileContent(): String {
        val file = File(context.filesDir, "results.json")
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    fun getResults(): List<Result> {
        return results.toList()
    }

    fun getNextResultId(): Int {
        return results.size + 1
    }

    fun saveResult(newResult: Result) {
        val resultIndex = results.indexOfFirst { it.id == newResult.id }
        if (resultIndex != -1) {
            results[resultIndex] = newResult
        } else {
            results.add(newResult)
        }

        saveResultsToJson(results)
    }

    private fun saveResultsToJson(results: List<Result>) {
        val jsonString = gson.toJson(results)
        val file = File(context.filesDir, "results.json")
        file.writeText(jsonString)
    }
}