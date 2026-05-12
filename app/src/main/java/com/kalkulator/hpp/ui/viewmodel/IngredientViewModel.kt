package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.kalkulator.hpp.data.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientViewModel(private val repository: IngredientRepository) : ViewModel() {

    val ingredients: StateFlow<List<Ingredient>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val showDialog = MutableStateFlow(false)
    val editingIngredient = MutableStateFlow<Ingredient?>(null)

    fun insert(ingredient: Ingredient) { viewModelScope.launch { repository.insert(ingredient) } }
    fun update(ingredient: Ingredient) { viewModelScope.launch { repository.update(ingredient) } }
    fun delete(ingredient: Ingredient) { viewModelScope.launch { repository.delete(ingredient) } }

    fun openAddDialog() { editingIngredient.value = null; showDialog.value = true }
    fun openEditDialog(ingredient: Ingredient) { editingIngredient.value = ingredient; showDialog.value = true }
    fun closeDialog() { showDialog.value = false; editingIngredient.value = null }

    class Factory(private val repo: IngredientRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = IngredientViewModel(repo) as T
    }
}
