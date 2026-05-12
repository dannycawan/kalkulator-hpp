package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): Recipe?

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<Recipe>>

    @Query("SELECT COUNT(*) FROM recipes")
    fun getCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM recipes WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}
