package ru.netology.nerecipe.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nerecipe.data.RecipeDao
import ru.netology.nerecipe.db.toEntity
import ru.netology.nerecipe.db.toRecipe
import ru.netology.nerecipe.recipe.Recipe

class RecipeRepositoryImpl(private val dao: RecipeDao) : RecipeRepository {

    override val data = dao.getAll().map { entities ->
        entities.map { it.toRecipe() }
    }

    override fun favoriteById(id: Long) {
        dao.favoriteById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(recipe: Recipe) {
        dao.save(recipe.toEntity())
    }
}