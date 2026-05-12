package com.kalkulator.hpp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,               // e.g., "gram", "ml", "pcs", "kg", "liter", "sdm"
    val pricePerUnit: Double,       // price per unit in local currency
    val stock: Double = 0.0,        // stok saat ini
    val supplier: String? = null,
    val notes: String = ""          // keterangan
)
