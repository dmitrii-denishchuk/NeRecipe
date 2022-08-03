package ru.netology.nerecipe.clickListeners

import ru.netology.nerecipe.recipe.Recipe

interface RecipeClickListeners {
    fun clickedFavorite(recipe: Recipe)
    fun clickedRemove(recipe: Recipe)
    fun clickedEdit(recipe: Recipe)
    fun clickedRecipe(recipe: Recipe)
}