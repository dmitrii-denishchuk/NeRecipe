package ru.netology.nerecipe.db

import ru.netology.nerecipe.recipe.Recipe

internal fun RecipeEntity.toRecipe() = Recipe(
    id = id,
    isFavorite = isFavorite,
    steps = step,
    title = title,
    author = author,
    category = category,
    picture = picture
)

internal fun Recipe.toEntity() = RecipeEntity(
    id = id,
    isFavorite = isFavorite,
    step = steps,
    title = title,
    author = author,
    category = category,
    picture = picture
)