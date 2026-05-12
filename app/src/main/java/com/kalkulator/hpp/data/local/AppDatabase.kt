package com.kalkulator.hpp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kalkulator.hpp.data.local.dao.*
import com.kalkulator.hpp.data.local.entity.*

@Database(
    entities = [
        Ingredient::class,
        Recipe::class,
        RecipeIngredientCrossRef::class,
        CalculationResult::class,
        Equipment::class,
        OverheadItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun calculationDao(): CalculationDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun overheadDao(): OverheadDao
}
