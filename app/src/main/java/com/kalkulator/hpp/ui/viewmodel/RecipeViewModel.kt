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

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("")

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<Recipe>> = combine(recipes, searchQuery, selectedCategory) { list, query, cat ->
        list.filter { recipe ->
            (query.isBlank() || recipe.name.contains(query, ignoreCase = true)) &&
            (cat.isBlank() || recipe.category == cat)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun setCategory(cat: String) { selectedCategory.value = cat }

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

    /** Duplicate recipe and its ingredients */
    fun duplicateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val newId = repository.duplicateRecipe(recipe)
            // Copy ingredients from original recipe
            repository.getIngredientsForRecipe(recipe.id).first().forEach { ing ->
                repository.addIngredientToRecipe(newId, ing.id, ing.quantity)
            }
        }
    }

    class Factory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecipeViewModel(repo) as T
    }
}
