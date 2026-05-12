package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.kalkulator.hpp.data.repository.CalculationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: CalculationRepository) : ViewModel() {

    val history: StateFlow<List<CalculationResult>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(item: CalculationResult) { viewModelScope.launch { repository.delete(item) } }
    fun deleteAll() { viewModelScope.launch { repository.deleteAll() } }

    class Factory(private val repo: CalculationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(repo) as T
    }
}
