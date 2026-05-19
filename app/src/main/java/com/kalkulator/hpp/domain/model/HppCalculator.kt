/*
 * Tujuan: Kalkulasi HPP (Harga Pokok Produksi) — pure Kotlin, tanpa Android dependency
 * Caller: CalculatorViewModel.recalculate()
 * Dependensi: -
 * Main Functions: materialCost(), hpp(), suggestedPrice(), roundUpToNearest(), actualMarginPct()
 * Side Effects: -
 */
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
     * PENTING: Menggunakan MARGIN (% dari harga jual), BUKAN markup (% dari HPP).
     * Contoh margin 30%: harga = HPP / 0.7, bukan HPP × 1.3
     */
    fun suggestedPrice(hpp: Double, marginPct: Double): Double {
        require(marginPct in 0.0..100.0) { "Margin must be between 0 and 100" }
        return if (marginPct >= 100) Double.POSITIVE_INFINITY else hpp / (1 - marginPct / 100.0)
    }

    /**
     * Bulatkan harga ke atas ke kelipatan [step] terdekat (default 500 — standar IDR).
     * Contoh: 14.286 → 14.500
     */
    fun roundUpToNearest(price: Double, step: Double = 500.0): Double {
        if (price <= 0) return 0.0
        return kotlin.math.ceil(price / step) * step
    }

    /**
     * Hitung margin aktual (%) setelah pembulatan harga.
     * margin = (roundedPrice - hpp) / roundedPrice × 100
     */
    fun actualMarginPct(roundedPrice: Double, hpp: Double): Double {
        if (roundedPrice <= 0 || hpp <= 0) return 0.0
        return (roundedPrice - hpp) / roundedPrice * 100.0
    }
}
