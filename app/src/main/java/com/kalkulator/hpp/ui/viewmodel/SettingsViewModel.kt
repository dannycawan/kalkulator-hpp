package com.kalkulator.hpp.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kalkulator.hpp.di.SettingsKeys
import com.kalkulator.hpp.di.settingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val context: Context) : ViewModel() {

    private val dataStore = context.settingsDataStore

    val isDarkMode: StateFlow<Boolean> = dataStore.data
        .map { it[SettingsKeys.DARK_MODE] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val followSystem: StateFlow<Boolean> = dataStore.data
        .map { it[SettingsKeys.FOLLOW_SYSTEM] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyProduction: StateFlow<Int> = dataStore.data
        .map { it[SettingsKeys.DAILY_PRODUCTION] ?: 50 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val currencySymbol: StateFlow<String> = dataStore.data
        .map { it[SettingsKeys.CURRENCY_SYMBOL] ?: "Rp" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rp")

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.DARK_MODE] = enabled }
        }
    }

    fun setFollowSystem(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.FOLLOW_SYSTEM] = enabled }
        }
    }

    fun setDailyProduction(count: Int) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.DAILY_PRODUCTION] = count.coerceAtLeast(1) }
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.CURRENCY_SYMBOL] = symbol }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(context) as T
    }
}
