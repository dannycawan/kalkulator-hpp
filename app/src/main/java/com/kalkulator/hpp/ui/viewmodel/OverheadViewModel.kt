package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.OverheadItem
import com.kalkulator.hpp.data.repository.OverheadRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OverheadViewModel(private val repository: OverheadRepository) : ViewModel() {

    val overheadItems: StateFlow<List<OverheadItem>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthlyCost: StateFlow<Double> = repository.getTotalMonthlyCost()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val showDialog = MutableStateFlow(false)
    val editingItem = MutableStateFlow<OverheadItem?>(null)

    fun insert(item: OverheadItem) { viewModelScope.launch { repository.insert(item) } }
    fun update(item: OverheadItem) { viewModelScope.launch { repository.update(item) } }
    fun delete(item: OverheadItem) { viewModelScope.launch { repository.delete(item) } }

    fun openAddDialog() { editingItem.value = null; showDialog.value = true }
    fun openEditDialog(item: OverheadItem) { editingItem.value = item; showDialog.value = true }
    fun closeDialog() { showDialog.value = false; editingItem.value = null }

    /** Calculate overhead cost per serving based on daily production */
    fun overheadPerServing(dailyProduction: Int): Double {
        if (dailyProduction <= 0) return 0.0
        return totalMonthlyCost.value / (dailyProduction * 30.0)
    }

    class Factory(private val repo: OverheadRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OverheadViewModel(repo) as T
    }
}
