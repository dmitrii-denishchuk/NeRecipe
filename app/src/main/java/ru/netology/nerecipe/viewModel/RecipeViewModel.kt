package ru.netology.nerecipe.viewModel

import android.app.Application
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
            content = recipe.content.filterNot { it.id < 0 },
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture
        ) ?: Recipe(
            content = recipe.content.filterNot { it.id < 0 },
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture
        )
        repository.save(someRecipe)
        currentRecipe.value = null
        currentContentList.clear()
    }

    fun clickedSaveContent(recipe: Recipe, content: Content) {
        currentContentList.add(
            Content(
                id = currentContentList.size,
                step = content.step,
                content = content.content,
                picture = content.picture
            )
        )
//        currentRecipe.value?.id?.let { convert(it) } ?.let {
//            Content(
//                id = it,
//                step = content.step,
//                content = content.content,
//                picture = content.picture
//            )} ?.let { currentContentList.add(it) }
        currentContentList.add(Content(0, "", "", ""))
        currentRecipe.value = currentRecipe.value?.copy(
            title = recipe.title,
            category = recipe.category,
            picture = recipe.picture,
            content = currentContentList
        )
    }

    fun plusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.plus(recipes)
    }

    fun minusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.minus(recipes)
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

fun convert(id: Long): Double {
    val newId = id.toString()
    return with(newId) {
        plus(".0")
        toDouble()
        +0.1
    }
}
