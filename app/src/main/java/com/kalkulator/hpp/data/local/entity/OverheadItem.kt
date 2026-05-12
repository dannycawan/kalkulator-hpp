package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Biaya overhead / lain-lain bulanan.
 * Contoh: Sewa Ruko, Listrik + Gas, Internet, Transport, dll.
 */
@Entity(tableName = "overhead_items")
data class OverheadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val monthlyCost: Double,
    val notes: String = ""
)
