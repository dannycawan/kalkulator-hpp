package com.kalkulator.hpp.domain.model

/**
 * Pure Kotlin calculation helper – no Android dependencies.
 */
object HppCalculator {

    /**
     * Compute total material cost from a list of (price per unit, quantity) pairs.
     */
    fun materialCost(ingredients: List<Pair<Double, Double>>): Double {
        return ingredients.sumOf { (pricePerUnit, qty) -> pricePerUnit * qty }
    }

    /**
     * Full HPP calculation.
     */
    fun hpp(materialCost: Double, laborCost: Double, overheadCost: Double, yield: Int): Double {
        require(yield > 0) { "Yield must be > 0" }
        return (materialCost + laborCost + overheadCost) / yield
    }

    /**
     * Suggested selling price based on desired margin percentage.
     */
    fun suggestedPrice(hpp: Double, marginPct: Double): Double {
        require(marginPct in 0.0..100.0) { "Margin must be between 0 and 100" }
        return if (marginPct >= 100) Double.POSITIVE_INFINITY else hpp / (1 - marginPct / 100.0)
    }
}
