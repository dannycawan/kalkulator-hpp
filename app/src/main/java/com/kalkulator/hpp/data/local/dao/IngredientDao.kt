package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<Ingredient>)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAll(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: Long): Ingredient?

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<Ingredient>>

    @Query("UPDATE ingredients SET stock = :stock WHERE id = :id")
    suspend fun updateStock(id: Long, stock: Double)

    @Query("SELECT COUNT(*) FROM ingredients")
    fun getCount(): Flow<Int>
}
