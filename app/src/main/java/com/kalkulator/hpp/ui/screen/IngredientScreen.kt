package com.kalkulator.hpp.ui.screen

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
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.kalkulator.hpp.ui.viewmodel.IngredientViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(viewModel: IngredientViewModel) {
    val ingredients by viewModel.filteredIngredients.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editing by viewModel.editingIngredient.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var showDeleteDialog by remember { mutableStateOf<Ingredient?>(null) }
    var showStockDialog by remember { mutableStateOf<Ingredient?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bahan Baku") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Bahan")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari bahan baku...") },
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

            if (ingredients.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(if (searchQuery.isBlank()) "Belum ada bahan baku" else "Tidak ditemukan", style = MaterialTheme.typography.titleMedium)
                        Text(if (searchQuery.isBlank()) "Tekan + untuk menambahkan" else "Coba kata kunci lain", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ingredients, key = { it.id }) { ingredient ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(ingredient.name, fontWeight = FontWeight.Bold)
                                        Text("${currencyFormat.format(ingredient.pricePerUnit)} / ${ingredient.unit}", style = MaterialTheme.typography.bodyMedium)
                                        if (ingredient.stock > 0) {
                                            Text("Stok: ${ingredient.stock} ${ingredient.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (ingredient.stock < 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        ingredient.supplier?.let {
                                            Text("Supplier: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (ingredient.notes.isNotBlank()) {
                                            Text(ingredient.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    // Update stock button
                                    IconButton(onClick = { showStockDialog = ingredient }) {
                                        Icon(Icons.Default.Inventory, "Update Stok", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.openEditDialog(ingredient) }) {
                                        Icon(Icons.Default.Edit, "Edit")
                                    }
                                    IconButton(onClick = { showDeleteDialog = ingredient }) {
                                        Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        IngredientDialog(editing = editing, onDismiss = { viewModel.closeDialog() }, onSave = { ing ->
            if (editing != null) viewModel.update(ing) else viewModel.insert(ing)
            viewModel.closeDialog()
        })
    }

    // Delete confirmation
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Bahan?") },
            text = { Text("\"${item.name}\" akan dihapus permanen. Bahan yang sudah dipakai di resep juga akan terhapus.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(item); showDeleteDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") } }
        )
    }

    // Stock update dialog
    showStockDialog?.let { item ->
        var stockStr by remember { mutableStateOf(item.stock.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showStockDialog = null },
            title = { Text("Update Stok: ${item.name}") },
            text = {
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stok saat ini (${item.unit})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    stockStr.toDoubleOrNull()?.let { viewModel.updateStock(item.id, it) }
                    showStockDialog = null
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showStockDialog = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun IngredientDialog(editing: Ingredient?, onDismiss: () -> Unit, onSave: (Ingredient) -> Unit) {
    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var unit by remember(editing) { mutableStateOf(editing?.unit ?: "") }
    var price by remember(editing) { mutableStateOf(editing?.pricePerUnit?.toString() ?: "") }
    var stock by remember(editing) { mutableStateOf(editing?.stock?.toLong()?.toString() ?: "0") }
    var supplier by remember(editing) { mutableStateOf(editing?.supplier ?: "") }
    var notes by remember(editing) { mutableStateOf(editing?.notes ?: "") }

    val unitOptions = listOf("gram", "kg", "ml", "liter", "pcs", "sdm", "sdt", "butir")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Edit Bahan" else "Tambah Bahan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Bahan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                // Unit with suggestions
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Satuan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    unitOptions.take(4).forEach { u ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    unitOptions.drop(4).forEach { u ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga per Satuan (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok saat ini") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier (opsional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Keterangan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = price.toDoubleOrNull() ?: return@TextButton
                if (name.isBlank() || unit.isBlank()) return@TextButton
                onSave(Ingredient(
                    id = editing?.id ?: 0,
                    name = name.trim(),
                    unit = unit.trim(),
                    pricePerUnit = p,
                    stock = stock.toDoubleOrNull() ?: 0.0,
                    supplier = supplier.ifBlank { null },
                    notes = notes.trim()
                ))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
