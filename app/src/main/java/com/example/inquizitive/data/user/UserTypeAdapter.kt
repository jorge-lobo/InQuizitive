package com.example.inquizitive.data.user

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class UserTypeAdapter : TypeAdapter<User>() {
    override fun write(out: JsonWriter, user: User) {
        out.beginObject()
        out.name("id").value(user.id)
        out.name("username").value(user.username)
        out.name("password").value(user.password)
        out.name("avatar").value(user.avatar)
        out.name("quizzesPlayed").value(user.quizzesPlayed)
        out.name("totalPoints").value(user.totalPoints)
        out.name("bestResult").value(user.bestResult)
        out.name("totalCoins").value(user.totalCoins)
        out.name("actualCoins").value(user.actualCoins)
        out.name("spentCoins").value(user.spentCoins)
        out.name("totalTime").value(user.totalTime)
        out.name("spentTime").value(user.spentTime)
        out.name("totalAnswers").value(user.totalAnswers)
        out.name("correctAnswers").value(user.correctAnswers)
        out.endObject()
    }

    override fun read(reader: JsonReader): User {
        val user = User()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> user.id = reader.nextInt()
                "username" -> user.username = reader.nextString()
                "password" -> user.password = reader.nextString()
                "avatar" -> user.avatar = reader.nextString()
                "quizzesPlayed" -> user.quizzesPlayed = reader.nextInt()
                "totalPoints" -> user.totalPoints = reader.nextInt()
                "bestResult" -> user.bestResult = reader.nextInt()
                "totalCoins" -> user.totalCoins = reader.nextInt()
                "actualCoins" -> user.actualCoins = reader.nextInt()
                "spentCoins" -> user.spentCoins = reader.nextInt()
                "totalTime" -> user.totalTime = reader.nextInt()
                "spentTime" -> user.spentTime = reader.nextInt()
                "totalAnswers" -> user.totalAnswers = reader.nextInt()
                "correctAnswers" -> user.correctAnswers = reader.nextInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return user
    }
}