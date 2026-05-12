package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.RecipeDao
import com.kalkulator.hpp.data.local.dao.RecipeIngredientDao
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.data.local.entity.RecipeIngredientCrossRef
import com.kalkulator.hpp.domain.model.IngredientWithQuantity
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {
    fun getAll(): Flow<List<Recipe>> = recipeDao.getAll()
    suspend fun getById(id: Long): Recipe? = recipeDao.getById(id)
    suspend fun insert(recipe: Recipe): Long = recipeDao.insert(recipe)
    suspend fun update(recipe: Recipe) = recipeDao.update(recipe)
    suspend fun delete(recipe: Recipe) = recipeDao.delete(recipe)

    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithQuantity>> =
        recipeIngredientDao.getIngredientsForRecipe(recipeId)

    suspend fun addIngredientToRecipe(recipeId: Long, ingredientId: Long, quantity: Double) =
        recipeIngredientDao.insert(RecipeIngredientCrossRef(recipeId = recipeId, ingredientId = ingredientId, quantity = quantity))

    suspend fun removeIngredientFromRecipe(id: Long) =
        recipeIngredientDao.deleteById(id)
}
