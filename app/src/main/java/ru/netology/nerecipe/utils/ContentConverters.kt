package ru.netology.nerecipe.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nerecipe.recipe.Step

class ContentConverters {

    @TypeConverter
    fun outContentList(value: List<Step>): String {
        val type = object : TypeToken<List<Step>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    fun inContentList(value: String): List<Step> {
        val type = object : TypeToken<List<Step>>() {}.type
        return Gson().fromJson(value, type)
    }
}