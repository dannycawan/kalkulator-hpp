package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.data.local.entity.OverheadItem
import com.kalkulator.hpp.ui.viewmodel.OverheadViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverheadScreen(viewModel: OverheadViewModel, dailyProduction: Int) {
    val items by viewModel.overheadItems.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editing by viewModel.editingItem.collectAsState()
    val totalCost by viewModel.totalMonthlyCost.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var showDeleteDialog by remember { mutableStateOf<OverheadItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biaya Overhead") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, "Tambah Biaya")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ringkasan Overhead", fontWeight = FontWeight.Bold)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("Total Biaya/Bulan", Modifier.weight(1f))
                            Text(currencyFormat.format(totalCost), fontWeight = FontWeight.Bold)
                        }
                        val perServing = if (dailyProduction > 0) totalCost / (dailyProduction * 30.0) else 0.0
                        Row(Modifier.fillMaxWidth()) {
                            Text("Overhead/Porsi (${dailyProduction} porsi/hari)", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text(currencyFormat.format(perServing), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Receipt, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada biaya overhead", style = MaterialTheme.typography.titleMedium)
                        Text("Contoh: Sewa ruko, listrik, internet, dll", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(items, key = { it.id }) { item ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("${currencyFormat.format(item.monthlyCost)} / bulan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            if (item.notes.isNotBlank()) {
                                Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { viewModel.openEditDialog(item) }) { Icon(Icons.Default.Edit, "Edit") }
                        IconButton(onClick = { showDeleteDialog = item }) { Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showDialog) {
        OverheadDialog(editing = editing, onDismiss = { viewModel.closeDialog() }, onSave = { oh ->
            if (editing != null) viewModel.update(oh) else viewModel.insert(oh)
            viewModel.closeDialog()
        })
    }

    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Biaya?") },
            text = { Text("\"${item.name}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(item); showDeleteDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun OverheadDialog(editing: OverheadItem?, onDismiss: () -> Unit, onSave: (OverheadItem) -> Unit) {
    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var cost by remember(editing) { mutableStateOf(editing?.monthlyCost?.toLong()?.toString() ?: "") }
    var notes by remember(editing) { mutableStateOf(editing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Edit Biaya" else "Tambah Biaya Overhead") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Biaya") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: Sewa Ruko") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Nominal per Bulan (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Keterangan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val c = cost.toDoubleOrNull() ?: return@TextButton
                if (name.isBlank()) return@TextButton
                onSave(OverheadItem(id = editing?.id ?: 0, name = name.trim(), monthlyCost = c, notes = notes.trim()))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
