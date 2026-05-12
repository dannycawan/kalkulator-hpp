package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.kalkulator.hpp.data.repository.CalculationRepository
import com.kalkulator.hpp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    private val recipeRepository: RecipeRepository,
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    val recipeCount: StateFlow<Int> = recipeRepository.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val avgHpp: StateFlow<Double> = calculationRepository.getAverageHpp()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val avgMargin: StateFlow<Double> = calculationRepository.getAverageMargin()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topProfitable: StateFlow<List<CalculationResult>> = calculationRepository.getTopProfitable()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topExpensiveHpp: StateFlow<List<CalculationResult>> = calculationRepository.getTopExpensiveHpp()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculationCount: StateFlow<Int> = calculationRepository.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Estimated monthly profit = sum of (suggestedPrice - hppPerUnit) * 30 days for top items */
    val estimatedMonthlyProfit: StateFlow<Double> = topProfitable
        .map { list ->
            list.sumOf { (it.suggestedPrice - it.hppPerUnit) * 30.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    class Factory(
        private val recipeRepo: RecipeRepository,
        private val calcRepo: CalculationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(recipeRepo, calcRepo) as T
    }
}
