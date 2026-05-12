package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.ui.viewmodel.CalculatorViewModel
import com.kalkulator.hpp.ui.viewmodel.MerchantPlatforms
import com.kalkulator.hpp.ui.viewmodel.RecipeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    calculatorViewModel: CalculatorViewModel,
    recipeViewModel: RecipeViewModel,
    totalMonthlyDepreciation: Double = 0.0,
    totalMonthlyOverhead: Double = 0.0,
    dailyProduction: Int = 50
) {
    val recipes by recipeViewModel.recipes.collectAsState()
    val calcState by calculatorViewModel.state.collectAsState()
    val recipeIngredients by recipeViewModel.recipeIngredients.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var laborStr by remember { mutableStateOf("0") }
    var depreciationStr by remember { mutableStateOf("0") }
    var overheadStr by remember { mutableStateOf("0") }
    var yieldStr by remember { mutableStateOf("1") }
    var marginStr by remember { mutableStateOf("30") }
    var discountStr by remember { mutableStateOf("0") }
    var feeStr by remember { mutableStateOf("0") }
    var packagingStr by remember { mutableStateOf("0") }

    // Auto-calculate depreciation & overhead per serving
    val autoDepreciation = if (dailyProduction > 0) totalMonthlyDepreciation / (dailyProduction * 30.0) else 0.0
    val autoOverhead = if (dailyProduction > 0) totalMonthlyOverhead / (dailyProduction * 30.0) else 0.0

    LaunchedEffect(selectedRecipeId) {
        selectedRecipeId?.let { recipeViewModel.selectRecipe(it) }
    }
    LaunchedEffect(recipeIngredients, recipeViewModel.selectedRecipe.collectAsState().value) {
        val r = recipeViewModel.selectedRecipe.value ?: return@LaunchedEffect
        calculatorViewModel.setRecipeName(r.name)
        calculatorViewModel.setCategory(r.category)
        calculatorViewModel.setIngredients(recipeIngredients)
        calculatorViewModel.setLaborCost(r.laborCost)
        calculatorViewModel.setYield(r.yield)
        laborStr = r.laborCost.toLong().toString()
        yieldStr = r.yield.toString()
        depreciationStr = autoDepreciation.toLong().toString()
        overheadStr = autoOverhead.toLong().toString()
        calculatorViewModel.setDepreciationCost(autoDepreciation)
        calculatorViewModel.setOverheadCost(autoOverhead)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalkulator HPP") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
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
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                // 1. Material cost summary
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("① Biaya Bahan Baku", fontWeight = FontWeight.Bold)
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
                                Text("Total Bahan", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                Text(currencyFormat.format(calcState.totalMaterialCost), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // 2. Labor
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("② Tenaga Kerja Langsung", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = laborStr, onValueChange = { laborStr = it; calculatorViewModel.setLaborCost(it.toDoubleOrNull() ?: 0.0) }, label = { Text("Biaya Tenaga Kerja (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }

                // 3. Depreciation
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("③ Depresiasi Alat", fontWeight = FontWeight.Bold)
                        Text("Auto-fill dari data alat (${currencyFormat.format(autoDepreciation)}/porsi)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(value = depreciationStr, onValueChange = { depreciationStr = it; calculatorViewModel.setDepreciationCost(it.toDoubleOrNull() ?: 0.0) }, label = { Text("Depresiasi per Porsi (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }

                // 4. Overhead
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("④ Biaya Overhead", fontWeight = FontWeight.Bold)
                        Text("Auto-fill dari data overhead (${currencyFormat.format(autoOverhead)}/porsi)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(value = overheadStr, onValueChange = { overheadStr = it; calculatorViewModel.setOverheadCost(it.toDoubleOrNull() ?: 0.0) }, label = { Text("Overhead per Porsi (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }

                // Yield & Margin
                OutlinedTextField(value = yieldStr, onValueChange = { yieldStr = it; calculatorViewModel.setYield(it.toIntOrNull() ?: 1) }, label = { Text("Yield (jumlah produk)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = marginStr, onValueChange = { marginStr = it; calculatorViewModel.setMarginPct(it.toDoubleOrNull() ?: 30.0) }, label = { Text("Margin Keuntungan (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Results
                if (calcState.hppPerUnit > 0) {
                    // Main HPP Result
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Hasil Perhitungan HPP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            HorizontalDivider()
                            ResultRow("Biaya Bahan Baku", currencyFormat.format(calcState.totalMaterialCost))
                            ResultRow("Tenaga Kerja", currencyFormat.format(calcState.laborCost))
                            ResultRow("Depresiasi Alat", currencyFormat.format(calcState.depreciationCost))
                            ResultRow("Overhead", currencyFormat.format(calcState.overheadCost))
                            val totalBiaya = calcState.totalMaterialCost + calcState.laborCost + calcState.depreciationCost + calcState.overheadCost
                            ResultRow("Total Biaya Produksi", currencyFormat.format(totalBiaya))
                            ResultRow("Yield", "${calcState.yield} unit")
                            HorizontalDivider()
                            ResultRow("HPP per Unit", currencyFormat.format(calcState.hppPerUnit))
                            ResultRow("Margin", "${calcState.marginPct}%")
                            ResultRow("Harga Jual", currencyFormat.format(calcState.suggestedPrice))
                            val profitPerUnit = calcState.suggestedPrice - calcState.hppPerUnit
                            ResultRow("Profit per Unit", currencyFormat.format(profitPerUnit))
                        }
                    }

                    // Multi-margin recommendations
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Rekomendasi Harga Jual", fontWeight = FontWeight.Bold)
                            HorizontalDivider()
                            ResultRow("Margin 30%", currencyFormat.format(calcState.price30))
                            ResultRow("Margin 40%", currencyFormat.format(calcState.price40))
                            ResultRow("Margin 50%", currencyFormat.format(calcState.price50))
                            ResultRow("Margin ${calcState.marginPct.toInt()}% (custom)", currencyFormat.format(calcState.suggestedPrice))
                        }
                    }

                    // BEP
                    if (calcState.bepUnits > 0) {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Break Even Point (BEP)", fontWeight = FontWeight.Bold)
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Text("Perlu jual ${calcState.bepUnits} porsi untuk balik modal biaya tetap", style = MaterialTheme.typography.bodyMedium)
                                Text("(depresiasi + overhead)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // ===== MERCHANT PLATFORM SECTION =====
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🛒 Simulasi Merchant Platform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Pilih platform untuk menghitung profit setelah potongan komisi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Platform selector chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(MerchantPlatforms.platforms) { index, platform ->
                                    FilterChip(
                                        selected = calcState.selectedPlatformIndex == index,
                                        onClick = {
                                            calculatorViewModel.selectPlatform(index)
                                            feeStr = platform.defaultFeePct.toString()
                                            packagingStr = platform.packagingFeeDefault.toLong().toString()
                                        },
                                        label = { Text("${platform.emoji} ${platform.name}") },
                                        leadingIcon = if (calcState.selectedPlatformIndex == index) {
                                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }

                            val selectedPlatform = MerchantPlatforms.platforms.getOrNull(calcState.selectedPlatformIndex)

                            // Fee input
                            OutlinedTextField(
                                value = feeStr,
                                onValueChange = {
                                    feeStr = it
                                    calculatorViewModel.setPlatformFeePct(it.toDoubleOrNull() ?: 0.0)
                                },
                                label = { Text("Komisi Platform (%)") },
                                supportingText = {
                                    if (selectedPlatform != null && selectedPlatform.name != "Custom" && selectedPlatform.name != "Langsung") {
                                        Text("Default ${selectedPlatform.name}: ${selectedPlatform.defaultFeePct}%")
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Packaging fee
                            if (selectedPlatform?.hasPackagingFee == true || calcState.packagingFee > 0) {
                                OutlinedTextField(
                                    value = packagingStr,
                                    onValueChange = {
                                        packagingStr = it
                                        calculatorViewModel.setPackagingFee(it.toDoubleOrNull() ?: 0.0)
                                    },
                                    label = { Text("Biaya Packaging (Rp)") },
                                    supportingText = { Text("Biaya kemasan/box per porsi") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Discount
                            OutlinedTextField(
                                value = discountStr,
                                onValueChange = {
                                    discountStr = it
                                    calculatorViewModel.setDiscountPct(it.toDoubleOrNull() ?: 0.0)
                                },
                                label = { Text("Diskon Promo (%)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Current platform result
                            if (calcState.platformFeePct > 0 || calcState.discountPct > 0 || calcState.packagingFee > 0) {
                                HorizontalDivider()
                                val platformName = selectedPlatform?.let { "${it.emoji} ${it.name}" } ?: "Platform"
                                Text("Hasil $platformName", fontWeight = FontWeight.Bold)
                                ResultRow("Harga Jual", currencyFormat.format(calcState.suggestedPrice))
                                if (calcState.discountPct > 0) {
                                    ResultRow("Setelah Diskon ${calcState.discountPct.toInt()}%", currencyFormat.format(calcState.priceAfterDiscount))
                                }
                                ResultRow("Komisi ${calcState.platformFeePct}%", "- ${currencyFormat.format(calcState.priceAfterDiscount * calcState.platformFeePct / 100.0)}")
                                if (calcState.packagingFee > 0) {
                                    ResultRow("Packaging", "- ${currencyFormat.format(calcState.packagingFee)}")
                                }
                                ResultRow("Pendapatan Bersih", currencyFormat.format(calcState.priceAfterFee))

                                val profitColor = if (calcState.profitAfterPromo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Profit per Porsi", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                    Text(currencyFormat.format(calcState.profitAfterPromo), fontWeight = FontWeight.Bold, color = profitColor)
                                }
                                if (calcState.profitAfterPromo < 0) {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("RUGI! Naikkan harga atau kurangi diskon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    // ===== COMPARISON TABLE =====
                    if (calcState.platformComparisons.isNotEmpty() && calcState.suggestedPrice > 0) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📊 Perbandingan Antar Platform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Profit per porsi di setiap platform (harga jual: ${currencyFormat.format(calcState.suggestedPrice)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))

                                calcState.platformComparisons.forEach { comparison ->
                                    val bgColor = when {
                                        comparison.isLoss -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        comparison.platform.name == "Langsung" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val profitColor = if (comparison.isLoss) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    val isSelected = MerchantPlatforms.platforms.indexOf(comparison.platform) == calcState.selectedPlatformIndex

                                    Surface(
                                        modifier = Modifier.fillMaxWidth().then(
                                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = bgColor
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("${comparison.platform.emoji} ${comparison.platform.name}", fontWeight = FontWeight.SemiBold)
                                                    if (isSelected) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text(
                                                    "Komisi: ${comparison.feePct}%" +
                                                        if (comparison.packagingFee > 0) " + Pkg ${currencyFormat.format(comparison.packagingFee)}" else "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    currencyFormat.format(comparison.profit),
                                                    fontWeight = FontWeight.Bold,
                                                    color = profitColor
                                                )
                                                Text(
                                                    if (comparison.isLoss) "RUGI" else "profit",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = profitColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
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
