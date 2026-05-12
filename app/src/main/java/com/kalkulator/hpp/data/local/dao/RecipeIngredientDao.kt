package com.kalkulator.hpp.data.local.dao

import androidx.room.*
import com.kalkulator.hpp.data.local.entity.RecipeIngredientCrossRef
import com.kalkulator.hpp.domain.model.IngredientWithQuantity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: RecipeIngredientCrossRef): Long

    @Delete
    suspend fun delete(crossRef: RecipeIngredientCrossRef)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)

    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT i.id, i.name, i.unit, i.pricePerUnit, i.supplier, ri.quantity, ri.id AS crossRefId
        FROM ingredients i
        INNER JOIN recipe_ingredients ri ON i.id = ri.ingredientId
        WHERE ri.recipeId = :recipeId
        ORDER BY i.name ASC
    """)
    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithQuantity>>
}
