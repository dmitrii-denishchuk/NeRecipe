package ru.netology.nerecipe.db

import ru.netology.nerecipe.recipe.Recipe

internal fun RecipeEntity.toRecipe() = Recipe(
    id = id,
    isFavorite = isFavorite,
    content = content,
    title = title,
    author = author,
    category = category,
    picture = picture
)

internal fun Recipe.toEntity() = RecipeEntity(
    id = id,
    isFavorite = isFavorite,
    content = content,
    title = title,
    author = author,
    category = category,
    picture = picture
)