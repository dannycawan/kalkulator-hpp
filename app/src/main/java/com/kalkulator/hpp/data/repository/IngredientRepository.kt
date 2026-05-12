package com.kalkulator.hpp.data.repository

import com.kalkulator.hpp.data.local.dao.IngredientDao
import com.kalkulator.hpp.data.local.entity.Ingredient
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val dao: IngredientDao) {
    fun getAll(): Flow<List<Ingredient>> = dao.getAll()
    fun searchByName(query: String): Flow<List<Ingredient>> = dao.searchByName(query)
    fun getCount(): Flow<Int> = dao.getCount()
    suspend fun getById(id: Long): Ingredient? = dao.getById(id)
    suspend fun insert(ingredient: Ingredient) = dao.insert(ingredient)
    suspend fun insertAll(ingredients: List<Ingredient>) = dao.insertAll(ingredients)
    suspend fun update(ingredient: Ingredient) = dao.update(ingredient)
    suspend fun delete(ingredient: Ingredient) = dao.delete(ingredient)
    suspend fun updateStock(id: Long, stock: Double) = dao.updateStock(id, stock)
}
