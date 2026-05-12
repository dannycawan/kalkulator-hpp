package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "",          // Makanan, Minuman, Snack, Kue, dll
    val photoUri: String? = null,       // URI foto dari galeri
    val notes: String = "",             // catatan tambahan
    val laborCost: Double = 0.0,
    val overheadCost: Double = 0.0,
    val yield: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
