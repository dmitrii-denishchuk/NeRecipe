package ru.netology.nerecipe.db

import ru.netology.nerecipe.recipe.Recipe

internal fun RecipeEntity.toRecipe() = Recipe(
    id = id,
    isFavorite = isFavorite,
    title = title,
    author = author,
    category = category,
    picture = picture,
    steps = step
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