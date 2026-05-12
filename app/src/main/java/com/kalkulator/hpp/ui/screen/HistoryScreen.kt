package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.ui.viewmodel.HistoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onEditCalculation: ((Long) -> Unit)? = null) {
    val history by viewModel.history.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<com.kalkulator.hpp.data.local.entity.CalculationResult?>(null) }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} dipilih") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, "Batal")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(Icons.Default.Delete, "Hapus Terpilih", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                )
            } else {
                TopAppBar(
                    title = { Text("Riwayat & Arsip") },
                    actions = {
                        if (history.isNotEmpty()) {
                            IconButton(onClick = { showClearDialog = true }) {
                                Icon(Icons.Default.DeleteSweep, "Hapus Semua")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari riwayat...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "Hapus")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(if (searchQuery.isBlank()) "Belum ada riwayat" else "Tidak ditemukan", style = MaterialTheme.typography.titleMedium)
                        Text("Hasil perhitungan akan muncul di sini", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        val isSelected = selectedIds.contains(item.id)
                        ElevatedCard(
                            Modifier.fillMaxWidth().combinedClickable(
                                onClick = {
                                    if (selectionMode) viewModel.toggleSelection(item.id)
                                },
                                onLongClick = {
                                    if (!selectionMode) viewModel.startSelectionMode(item.id)
                                    else viewModel.toggleSelection(item.id)
                                }
                            ),
                            colors = if (isSelected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                     else CardDefaults.elevatedCardColors()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    if (selectionMode) {
                                        Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleSelection(item.id) })
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(item.recipeName, fontWeight = FontWeight.Bold)
                                            if (item.category.isNotBlank()) {
                                                Spacer(Modifier.width(8.dp))
                                                SuggestionChip(onClick = {}, label = { Text(item.category, style = MaterialTheme.typography.labelSmall) })
                                            }
                                        }
                                        Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (!selectionMode) {
                                        IconButton(onClick = { showDeleteDialog = item }) {
                                            Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    Text("HPP/Unit", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    Text(currencyFormat.format(item.hppPerUnit), fontWeight = FontWeight.SemiBold)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Harga Jual (margin ${item.marginPct.toInt()}%)", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    Text(currencyFormat.format(item.suggestedPrice), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Profit/Unit", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    Text(currencyFormat.format(item.suggestedPrice - item.hppPerUnit), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Hapus Semua Riwayat?") },
            text = { Text("Semua data riwayat perhitungan akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); showClearDialog = false }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Batal") } }
        )
    }

    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Riwayat?") },
            text = { Text("\"${item.recipeName}\" akan dihapus dari riwayat.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(item); showDeleteDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") } }
        )
    }
}
