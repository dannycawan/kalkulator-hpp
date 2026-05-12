package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.clickable
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
import com.kalkulator.hpp.domain.model.TemplateData
import com.kalkulator.hpp.domain.model.RecipeTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(onUseTemplate: (RecipeTemplate) -> Unit) {
    val templates = remember { TemplateData.templates }
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = remember { listOf("Semua") + TemplateData.categories }
    var showDetailDialog by remember { mutableStateOf<RecipeTemplate?>(null) }

    val filtered = if (selectedCategory == "Semua") templates else templates.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Template Resep")
                        Text("${templates.size} template siap pakai", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category filter chips
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            items(filtered) { template ->
                ElevatedCard(
                    Modifier.fillMaxWidth().clickable { showDetailDialog = template }
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(template.emoji, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(template.name, fontWeight = FontWeight.Bold)
                            Text(template.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text("${template.ingredients.size} bahan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilledTonalButton(onClick = { onUseTemplate(template) }) {
                            Text("Pakai", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
    showDetailDialog?.let { template ->
        AlertDialog(
            onDismissRequest = { showDetailDialog = null },
            title = { Text("${template.emoji} ${template.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Kategori: ${template.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("Yield: ${template.yield} porsi", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Text("Bahan:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    template.ingredients.forEach { (name, qty, unit) ->
                        Text("• $name: $qty $unit", style = MaterialTheme.typography.bodySmall)
                    }
                    if (template.laborCost > 0) {
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Text("Tenaga Kerja: Rp${template.laborCost.toLong()}/porsi", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onUseTemplate(template); showDetailDialog = null }) { Text("Gunakan Template") }
            },
            dismissButton = { TextButton(onClick = { showDetailDialog = null }) { Text("Tutup") } }
        )
    }
}
