package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.Equipment
import com.kalkulator.hpp.data.repository.EquipmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EquipmentViewModel(private val repository: EquipmentRepository) : ViewModel() {

    val equipment: StateFlow<List<Equipment>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val showDialog = MutableStateFlow(false)
    val editingEquipment = MutableStateFlow<Equipment?>(null)

    /** Total monthly depreciation of all equipment */
    val totalMonthlyDepreciation: StateFlow<Double> = equipment
        .map { list -> list.sumOf { it.monthlyDepreciation } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun insert(item: Equipment) { viewModelScope.launch { repository.insert(item) } }
    fun update(item: Equipment) { viewModelScope.launch { repository.update(item) } }
    fun delete(item: Equipment) { viewModelScope.launch { repository.delete(item) } }

    fun openAddDialog() { editingEquipment.value = null; showDialog.value = true }
    fun openEditDialog(item: Equipment) { editingEquipment.value = item; showDialog.value = true }
    fun closeDialog() { showDialog.value = false; editingEquipment.value = null }

    /** Calculate depreciation cost per serving based on daily production */
    fun depreciationPerServing(dailyProduction: Int): Double {
        if (dailyProduction <= 0) return 0.0
        val monthlyDep = totalMonthlyDepreciation.value
        return monthlyDep / (dailyProduction * 30.0)
    }

    class Factory(private val repo: EquipmentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EquipmentViewModel(repo) as T
    }
}
