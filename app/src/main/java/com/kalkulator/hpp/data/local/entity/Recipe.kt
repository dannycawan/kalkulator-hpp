package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val laborCost: Double = 0.0,
    val overheadCost: Double = 0.0,
    val yield: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
