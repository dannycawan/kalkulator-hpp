package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.kalkulator.hpp.ui.viewmodel.IngredientViewModel
import com.kalkulator.hpp.ui.viewmodel.RecipeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeViewModel: RecipeViewModel,
    ingredientViewModel: IngredientViewModel,
    onBack: () -> Unit
) {
    val recipe by recipeViewModel.selectedRecipe.collectAsState()
    val recipeIngredients by recipeViewModel.recipeIngredients.collectAsState()
    val allIngredients by ingredientViewModel.ingredients.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var showAddDialog by remember { mutableStateOf(false) }

    val currentRecipe = recipe ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRecipe.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Tambah Bahan ke Resep")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                if (currentRecipe.description.isNotBlank()) {
                    Text(currentRecipe.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                }
                Text("Yield: ${currentRecipe.yield} unit", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Bahan Baku", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (recipeIngredients.isEmpty()) {
                item {
                    Text("Belum ada bahan. Tekan + untuk menambahkan.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            items(recipeIngredients) { ing ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ing.name, fontWeight = FontWeight.SemiBold)
                            Text("${ing.quantity} ${ing.unit} × ${currencyFormat.format(ing.pricePerUnit)}", style = MaterialTheme.typography.bodySmall)
                            Text("Subtotal: ${currencyFormat.format(ing.quantity * ing.pricePerUnit)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { recipeViewModel.removeIngredient(ing.crossRefId) }) {
                            Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (recipeIngredients.isNotEmpty()) {
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    val total = recipeIngredients.sumOf { it.quantity * it.pricePerUnit }
                    Text("Total Biaya Bahan: ${currencyFormat.format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }

    if (showAddDialog) {
        AddIngredientToRecipeDialog(
            availableIngredients = allIngredients.filter { available -> recipeIngredients.none { it.id == available.id } },
            onDismiss = { showAddDialog = false },
            onAdd = { ingredientId, qty ->
                recipeViewModel.addIngredient(currentRecipe.id, ingredientId, qty)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientToRecipeDialog(availableIngredients: List<Ingredient>, onDismiss: () -> Unit, onAdd: (Long, Double) -> Unit) {
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selected = availableIngredients.find { it.id == selectedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Bahan ke Resep") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (availableIngredients.isEmpty()) {
                    Text("Semua bahan sudah ditambahkan, atau belum ada bahan baku. Tambahkan di tab Bahan Baku.")
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selected?.name ?: "Pilih bahan...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Bahan") }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availableIngredients.forEach { ing ->
                                DropdownMenuItem(
                                    text = { Text("${ing.name} (${ing.unit})") },
                                    onClick = { selectedId = ing.id; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it },
                        label = { Text("Jumlah (${selected?.unit ?: "satuan"})") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = selectedId ?: return@TextButton
                    val qty = quantity.toDoubleOrNull() ?: return@TextButton
                    if (qty > 0) onAdd(id, qty)
                },
                enabled = selectedId != null && (quantity.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Tambah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
