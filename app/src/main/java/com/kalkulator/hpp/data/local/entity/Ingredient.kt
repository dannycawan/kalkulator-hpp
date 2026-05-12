package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,               // e.g., "gram", "ml"
    val pricePerUnit: Double,       // price per unit in local currency
    val supplier: String? = null
)
