package com.kalkulator.hpp.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kalkulator.hpp.data.local.AppDatabase
import com.kalkulator.hpp.data.repository.*
import com.kalkulator.hpp.util.CsvUtil
import com.kalkulator.hpp.util.PdfUtil

/** DataStore for app settings */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system_theme")
    val DAILY_PRODUCTION = intPreferencesKey("daily_production")
    val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
}

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
    lateinit var equipmentRepository: EquipmentRepository
        private set
    lateinit var overheadRepository: OverheadRepository
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
        equipmentRepository = EquipmentRepository(database.equipmentDao())
        overheadRepository = OverheadRepository(database.overheadDao())

        csvUtil = CsvUtil()
        pdfUtil = PdfUtil(this)
    }
}
