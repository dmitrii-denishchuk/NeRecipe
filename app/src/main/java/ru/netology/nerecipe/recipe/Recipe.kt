package ru.netology.nerecipe.recipe

import io.github.serpro69.kfaker.Faker
import kotlinx.serialization.Serializable
import ru.netology.nerecipe.data.repository.RecipeRepository

@Serializable
data class Recipe(
    val id: Long = RecipeRepository.NEW_RECIPE_ID,
    val isFavorite: Boolean = false,
    val title: String = "",
    val author: String = Faker().name.name(),
    val category: String = "",
    val picture: String = "",
    var steps: List<Step> = mutableListOf(Step(1000, "", "", ""))
)