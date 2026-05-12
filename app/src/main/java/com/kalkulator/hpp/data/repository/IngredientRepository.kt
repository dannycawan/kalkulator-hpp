package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.IngredientDao
import com.kalkulator.hpp.data.local.entity.Ingredient
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val dao: IngredientDao) {
    fun getAll(): Flow<List<Ingredient>> = dao.getAll()
    suspend fun insert(ingredient: Ingredient) = dao.insert(ingredient)
    suspend fun update(ingredient: Ingredient) = dao.update(ingredient)
    suspend fun delete(ingredient: Ingredient) = dao.delete(ingredient)
}
