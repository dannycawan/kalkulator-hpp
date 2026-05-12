package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Alat & Peralatan – untuk menghitung biaya depresiasi/penyusutan.
 * Depresiasi per bulan = (purchasePrice - residualValue) / usefulLifeMonths
 */
@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchasePrice: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val usefulLifeMonths: Int,      // umur ekonomis dalam bulan
    val residualValue: Double = 0.0, // nilai sisa
    val notes: String = ""
) {
    /** Penyusutan per bulan */
    @get:Ignore
    val monthlyDepreciation: Double
        get() = if (usefulLifeMonths > 0) (purchasePrice - residualValue) / usefulLifeMonths else 0.0
}
