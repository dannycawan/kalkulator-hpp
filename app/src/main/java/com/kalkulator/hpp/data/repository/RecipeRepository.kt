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
    fun searchByName(query: String): Flow<List<Recipe>> = recipeDao.searchByName(query)
    fun getByCategory(category: String): Flow<List<Recipe>> = recipeDao.getByCategory(category)
    fun getCount(): Flow<Int> = recipeDao.getCount()
    fun getAllCategories(): Flow<List<String>> = recipeDao.getAllCategories()
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

    /** Duplicate a recipe and all its ingredients */
    suspend fun duplicateRecipe(recipe: Recipe): Long {
        val newId = recipeDao.insert(recipe.copy(id = 0, name = "${recipe.name} (Copy)", createdAt = System.currentTimeMillis()))
        // ingredients will be copied by the caller using getIngredientsForRecipe
        return newId
    }
}
