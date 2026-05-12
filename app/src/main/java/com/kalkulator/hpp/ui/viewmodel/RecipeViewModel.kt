package com.kalkulator.hpp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.data.repository.RecipeRepository
import com.kalkulator.hpp.domain.model.IngredientWithQuantity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val recipes: StateFlow<List<Recipe>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe

    val recipeIngredients: StateFlow<List<IngredientWithQuantity>> = _selectedRecipe
        .flatMapLatest { recipe ->
            if (recipe != null) repository.getIngredientsForRecipe(recipe.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectRecipe(id: Long) { viewModelScope.launch { _selectedRecipe.value = repository.getById(id) } }
    fun insert(recipe: Recipe) { viewModelScope.launch { repository.insert(recipe) } }
    fun update(recipe: Recipe) { viewModelScope.launch { repository.update(recipe) } }
    fun delete(recipe: Recipe) { viewModelScope.launch { repository.delete(recipe) } }
    fun addIngredient(recipeId: Long, ingredientId: Long, qty: Double) {
        viewModelScope.launch { repository.addIngredientToRecipe(recipeId, ingredientId, qty) }
    }
    fun removeIngredient(crossRefId: Long) {
        viewModelScope.launch { repository.removeIngredientFromRecipe(crossRefId) }
    }

    class Factory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecipeViewModel(repo) as T
    }
}
