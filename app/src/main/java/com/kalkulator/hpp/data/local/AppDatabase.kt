package com.kalkulator.hpp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kalkulator.hpp.data.local.dao.IngredientDao
import com.kalkulator.hpp.data.local.dao.RecipeDao
import com.kalkulator.hpp.data.local.dao.RecipeIngredientDao
import com.kalkulator.hpp.data.local.dao.CalculationDao
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.data.local.entity.RecipeIngredientCrossRef
import com.kalkulator.hpp.data.local.entity.CalculationResult

@Database(
    entities = [Ingredient::class, Recipe::class, RecipeIngredientCrossRef::class, CalculationResult::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun calculationDao(): CalculationDao
}
