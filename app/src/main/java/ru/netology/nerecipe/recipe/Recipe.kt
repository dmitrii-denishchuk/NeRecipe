package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable
import ru.netology.nerecipe.data.repository.RecipeRepository

@Serializable
data class Recipe(
    val id: Long = RecipeRepository.NEW_RECIPE_ID,
    val isFavorite: Boolean = false,
    var steps: List<Step> = mutableListOf(Step(1000, "", "", "")),
    val title: String = "",
    val author: String = "Я",
    val category: String = "",
    val picture: String = ""
)