package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_results")
data class CalculationResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeName: String,
    val category: String = "",
    val totalMaterialCost: Double,
    val laborCost: Double,
    val depreciationCost: Double = 0.0,
    val overheadCost: Double,
    val yield: Int,
    val hppPerUnit: Double,
    val marginPct: Double,
    val suggestedPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)
