package ru.netology.nerecipe.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nerecipe.recipe.Content

class ContentConverters {

    @TypeConverter
    fun outContentList(value: List<Content>): String {
        val type = object : TypeToken<List<Content>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    fun inContentList(value: String): List<Content> {
        val type = object : TypeToken<List<Content>>() {}.type
        return Gson().fromJson(value, type)
    }
}