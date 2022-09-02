package ru.netology.nerecipe.viewModel

import android.app.Application
import androidx.core.content.contentValuesOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.data.repository.RecipeRepository
import ru.netology.nerecipe.data.repository.RecipeRepositoryImpl
import ru.netology.nerecipe.db.AppDb
import ru.netology.nerecipe.recipe.Content
import ru.netology.nerecipe.recipe.Recipe

class RecipeViewModel(application: Application) : AndroidViewModel(application),
    RecipeClickListeners {

    private val repository: RecipeRepository = RecipeRepositoryImpl(
        dao = AppDb.getInstance(
            context = application
        ).recipeDao
    )

    val data get() = repository.data
    val currentRecipe = MutableLiveData<Recipe?>(null)
    val currentContentList = mutableListOf<Content>()
    var filteredRecipes = MutableLiveData<List<Recipe>>()
    var checkboxesState = booleanArrayOf()

    fun clickedSave(recipe: Recipe) {
        if (recipe.title.isBlank()) return
        val someRecipe = currentRecipe.value?.copy(
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture,
            content = recipe.content
        ) ?: Recipe(
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture,
            content = recipe.content
        )
        repository.save(someRecipe)
        currentRecipe.value = null
        currentContentList.clear()
    }

    fun clickedSaveCurrentContent(content: Content) {
        if (currentContentList.isEmpty()) {
            with(currentContentList) {
                add(content)
                add(Content(currentContentList.size, "", "", ""))
            }
        } else {
            with(currentContentList) {
                removeAt(currentContentList.size - 1)
                add(content.copy(id = currentContentList.size))
                add(Content(currentContentList.size, "", "", ""))
            }
        }
    }

    fun clickedRemoveContent(contentId: Int) {
        if (contentId == -1) currentContentList.removeAt(currentContentList.size - 1)
        else currentContentList.removeAt(contentId)
    }

    fun clickedSaveCurrentRecipe(recipe: Recipe) {
        currentRecipe.value = currentRecipe.value?.copy(
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture,
            content = currentContentList.toList()
        )
    }

    fun plusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.plus(recipes)
    }

    fun minusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.minus(recipes.toSet())
    }

    override fun clickedFavorite(recipe: Recipe) = repository.favoriteById(recipe.id)

    override fun clickedRemove(recipe: Recipe) = repository.removeById(recipe.id)

    override fun clickedEdit(recipe: Recipe) {
        currentRecipe.value = recipe
    }

    override fun clickedRecipe(recipe: Recipe) {
        currentRecipe.value = recipe
    }
}