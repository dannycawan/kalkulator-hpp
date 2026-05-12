package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val history by viewModel.history.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (history.isNotEmpty()) {
                TopAppBar(
                    title = { Text("Riwayat (${history.size})") },
                    actions = {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, "Hapus Semua")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Belum ada riwayat", style = MaterialTheme.typography.titleMedium)
                    Text("Hasil perhitungan akan muncul di sini", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { item ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.recipeName, fontWeight = FontWeight.Bold)
                                    Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.delete(item) }) {
                                    Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
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
}
