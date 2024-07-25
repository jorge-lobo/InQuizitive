package com.example.inquizitive.data.result

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class ResultTypeAdapter : TypeAdapter<Result>() {
    override fun write(out: JsonWriter, result: Result) {
        out.beginObject()
        out.name("id").value(result.id)
        out.name("userId").value(result.userId)
        out.name("score").value(result.score)
        out.endObject()
    }

    override fun read(reader: JsonReader): Result {
        val result = Result()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> result.id = reader.nextInt()
                "userId" -> result.userId = reader.nextInt()
                "score" -> result.score = reader.nextInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return result
    }
}