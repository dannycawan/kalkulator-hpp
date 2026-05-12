package com.kalkulator.hpp.di

import android.app.Application
import androidx.room.Room
import com.kalkulator.hpp.data.local.AppDatabase
import com.kalkulator.hpp.data.repository.IngredientRepository
import com.kalkulator.hpp.data.repository.RecipeRepository
import com.kalkulator.hpp.data.repository.CalculationRepository
import com.kalkulator.hpp.util.CsvUtil
import com.kalkulator.hpp.util.PdfUtil

/**
 * Simple manual DI container – creates singletons needed across the app.
 */
class AppModule : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var ingredientRepository: IngredientRepository
        private set
    lateinit var recipeRepository: RecipeRepository
        private set
    lateinit var calculationRepository: CalculationRepository
        private set
    lateinit var csvUtil: CsvUtil
        private set
    lateinit var pdfUtil: PdfUtil
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "kalkulator_hpp.db"
        ).fallbackToDestructiveMigration()
            .build()

        ingredientRepository = IngredientRepository(database.ingredientDao())
        recipeRepository = RecipeRepository(database.recipeDao(), database.recipeIngredientDao())
        calculationRepository = CalculationRepository(database.calculationDao())

        csvUtil = CsvUtil()
        pdfUtil = PdfUtil(this)
    }
}
