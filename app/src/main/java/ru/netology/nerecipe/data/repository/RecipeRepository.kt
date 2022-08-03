package ru.netology.nerecipe.data.repository

import androidx.lifecycle.LiveData
import ru.netology.nerecipe.recipe.Content
import ru.netology.nerecipe.recipe.Recipe

interface RecipeRepository {
    val data: LiveData<List<Recipe>>

    fun favoriteById(id: Long)
    fun removeById(id: Long)
    fun save(recipe: Recipe)

    companion object {
        const val NEW_RECIPE_ID = 0L
    }
}