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

data class CalculatorUiState(
    val recipeName: String = "",
    val ingredients: List<IngredientWithQuantity> = emptyList(),
    val laborCost: Double = 0.0,
    val overheadCost: Double = 0.0,
    val yield: Int = 1,
    val marginPct: Double = 30.0,
    val totalMaterialCost: Double = 0.0,
    val hppPerUnit: Double = 0.0,
    val suggestedPrice: Double = 0.0,
    val saved: Boolean = false
)

class CalculatorViewModel(private val calculationRepository: CalculationRepository) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state

    fun setRecipeName(name: String) { _state.value = _state.value.copy(recipeName = name); recalculate() }
    fun setIngredients(list: List<IngredientWithQuantity>) { _state.value = _state.value.copy(ingredients = list); recalculate() }
    fun setLaborCost(cost: Double) { _state.value = _state.value.copy(laborCost = cost); recalculate() }
    fun setOverheadCost(cost: Double) { _state.value = _state.value.copy(overheadCost = cost); recalculate() }
    fun setYield(y: Int) { if (y > 0) { _state.value = _state.value.copy(yield = y); recalculate() } }
    fun setMarginPct(m: Double) { _state.value = _state.value.copy(marginPct = m.coerceIn(0.0, 99.0)); recalculate() }

    private fun recalculate() {
        val s = _state.value
        val materialCost = HppCalculator.materialCost(s.ingredients.map { it.pricePerUnit to it.quantity })
        val hpp = if (s.yield > 0) HppCalculator.hpp(materialCost, s.laborCost, s.overheadCost, s.yield) else 0.0
        val price = if (hpp > 0 && s.marginPct < 100) HppCalculator.suggestedPrice(hpp, s.marginPct) else 0.0
        _state.value = s.copy(totalMaterialCost = materialCost, hppPerUnit = hpp, suggestedPrice = price, saved = false)
    }

    fun saveCalculation() {
        val s = _state.value
        if (s.recipeName.isBlank() || s.hppPerUnit <= 0) return
        viewModelScope.launch {
            calculationRepository.insert(CalculationResult(
                recipeName = s.recipeName,
                totalMaterialCost = s.totalMaterialCost,
                laborCost = s.laborCost,
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
