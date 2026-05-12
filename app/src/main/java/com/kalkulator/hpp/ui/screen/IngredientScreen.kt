package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    val ingredients by viewModel.ingredients.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val editing by viewModel.editingIngredient.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Bahan")
            }
        }
    ) { padding ->
        if (ingredients.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Belum ada bahan baku", style = MaterialTheme.typography.titleMedium)
                    Text("Tekan + untuk menambahkan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients, key = { it.id }) { ingredient ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(ingredient.name, fontWeight = FontWeight.Bold)
                                Text("${currencyFormat.format(ingredient.pricePerUnit)} / ${ingredient.unit}", style = MaterialTheme.typography.bodyMedium)
                                ingredient.supplier?.let { Text("Supplier: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            IconButton(onClick = { viewModel.openEditDialog(ingredient) }) { Icon(Icons.Default.Edit, "Edit") }
                            IconButton(onClick = { viewModel.delete(ingredient) }) { Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
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
}

@Composable
fun IngredientDialog(editing: Ingredient?, onDismiss: () -> Unit, onSave: (Ingredient) -> Unit) {
    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var unit by remember(editing) { mutableStateOf(editing?.unit ?: "") }
    var price by remember(editing) { mutableStateOf(editing?.pricePerUnit?.toString() ?: "") }
    var supplier by remember(editing) { mutableStateOf(editing?.supplier ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing != null) "Edit Bahan" else "Tambah Bahan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Bahan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Satuan (gram, ml, pcs)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga per Satuan (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier (opsional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = price.toDoubleOrNull() ?: return@TextButton
                if (name.isBlank() || unit.isBlank()) return@TextButton
                onSave(Ingredient(id = editing?.id ?: 0, name = name.trim(), unit = unit.trim(), pricePerUnit = p, supplier = supplier.ifBlank { null }))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
