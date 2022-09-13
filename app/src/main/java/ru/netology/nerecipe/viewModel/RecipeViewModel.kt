package ru.netology.nerecipe.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.data.repository.RecipeRepository
import ru.netology.nerecipe.data.repository.RecipeRepositoryImpl
import ru.netology.nerecipe.db.AppDb
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.recipe.Step

const val EMPTY_STEP_ID = 1000

class RecipeViewModel(application: Application) : AndroidViewModel(application),
    RecipeClickListeners {

    private val repository: RecipeRepository = RecipeRepositoryImpl(
        dao = AppDb.getInstance(
            context = application
        ).recipeDao
    )

    val data get() = repository.data
    val currentRecipe = MutableLiveData<Recipe?>(null)
    val currentStepsList = mutableListOf<Step>()
    var filteredRecipes = MutableLiveData<List<Recipe>>()
    var checkboxesState = booleanArrayOf()

    fun clickedAddToDb() {
        val lastStep = currentRecipe.value?.steps?.first { it.id == EMPTY_STEP_ID } ?: return
        clickedAddOrRemoveStep(lastStep)
        val someRecipe = currentRecipe.value?.copy(
            steps = currentStepsList.filterNot { it.title == "" }.sortedBy { it.id }
        ) ?: Recipe(
            steps = currentStepsList.filterNot { it.title == "" }.sortedBy { it.id }
        )
        repository.save(someRecipe)
        currentRecipe.value = null
        currentStepsList.clear()
    }

    fun clickedSaveCurrentStep(step: Step) {
        val currentStep = currentStepsList.first { it.id == step.id }
        with(currentStepsList) {
            remove(currentStep)
            add(step)
        }
        currentRecipe.value = currentRecipe.value?.copy(
            steps = currentStepsList.toList().sortedBy { it.id }
        )
    }

    fun clickedAddOrRemoveStep(step: Step) {
        if (step.id == EMPTY_STEP_ID) {
            with(currentStepsList) {
                removeIf { it.id == step.id }
                add((step.copy(id = currentStepsList.size)))
                add(Step())
            }
        } else currentStepsList.removeIf { it.id == step.id }
        currentRecipe.value = currentRecipe.value?.copy(
            steps = currentStepsList.toList().sortedBy { it.id }
        )
    }

    fun clickedSaveCurrentRecipe(recipe: Recipe) {
        when (recipe.id) {
            -1L -> {
                currentRecipe.value = currentRecipe.value?.copy(
                    title = recipe.title
                )
            }
            -2L -> {
                currentRecipe.value = currentRecipe.value?.copy(
                    category = recipe.category
                )
            }
            -3L -> {
                currentRecipe.value = currentRecipe.value?.copy(
                    picture = recipe.picture
                )
            }
        }
    }

    fun plusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.plus(recipes)
    }

    fun minusRecipe(recipes: List<Recipe>) {
        filteredRecipes.value = filteredRecipes.value?.minus(recipes.toSet())
    }

    override fun clickedFavorite(recipe: Recipe) = repository.favoriteById(recipe.id)

    override fun clickedRemove(recipe: Recipe) = repository.removeById(recipe.id)

    override fun clickedNewOrEdit(recipe: Recipe) {
        currentStepsList.addAll(recipe.steps)
        if (data.value?.contains(recipe) == true)
            currentStepsList.add(Step())
        currentRecipe.value = recipe.copy(steps = currentStepsList.toList())
    }

    override fun clickedRecipe(recipe: Recipe) {
        currentRecipe.value = recipe
    }
}