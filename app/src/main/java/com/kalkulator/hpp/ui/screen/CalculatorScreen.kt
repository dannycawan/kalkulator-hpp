package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.domain.model.IngredientWithQuantity
import com.kalkulator.hpp.ui.viewmodel.CalculatorViewModel
import com.kalkulator.hpp.ui.viewmodel.RecipeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    calculatorViewModel: CalculatorViewModel,
    recipeViewModel: RecipeViewModel
) {
    val recipes by recipeViewModel.recipes.collectAsState()
    val calcState by calculatorViewModel.state.collectAsState()
    val recipeIngredients by recipeViewModel.recipeIngredients.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var laborStr by remember { mutableStateOf("0") }
    var overheadStr by remember { mutableStateOf("0") }
    var yieldStr by remember { mutableStateOf("1") }
    var marginStr by remember { mutableStateOf("30") }

    // When recipe changes, load its ingredients
    LaunchedEffect(selectedRecipeId) {
        selectedRecipeId?.let { recipeViewModel.selectRecipe(it) }
    }
    LaunchedEffect(recipeIngredients, recipeViewModel.selectedRecipe.collectAsState().value) {
        val r = recipeViewModel.selectedRecipe.value ?: return@LaunchedEffect
        calculatorViewModel.setRecipeName(r.name)
        calculatorViewModel.setIngredients(recipeIngredients)
        calculatorViewModel.setLaborCost(r.laborCost)
        calculatorViewModel.setOverheadCost(r.overheadCost)
        calculatorViewModel.setYield(r.yield)
        laborStr = r.laborCost.toLong().toString()
        overheadStr = r.overheadCost.toLong().toString()
        yieldStr = r.yield.toString()
    }

    Scaffold(
        floatingActionButton = {
            if (calcState.hppPerUnit > 0) {
                ExtendedFloatingActionButton(
                    onClick = { calculatorViewModel.saveCalculation() },
                    icon = { Icon(Icons.Default.Check, "Simpan") },
                    text = { Text(if (calcState.saved) "Tersimpan ✓" else "Simpan Hasil") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Kalkulator HPP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // Recipe selector
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = recipes.find { it.id == selectedRecipeId }?.name ?: "Pilih resep...",
                    onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Resep / Produk") }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    recipes.forEach { recipe ->
                        DropdownMenuItem(
                            text = { Text(recipe.name) },
                            onClick = { selectedRecipeId = recipe.id; expanded = false }
                        )
                    }
                }
            }

            if (selectedRecipeId != null) {
                // Material cost summary
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Biaya Bahan Baku", fontWeight = FontWeight.Bold)
                        if (recipeIngredients.isEmpty()) {
                            Text("Belum ada bahan. Tambahkan di tab Resep.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            recipeIngredients.forEach { ing ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text("${ing.name} (${ing.quantity} ${ing.unit})", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    Text(currencyFormat.format(ing.quantity * ing.pricePerUnit), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text("Total Bahan", Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text(currencyFormat.format(calcState.totalMaterialCost), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Labor & overhead
                OutlinedTextField(value = laborStr, onValueChange = { laborStr = it; calculatorViewModel.setLaborCost(it.toDoubleOrNull() ?: 0.0) }, label = { Text("Biaya Tenaga Kerja (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = overheadStr, onValueChange = { overheadStr = it; calculatorViewModel.setOverheadCost(it.toDoubleOrNull() ?: 0.0) }, label = { Text("Biaya Overhead (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = yieldStr, onValueChange = { yieldStr = it; calculatorViewModel.setYield(it.toIntOrNull() ?: 1) }, label = { Text("Yield (jumlah produk)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = marginStr, onValueChange = { marginStr = it; calculatorViewModel.setMarginPct(it.toDoubleOrNull() ?: 30.0) }, label = { Text("Margin Keuntungan (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Results card
                if (calcState.hppPerUnit > 0) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Hasil Perhitungan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            HorizontalDivider()
                            ResultRow("Total Biaya Bahan", currencyFormat.format(calcState.totalMaterialCost))
                            ResultRow("Biaya Tenaga Kerja", currencyFormat.format(calcState.laborCost))
                            ResultRow("Biaya Overhead", currencyFormat.format(calcState.overheadCost))
                            ResultRow("Total Biaya Produksi", currencyFormat.format(calcState.totalMaterialCost + calcState.laborCost + calcState.overheadCost))
                            ResultRow("Yield", "${calcState.yield} unit")
                            HorizontalDivider()
                            ResultRow("HPP per Unit", currencyFormat.format(calcState.hppPerUnit))
                            ResultRow("Margin", "${calcState.marginPct}%")
                            ResultRow("Harga Jual", currencyFormat.format(calcState.suggestedPrice))
                        }
                    }
                }
            }

            Spacer(Modifier.height(72.dp)) // FAB space
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
