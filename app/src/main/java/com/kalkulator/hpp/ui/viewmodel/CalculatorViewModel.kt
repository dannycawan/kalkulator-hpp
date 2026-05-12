package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.kalkulator.hpp.data.repository.CalculationRepository
import com.kalkulator.hpp.domain.model.HppCalculator
import com.kalkulator.hpp.domain.model.IngredientWithQuantity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Preset merchant platforms with default commission percentages.
 */
data class MerchantPlatform(
    val name: String,
    val emoji: String,
    val defaultFeePct: Double,     // komisi default (%)
    val hasPackagingFee: Boolean = false,
    val packagingFeeDefault: Double = 0.0  // biaya packaging default (Rp)
)

/** Comparison result for each platform */
data class PlatformComparison(
    val platform: MerchantPlatform,
    val feePct: Double,
    val feeAmount: Double,
    val packagingFee: Double,
    val netRevenue: Double,
    val profit: Double,
    val profitPct: Double,
    val isLoss: Boolean
)

object MerchantPlatforms {
    val platforms = listOf(
        MerchantPlatform("Langsung", "🏪", 0.0),
        MerchantPlatform("GoFood", "🟢", 20.0, hasPackagingFee = true, packagingFeeDefault = 1000.0),
        MerchantPlatform("GrabFood", "🟩", 20.0, hasPackagingFee = true, packagingFeeDefault = 1000.0),
        MerchantPlatform("ShopeeFood", "🟠", 15.0, hasPackagingFee = true, packagingFeeDefault = 500.0),
        MerchantPlatform("Tokopedia", "🟢", 5.5),
        MerchantPlatform("Shopee", "🟠", 6.5),
        MerchantPlatform("Custom", "⚙️", 0.0)
    )
}

data class CalculatorUiState(
    val recipeName: String = "",
    val category: String = "",
    val ingredients: List<IngredientWithQuantity> = emptyList(),
    val laborCost: Double = 0.0,
    val depreciationCost: Double = 0.0,
    val overheadCost: Double = 0.0,
    val yield: Int = 1,
    val marginPct: Double = 30.0,
    val totalMaterialCost: Double = 0.0,
    val hppPerUnit: Double = 0.0,
    val suggestedPrice: Double = 0.0,
    // Multi-margin recommendations
    val price30: Double = 0.0,
    val price40: Double = 0.0,
    val price50: Double = 0.0,
    // BEP
    val bepUnits: Int = 0,
    // Promo simulation
    val discountPct: Double = 0.0,
    val platformFeePct: Double = 0.0,
    val packagingFee: Double = 0.0,
    val priceAfterDiscount: Double = 0.0,
    val priceAfterFee: Double = 0.0,
    val profitAfterPromo: Double = 0.0,
    // Selected merchant
    val selectedPlatformIndex: Int = 0, // index into MerchantPlatforms.platforms
    // Platform comparison table
    val platformComparisons: List<PlatformComparison> = emptyList(),
    // State
    val saved: Boolean = false
)

