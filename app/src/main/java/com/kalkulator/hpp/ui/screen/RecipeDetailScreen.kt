package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var scaleFactor by remember { mutableStateOf(1) }

    val currentRecipe = recipe ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentRecipe.name)
                        if (currentRecipe.category.isNotBlank()) {
                            Text(currentRecipe.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Tambah Bahan ke Resep")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recipe info
            item {
                if (currentRecipe.description.isNotBlank()) {
                    Text(currentRecipe.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                }
                if (currentRecipe.notes.isNotBlank()) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Note, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text(currentRecipe.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text("Yield: ${currentRecipe.yield} unit", style = MaterialTheme.typography.bodyMedium)
            }

            // Scaling
            item {
                Text("Scaling Porsi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val scales = listOf(1, 10, 50, 100, 500)
                    items(scales) { s ->
                        FilterChip(
                            selected = scaleFactor == s,
                            onClick = { scaleFactor = s },
                            label = { Text("${s}x") }
                        )
                    }
                }
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("Bahan Baku ${if (scaleFactor > 1) "(${scaleFactor}x porsi)" else ""}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (recipeIngredients.isEmpty()) {
                item {
                    Text("Belum ada bahan. Tekan + untuk menambahkan.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            items(recipeIngredients) { ing ->
                val scaledQty = ing.quantity * scaleFactor
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ing.name, fontWeight = FontWeight.SemiBold)
                            Text("$scaledQty ${ing.unit} × ${currencyFormat.format(ing.pricePerUnit)}", style = MaterialTheme.typography.bodySmall)
                            Text("Subtotal: ${currencyFormat.format(scaledQty * ing.pricePerUnit)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteDialog = ing.crossRefId }) {
                            Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (recipeIngredients.isNotEmpty()) {
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    val total = recipeIngredients.sumOf { it.quantity * it.pricePerUnit * scaleFactor }
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Total Biaya Bahan ${if (scaleFactor > 1) "(${scaleFactor}x)" else ""}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text(currencyFormat.format(total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
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

    // Delete confirmation
    showDeleteDialog?.let { crossRefId ->
        val ingName = recipeIngredients.find { it.crossRefId == crossRefId }?.name ?: "bahan"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Bahan?") },
            text = { Text("\"$ingName\" akan dihapus dari resep ini.") },
            confirmButton = {
                TextButton(onClick = { recipeViewModel.removeIngredient(crossRefId); showDeleteDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") } }
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
