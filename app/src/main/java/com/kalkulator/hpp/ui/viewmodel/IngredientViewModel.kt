package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.kalkulator.hpp.data.repository.IngredientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IngredientViewModel(private val repository: IngredientRepository) : ViewModel() {

    val ingredients: StateFlow<List<Ingredient>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val showDialog = MutableStateFlow(false)
    val editingIngredient = MutableStateFlow<Ingredient?>(null)

    // Search & Filter
    val searchQuery = MutableStateFlow("")

    val filteredIngredients: StateFlow<List<Ingredient>> = combine(ingredients, searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) || it.unit.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { searchQuery.value = query }

    fun insert(ingredient: Ingredient) { viewModelScope.launch { repository.insert(ingredient) } }
    fun insertAll(ingredients: List<Ingredient>) { viewModelScope.launch { repository.insertAll(ingredients) } }
    fun update(ingredient: Ingredient) { viewModelScope.launch { repository.update(ingredient) } }
    fun delete(ingredient: Ingredient) { viewModelScope.launch { repository.delete(ingredient) } }
    fun updateStock(id: Long, stock: Double) { viewModelScope.launch { repository.updateStock(id, stock) } }

    fun openAddDialog() { editingIngredient.value = null; showDialog.value = true }
    fun openEditDialog(ingredient: Ingredient) { editingIngredient.value = ingredient; showDialog.value = true }
    fun closeDialog() { showDialog.value = false; editingIngredient.value = null }

    class Factory(private val repo: IngredientRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = IngredientViewModel(repo) as T
    }
}
