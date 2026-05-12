package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: RecipeViewModel, onRecipeClick: (Long) -> Unit) {
    val recipes by viewModel.filteredRecipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Resep") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Resep")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari resep...") },
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

            // Category filter
            if (categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory.isBlank(),
                            onClick = { viewModel.setCategory("") },
                            label = { Text("Semua") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.setCategory(if (selectedCategory == cat) "" else cat) },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (recipes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(if (searchQuery.isBlank() && selectedCategory.isBlank()) "Belum ada resep" else "Tidak ditemukan", style = MaterialTheme.typography.titleMedium)
                        Text(if (searchQuery.isBlank()) "Tekan + untuk menambahkan" else "Coba kata kunci lain", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        ElevatedCard(Modifier.fillMaxWidth().clickable { onRecipeClick(recipe.id) }) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(recipe.name, fontWeight = FontWeight.Bold)
                                        if (recipe.category.isNotBlank()) {
                                            Spacer(Modifier.width(8.dp))
                                            SuggestionChip(
                                                onClick = { viewModel.setCategory(recipe.category) },
                                                label = { Text(recipe.category, style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }
                                    if (recipe.description.isNotBlank()) {
                                        Text(recipe.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                    }
                                    Text("Yield: ${recipe.yield} unit", style = MaterialTheme.typography.bodySmall)
                                }
                                // Duplicate
                                IconButton(onClick = { viewModel.duplicateRecipe(recipe) }) {
                                    Icon(Icons.Default.ContentCopy, "Duplikat", tint = MaterialTheme.colorScheme.primary)
                                }
                                // Delete
                                IconButton(onClick = { showDeleteDialog = recipe }) {
                                    Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
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
        RecipeDialog(onDismiss = { showDialog = false }, onSave = { recipe ->
            viewModel.insert(recipe)
            showDialog = false
        })
    }

    showDeleteDialog?.let { recipe ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Resep?") },
            text = { Text("\"${recipe.name}\" dan semua bahan di dalamnya akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(recipe); showDeleteDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun RecipeDialog(onDismiss: () -> Unit, onSave: (Recipe) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var yieldStr by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    val categoryOptions = listOf("Makanan", "Minuman", "Snack", "Kue", "Dessert", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Resep") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk/Resep") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi (opsional)") }, modifier = Modifier.fillMaxWidth())
                // Category chips
                Text("Kategori:", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categoryOptions.take(3).forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categoryOptions.drop(3).forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                OutlinedTextField(value = yieldStr, onValueChange = { yieldStr = it }, label = { Text("Yield (jumlah produk)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) return@TextButton
                val y = yieldStr.toIntOrNull() ?: 1
                onSave(Recipe(name = name.trim(), description = description.trim(), category = category, notes = notes.trim(), yield = if (y > 0) y else 1))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
