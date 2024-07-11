package com.example.inquizitive.data.user

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.inquizitive.utils.AppConstants
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class UserRepository(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(AppConstants.PREFS_KEY, Context.MODE_PRIVATE)
    private val users = mutableListOf<User>()
    private val gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .registerTypeAdapter(User::class.java, UserTypeAdapter())
        .create()

    init {
        loadUsersFromInternalStorage()
    }

    private fun loadUsersFromInternalStorage() {
        val jsonString = loadUserFileContent()
        if (jsonString.isNotEmpty()) {
            val usersType: Type = object : TypeToken<List<User>>() {}.type
            try {
                users.addAll(gson.fromJson(jsonString, usersType))
            } catch (e: Exception) {
                val usersObject = JsonParser.parseString(jsonString).asJsonObject
                val usersArray = usersObject.getAsJsonArray("users")
                users.addAll(gson.fromJson(usersArray, usersType))
            }
        }
    }

    private fun loadUserFileContent(): String {
        val file = File(context.filesDir, "users.json")
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    fun getUsers(): List<User> {
        return users.toList()
    }

    fun getNextUserId(): Int {
        return users.size + 1
    }

    fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }?.copy()
    }

    fun getUserById(userId: Int): User? {
        return users.find { it.id == userId }?.copy()
    }

    fun saveCurrentUserId(userId: Int) {
        sharedPreferences.edit {
            putInt(AppConstants.KEY_CURRENT_USER_ID, userId)
            apply()
        }
    }

    private fun getCurrentUserId(): Int {
        return sharedPreferences.getInt(AppConstants.KEY_CURRENT_USER_ID, -1)
    }

    fun getCurrentUser(): User? {
        val currentUserId = getCurrentUserId()
        return users.find { it.id == currentUserId }
    }

    fun updateUserAvatar(userId: Int, newAvatar: String) {
        val user = getUserById(userId)
        user?.avatar = newAvatar
        saveUser(user!!)
    }

    fun saveUser(updatedUser: User) {
        val userIndex = users.indexOfFirst { it.id == updatedUser.id }
        if (userIndex != -1) {
            users[userIndex] = updatedUser
        } else {
            users.add(updatedUser)
        }

        saveUsersToJson(users)
    }

    private fun saveUsersToJson(users: List<User>) {
        val jsonString = gson.toJson(users)
        val file = File(context.filesDir, "users.json")
        file.writeText(jsonString)
    }
}