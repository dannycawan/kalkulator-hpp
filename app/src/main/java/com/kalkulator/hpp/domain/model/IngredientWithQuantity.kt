package com.kalkulator.hpp.domain.model

/**
 * POJO returned by Room JOIN query – ingredient data combined with recipe quantity.
 */
data class IngredientWithQuantity(
    val id: Long,
    val name: String,
    val unit: String,
    val pricePerUnit: Double,
    val supplier: String?,
    val quantity: Double,
    val crossRefId: Long
)
