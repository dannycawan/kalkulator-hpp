package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.kalkulator.hpp.data.repository.CalculationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: CalculationRepository) : ViewModel() {

    private val _allHistory = repository.getAll()

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("")
    val selectionMode = MutableStateFlow(false)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val history: StateFlow<List<CalculationResult>> = combine(_allHistory, searchQuery, selectedCategory) { list, query, cat ->
        list.filter { item ->
            (query.isBlank() || item.recipeName.contains(query, ignoreCase = true)) &&
            (cat.isBlank() || item.category == cat)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun setCategory(cat: String) { selectedCategory.value = cat }

    fun toggleSelection(id: Long) {
        val current = selectedIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        selectedIds.value = current
        if (current.isEmpty()) selectionMode.value = false
    }

    fun startSelectionMode(id: Long) {
        selectionMode.value = true
        selectedIds.value = setOf(id)
    }

    fun clearSelection() {
        selectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            repository.deleteByIds(selectedIds.value.toList())
            clearSelection()
        }
    }

    fun delete(item: CalculationResult) { viewModelScope.launch { repository.delete(item) } }
    fun deleteAll() { viewModelScope.launch { repository.deleteAll() } }

    class Factory(private val repo: CalculationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(repo) as T
    }
}
