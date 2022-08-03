package ru.netology.nerecipe.data.repository

import androidx.lifecycle.LiveData
import ru.netology.nerecipe.recipe.Content

interface ContentRepository {
    val contentData: LiveData<List<Content>>

    fun removeById(id: Long)
    fun save(content: Content)

    companion object {
        const val NEW_CONTENT_ID = 0
    }
}