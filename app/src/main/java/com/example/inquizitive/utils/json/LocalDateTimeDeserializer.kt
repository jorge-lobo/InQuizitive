package com.example.inquizitive.utils.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

internal class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    private val formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDateTime {
        return LocalDateTime.parse(
            json.asString,
            formatter
        )
    }
}