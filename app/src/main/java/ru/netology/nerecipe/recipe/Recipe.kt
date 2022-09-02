package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable
import ru.netology.nerecipe.data.repository.RecipeRepository

@Serializable
data class Recipe(
    val id: Long = RecipeRepository.NEW_RECIPE_ID,
    val isFavorite: Boolean = false,
    var content: List<Content> = mutableListOf(Content(0, "", "", "")),
    val title: String = "",
    val author: String = "Я",
    val category: String = "",
    val picture: String = ""
)