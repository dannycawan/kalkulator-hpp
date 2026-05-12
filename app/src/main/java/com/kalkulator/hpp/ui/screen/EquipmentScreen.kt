package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.data.local.entity.Equipment
import com.kalkulator.hpp.ui.viewmodel.EquipmentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(viewModel: EquipmentViewModel, dailyProduction: Int) {
    val equipment by viewModel.equipment.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editing by viewModel.editingEquipment.collectAsState()
    val totalDepreciation by viewModel.totalMonthlyDepreciation.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var showDeleteDialog by remember { mutableStateOf<Equipment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biaya Alat & Peralatan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, "Tambah Alat")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary card
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ringkasan Depresiasi", fontWeight = FontWeight.Bold)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("Total Penyusutan/Bulan", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(currencyFormat.format(totalDepreciation), fontWeight = FontWeight.Bold)
                        }
                        val perServing = if (dailyProduction > 0) totalDepreciation / (dailyProduction * 30.0) else 0.0
                        Row(Modifier.fillMaxWidth()) {
                            Text("Penyusutan/Porsi (${dailyProduction} porsi/hari)", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text(currencyFormat.format(perServing), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (equipment.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Build, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada alat", style = MaterialTheme.typography.titleMedium)
                        Text("Tambahkan alat untuk menghitung depresiasi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(equipment, key = { it.id }) { item ->
                val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id")) }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Harga: ${currencyFormat.format(item.purchasePrice)}", style = MaterialTheme.typography.bodySmall)
                                Text("Beli: ${dateFormat.format(Date(item.purchaseDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Umur: ${item.usefulLifeMonths} bulan | Sisa: ${currencyFormat.format(item.residualValue)}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.openEditDialog(item) }) { Icon(Icons.Default.Edit, "Edit") }
                            IconButton(onClick = { showDeleteDialog = item }) { Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error) }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Text("Penyusutan: ${currencyFormat.format(item.monthlyDepreciation)} / bulan",
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium)
                        if (item.notes.isNotBlank()) {
                            Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    // Add/Edit dialog
    if (showDialog) {
        EquipmentDialog(editing = editing, onDismiss = { viewModel.closeDialog() }, onSave = { eq ->
            if (editing != null) viewModel.update(eq) else viewModel.insert(eq)
            viewModel.closeDialog()
        })
    }

    // Delete confirmation
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Alat?") },
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
fun EquipmentDialog(editing: Equipment?, onDismiss: () -> Unit, onSave: (Equipment) -> Unit) {
    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var price by remember(editing) { mutableStateOf(editing?.purchasePrice?.toLong()?.toString() ?: "") }
    var lifeMonths by remember(editing) { mutableStateOf(editing?.usefulLifeMonths?.toString() ?: "") }
    var residual by remember(editing) { mutableStateOf(editing?.residualValue?.toLong()?.toString() ?: "0") }
    var notes by remember(editing) { mutableStateOf(editing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Edit Alat" else "Tambah Alat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Alat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga Beli (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lifeMonths, onValueChange = { lifeMonths = it }, label = { Text("Umur Ekonomis (bulan)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = residual, onValueChange = { residual = it }, label = { Text("Nilai Sisa (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Keterangan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = price.toDoubleOrNull() ?: return@TextButton
                val l = lifeMonths.toIntOrNull() ?: return@TextButton
                if (name.isBlank() || l <= 0) return@TextButton
                onSave(Equipment(
                    id = editing?.id ?: 0,
                    name = name.trim(),
                    purchasePrice = p,
                    purchaseDate = editing?.purchaseDate ?: System.currentTimeMillis(),
                    usefulLifeMonths = l,
                    residualValue = residual.toDoubleOrNull() ?: 0.0,
                    notes = notes.trim()
                ))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