class CalculatorViewModel(private val calculationRepository: CalculationRepository) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state

    fun setRecipeName(name: String) { _state.value = _state.value.copy(recipeName = name); recalculate() }
    fun setCategory(cat: String) { _state.value = _state.value.copy(category = cat) }
    fun setIngredients(list: List<IngredientWithQuantity>) { _state.value = _state.value.copy(ingredients = list); recalculate() }
    fun setLaborCost(cost: Double) { _state.value = _state.value.copy(laborCost = cost); recalculate() }
    fun setDepreciationCost(cost: Double) { _state.value = _state.value.copy(depreciationCost = cost); recalculate() }
    fun setOverheadCost(cost: Double) { _state.value = _state.value.copy(overheadCost = cost); recalculate() }
    fun setYield(y: Int) { if (y > 0) { _state.value = _state.value.copy(yield = y); recalculate() } }
    fun setMarginPct(m: Double) { _state.value = _state.value.copy(marginPct = m.coerceIn(0.0, 99.0)); recalculate() }
    fun setDiscountPct(d: Double) { _state.value = _state.value.copy(discountPct = d.coerceIn(0.0, 100.0)); recalculate() }
    fun setPlatformFeePct(f: Double) { _state.value = _state.value.copy(platformFeePct = f.coerceIn(0.0, 100.0)); recalculate() }
    fun setPackagingFee(fee: Double) { _state.value = _state.value.copy(packagingFee = fee.coerceAtLeast(0.0)); recalculate() }

    fun selectPlatform(index: Int) {
        val platform = MerchantPlatforms.platforms.getOrNull(index) ?: return
        _state.value = _state.value.copy(
            selectedPlatformIndex = index,
            platformFeePct = platform.defaultFeePct,
            packagingFee = platform.packagingFeeDefault
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _state.value
        val materialCost = HppCalculator.materialCost(s.ingredients.map { it.pricePerUnit to it.quantity })
        val totalCost = materialCost + s.laborCost + s.depreciationCost + s.overheadCost
        val hpp = if (s.yield > 0) totalCost / s.yield else 0.0
        val price = if (hpp > 0 && s.marginPct < 100) HppCalculator.suggestedPrice(hpp, s.marginPct) else 0.0

        // Multi-margin
        val p30 = if (hpp > 0) HppCalculator.suggestedPrice(hpp, 30.0) else 0.0
        val p40 = if (hpp > 0) HppCalculator.suggestedPrice(hpp, 40.0) else 0.0
        val p50 = if (hpp > 0) HppCalculator.suggestedPrice(hpp, 50.0) else 0.0

        // BEP
        val fixedCost = s.depreciationCost + s.overheadCost
        val profitPerUnit = price - hpp
        val bep = if (profitPerUnit > 0) (fixedCost / profitPerUnit).toInt() + 1 else 0

        // Promo simulation with current platform
        val afterDiscount = price * (1 - s.discountPct / 100.0)
        val feeAmount = afterDiscount * (s.platformFeePct / 100.0)
        val afterFee = afterDiscount - feeAmount - s.packagingFee
        val promoProfit = afterFee - hpp

        // Platform comparison table
        val comparisons = if (price > 0) {
            MerchantPlatforms.platforms.mapIndexed { idx, platform ->
                val feePct = if (idx == s.selectedPlatformIndex && platform.name == "Custom") s.platformFeePct else platform.defaultFeePct
                val pkgFee = if (platform.hasPackagingFee) platform.packagingFeeDefault else 0.0
                val priceAfterDisc = price * (1 - s.discountPct / 100.0)
                val fee = priceAfterDisc * (feePct / 100.0)
                val net = priceAfterDisc - fee - pkgFee
                val profit = net - hpp
                PlatformComparison(
                    platform = platform,
                    feePct = feePct,
                    feeAmount = fee,
                    packagingFee = pkgFee,
                    netRevenue = net,
                    profit = profit,
                    profitPct = if (net > 0) (profit / net * 100.0) else 0.0,
                    isLoss = profit < 0
                )
            }
        } else emptyList()

        _state.value = s.copy(
            totalMaterialCost = materialCost,
            hppPerUnit = hpp,
            suggestedPrice = price,
            price30 = p30, price40 = p40, price50 = p50,
            bepUnits = bep,
            priceAfterDiscount = afterDiscount,
            priceAfterFee = afterFee,
            profitAfterPromo = promoProfit,
            platformComparisons = comparisons,
            saved = false
        )
    }

    fun saveCalculation() {
        val s = _state.value
        if (s.recipeName.isBlank() || s.hppPerUnit <= 0) return
        viewModelScope.launch {
            calculationRepository.insert(CalculationResult(
                recipeName = s.recipeName,
                category = s.category,
                totalMaterialCost = s.totalMaterialCost,
                laborCost = s.laborCost,
                depreciationCost = s.depreciationCost,
                overheadCost = s.overheadCost,
                yield = s.yield,
                hppPerUnit = s.hppPerUnit,
                marginPct = s.marginPct,
                suggestedPrice = s.suggestedPrice
            ))
            _state.value = s.copy(saved = true)
        }
    }

    class Factory(private val repo: CalculationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalculatorViewModel(repo) as T
    }
}
