package com.example.inquizitive.utils.json

import android.content.Context
import java.io.IOException

class JSONLoader(private val context: Context) {

    fun loadJSONFromAsset(fileName: String): String {
        val json: String = try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
        return json
    }
}