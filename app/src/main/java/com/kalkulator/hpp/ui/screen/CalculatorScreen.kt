/*
 * Tujuan: UI kalkulator HPP â€” 3 tab: Input, Harga & Rekomendasi, Simulasi Platform
 * Caller: NavGraph.kt composable(Screen.Calculator.route)
 * Dependensi: CalculatorViewModel, RecipeViewModel, MerchantPlatforms
 * Main Functions: CalculatorScreen(), InputTab(), HargaTab(), PlatformTab()
 * Side Effects: Read RecipeIngredients via RecipeViewModel; Write via CalculatorViewModel.saveCalculation()
 */
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
import androidx.compose.material.icons.filled.*
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
    val fmt = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var laborStr by remember { mutableStateOf("0") }
    var depStr by remember { mutableStateOf("0") }
    var ovhStr by remember { mutableStateOf("0") }
    var yieldStr by remember { mutableStateOf("1") }
    var marginStr by remember { mutableStateOf("30") }
    var discountStr by remember { mutableStateOf("0") }
    var feeStr by remember { mutableStateOf("0") }
    var pkgStr by remember { mutableStateOf("0") }
    var selectedTab by remember { mutableStateOf(0) }

    val autoDep = if (dailyProduction > 0) totalMonthlyDepreciation / (dailyProduction * 30.0) else 0.0
    val autoOvh = if (dailyProduction > 0) totalMonthlyOverhead / (dailyProduction * 30.0) else 0.0

    LaunchedEffect(dailyProduction) { calculatorViewModel.setDailyProduction(dailyProduction) }
    LaunchedEffect(selectedRecipeId) { selectedRecipeId?.let { recipeViewModel.selectRecipe(it) } }
    LaunchedEffect(recipeIngredients, recipeViewModel.selectedRecipe.collectAsState().value) {
        val r = recipeViewModel.selectedRecipe.value ?: return@LaunchedEffect
        calculatorViewModel.setRecipeName(r.name)
        calculatorViewModel.setCategory(r.category)
        calculatorViewModel.setIngredients(recipeIngredients)
        calculatorViewModel.setLaborCost(r.laborCost)
        calculatorViewModel.setYield(r.yield)
        laborStr = r.laborCost.toLong().toString()
        yieldStr = r.yield.toString()
        depStr = autoDep.toLong().toString()
        ovhStr = autoOvh.toLong().toString()
        calculatorViewModel.setDepreciationCost(autoDep)
        calculatorViewModel.setOverheadCost(autoOvh)
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
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text(if (calcState.saved) "Tersimpan âœ“" else "Simpan Hasil") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Selector resep â€” selalu terlihat
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = recipes.find { it.id == selectedRecipeId }?.name ?: "Pilih resep...",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Resep / Produk") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        recipes.forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { selectedRecipeId = r.id; expanded = false })
                        }
                    }
                }
            }

            if (selectedRecipeId != null) {
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("â‘  Input", "â‘¡ Harga", "â‘¢ Platform").forEachIndexed { i, t ->
                        Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                            text = { Text(t, style = MaterialTheme.typography.labelMedium) })
                    }
                }
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> InputTab(
                            calcState = calcState, recipeIngredients = recipeIngredients, fmt = fmt,
                            autoDepreciation = autoDep, autoOverhead = autoOvh,
                            laborStr = laborStr, onLaborChange = { laborStr = it; calculatorViewModel.setLaborCost(it.toDoubleOrNull() ?: 0.0) },
                            depStr = depStr, onDepChange = { depStr = it; calculatorViewModel.setDepreciationCost(it.toDoubleOrNull() ?: 0.0) },
                            ovhStr = ovhStr, onOvhChange = { ovhStr = it; calculatorViewModel.setOverheadCost(it.toDoubleOrNull() ?: 0.0) },
                            yieldStr = yieldStr, onYieldChange = { yieldStr = it; calculatorViewModel.setYield(it.toIntOrNull() ?: 1) },
                            marginStr = marginStr, onMarginChange = { marginStr = it; calculatorViewModel.setMarginPct(it.toDoubleOrNull() ?: 30.0) },
                            onGoToHarga = { selectedTab = 1 }
                        )
                        1 -> HargaTab(calcState = calcState, fmt = fmt, dailyProduction = dailyProduction)
                        2 -> PlatformTab(
                            calcState = calcState, fmt = fmt,
                            feeStr = feeStr, onFeeChange = { feeStr = it; calculatorViewModel.setPlatformFeePct(it.toDoubleOrNull() ?: 0.0) },
                            pkgStr = pkgStr, onPkgChange = { pkgStr = it; calculatorViewModel.setPackagingFee(it.toDoubleOrNull() ?: 0.0) },
                            discountStr = discountStr, onDiscountChange = { discountStr = it; calculatorViewModel.setDiscountPct(it.toDoubleOrNull() ?: 0.0) },
                            onSelectPlatform = { idx ->
                                calculatorViewModel.selectPlatform(idx)
                                val p = MerchantPlatforms.platforms.getOrNull(idx)
                                feeStr = p?.defaultFeePct?.toString() ?: "0"
                                pkgStr = p?.packagingFeeDefault?.toLong()?.toString() ?: "0"
                            }
                        )
                    }
                    Spacer(Modifier.height(72.dp))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pilih resep untuk mulai menghitung HPP",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// -----------------------------------------------------------
// TAB 1 — INPUT BIAYA
// -----------------------------------------------------------
@Composable
private fun InputTab(
    calcState: com.kalkulator.hpp.ui.viewmodel.CalculatorUiState,
    recipeIngredients: List<com.kalkulator.hpp.domain.model.IngredientWithQuantity>,
    fmt: NumberFormat,
    autoDepreciation: Double, autoOverhead: Double,
    laborStr: String, onLaborChange: (String) -> Unit,
    depStr: String, onDepChange: (String) -> Unit,
    ovhStr: String, onOvhChange: (String) -> Unit,
    yieldStr: String, onYieldChange: (String) -> Unit,
    marginStr: String, onMarginChange: (String) -> Unit,
    onGoToHarga: () -> Unit
) {
    // Kartu Bahan Baku
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("? Biaya Bahan Baku", fontWeight = FontWeight.Bold)
            if (recipeIngredients.isEmpty()) {
                Text("Belum ada bahan. Tambahkan di tab Resep.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                recipeIngredients.forEach { ing ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text("${ing.name} (${ing.quantity} ${ing.unit})", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text(fmt.format(ing.quantity * ing.pricePerUnit), style = MaterialTheme.typography.bodySmall)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Total Bahan", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(fmt.format(calcState.totalMaterialCost), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Kartu Tenaga Kerja
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("? Tenaga Kerja Langsung", fontWeight = FontWeight.Bold)
            OutlinedTextField(laborStr, onLaborChange, label = { Text("Biaya Tenaga Kerja (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }

    // Kartu Depresiasi
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("? Depresiasi Alat", fontWeight = FontWeight.Bold)
            Text("Auto-fill dari data alat (${fmt.format(autoDepreciation)}/porsi)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(depStr, onDepChange, label = { Text("Depresiasi per Porsi (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }

    // Kartu Overhead
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("? Biaya Overhead", fontWeight = FontWeight.Bold)
            Text("Auto-fill dari data overhead (${fmt.format(autoOverhead)}/porsi)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(ovhStr, onOvhChange, label = { Text("Overhead per Porsi (Rp)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }

    // Yield & Margin
    OutlinedTextField(yieldStr, onYieldChange, label = { Text("Yield (jumlah produk)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        value = marginStr, onValueChange = onMarginChange,
        label = { Text("Margin Keuntungan (%)") },
        supportingText = { Text("% dari harga jual, bukan dari HPP. Contoh: margin 30% ? harga = HPP ÷ 0,7") },
        singleLine = true, modifier = Modifier.fillMaxWidth()
    )

    // Tombol lihat hasil
    if (calcState.hppPerUnit > 0) {
        Button(onClick = onGoToHarga, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Lihat Rekomendasi Harga ?")
        }
    }
}

// -----------------------------------------------------------
// TAB 2 — HARGA & REKOMENDASI
// -----------------------------------------------------------
@Composable
private fun HargaTab(
    calcState: com.kalkulator.hpp.ui.viewmodel.CalculatorUiState,
    fmt: NumberFormat,
    dailyProduction: Int
) {
    if (calcState.hppPerUnit <= 0) {
        Card(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Isi data biaya di tab Input terlebih dahulu.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    // Kartu Ringkasan HPP
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Hasil Perhitungan HPP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            HorizontalDivider()
            val totalBiaya = calcState.totalMaterialCost + calcState.laborCost + calcState.depreciationCost + calcState.overheadCost
            ResultRow("Bahan Baku", fmt.format(calcState.totalMaterialCost))
            ResultRow("Tenaga Kerja", fmt.format(calcState.laborCost))
            ResultRow("Depresiasi Alat", fmt.format(calcState.depreciationCost))
            ResultRow("Overhead", fmt.format(calcState.overheadCost))
            ResultRow("Total Biaya Produksi", fmt.format(totalBiaya))
            ResultRow("Yield", "${calcState.yield} unit")
            HorizontalDivider()
            Row(Modifier.fillMaxWidth()) {
                Text("HPP per Unit", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(fmt.format(calcState.hppPerUnit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Peringatan margin tipis
    if (calcState.marginThin) {
        Surface(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.errorContainer) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Margin ${calcState.marginPct.toInt()}% sangat tipis! Rentan rugi setelah potongan platform atau diskon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Kartu Rekomendasi Harga Jual
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Rekomendasi Harga Jual", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()

            // Harga Minimum
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.errorContainer.copy(0.4f)).padding(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("?? Harga Minimum (HPP)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Text("Batas bawah mutlak — jangan jual di bawah ini", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(fmt.format(calcState.minimumPrice), fontWeight = FontWeight.Bold)
            }

            // Harga Psikologis
            if (calcState.roundedPrice > 0) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.5f)).padding(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("?? Harga Psikologis", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text("Dibulatkan ke Rp 500 terdekat • margin aktual: ${"%.1f".format(calcState.roundedMarginPct)}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(fmt.format(calcState.roundedPrice), fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()
            Text("Pilih sesuai segmen pasar:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Tier Warung / Offline
            RecommendRow("?? Warung / Offline", "Margin 30% dari harga jual", calcState.price30, fmt, highlight = calcState.marginPct == 30.0)
            // Tier Online / GoFood
            RecommendRow("?? Online / GoFood", "Margin 40% • cocok setelah potongan komisi", calcState.price40, fmt, highlight = calcState.marginPct == 40.0)
            // Tier Premium / Kafe
            RecommendRow("? Premium / Kafe", "Margin 50% • untuk produk bernilai tinggi", calcState.price50, fmt, highlight = calcState.marginPct == 50.0)

            // Custom — hanya tampil jika tidak sama dengan 30/40/50
            val marginInt = calcState.marginPct.toInt()
            if (marginInt != 30 && marginInt != 40 && marginInt != 50 && calcState.suggestedPrice > 0) {
                RecommendRow("?? Kustom (${marginInt}%)", "Sesuai margin yang kamu set", calcState.suggestedPrice, fmt, highlight = true)
            }
        }
    }

    // Kartu BEP
    if (calcState.bepUnits > 0) {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Break Even Point (BEP)", fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Perlu jual ${calcState.bepUnits} porsi", fontWeight = FontWeight.SemiBold)
                        Text("untuk balik modal biaya tetap (depresiasi + overhead)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (calcState.bepDays > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiary) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("~${calcState.bepDays}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.titleMedium)
                                Text("hari", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiary)
                            }
                        }
                    }
                }
                if (dailyProduction > 0) Text("Asumsi produksi $dailyProduction porsi/hari (ubah di Pengaturan)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RecommendRow(label: String, sublabel: String, price: Double, fmt: NumberFormat, highlight: Boolean = false) {
    val bg = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(0.6f) else MaterialTheme.colorScheme.surface.copy(0.3f)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(bg).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodySmall)
            Text(sublabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(fmt.format(price), fontWeight = FontWeight.Bold, color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

// -----------------------------------------------------------
// TAB 3 — SIMULASI PLATFORM
// -----------------------------------------------------------
@Composable
private fun PlatformTab(
    calcState: com.kalkulator.hpp.ui.viewmodel.CalculatorUiState,
    fmt: NumberFormat,
    feeStr: String, onFeeChange: (String) -> Unit,
    pkgStr: String, onPkgChange: (String) -> Unit,
    discountStr: String, onDiscountChange: (String) -> Unit,
    onSelectPlatform: (Int) -> Unit
) {
    if (calcState.hppPerUnit <= 0) {
        Card(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Hitung HPP terlebih dahulu di tab Input.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("?? Simulasi Merchant Platform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Chip selector platform
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(MerchantPlatforms.platforms) { idx, p ->
                    val isBest = idx == calcState.bestPlatformIndex
                    FilterChip(
                        selected = calcState.selectedPlatformIndex == idx,
                        onClick = { onSelectPlatform(idx) },
                        label = {
                            Text(
                                if (isBest) "${p.emoji} ${p.name} ?" else "${p.emoji} ${p.name}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = if (calcState.selectedPlatformIndex == idx) {
                            { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            // Input komisi, packaging, diskon
            val selP = MerchantPlatforms.platforms.getOrNull(calcState.selectedPlatformIndex)
            OutlinedTextField(feeStr, onFeeChange, label = { Text("Komisi Platform (%)") },
                supportingText = if (selP != null && selP.name != "Custom" && selP.name != "Langsung") {
                    { Text("Default ${selP.name}: ${selP.defaultFeePct}%") }
                } else null,
                singleLine = true, modifier = Modifier.fillMaxWidth())

            if (selP?.hasPackagingFee == true || calcState.packagingFee > 0) {
                OutlinedTextField(pkgStr, onPkgChange, label = { Text("Biaya Packaging (Rp)") },
                    supportingText = { Text("Biaya kemasan/box per porsi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(discountStr, onDiscountChange, label = { Text("Diskon Promo (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            // Hasil platform terpilih
            if (calcState.platformFeePct > 0 || calcState.discountPct > 0 || calcState.packagingFee > 0) {
                HorizontalDivider()
                val pName = selP?.let { "${it.emoji} ${it.name}" } ?: "Platform"
                Text("Hasil $pName", fontWeight = FontWeight.Bold)
                ResultRow("Harga Jual", fmt.format(calcState.suggestedPrice))
                if (calcState.discountPct > 0) ResultRow("Setelah Diskon ${calcState.discountPct.toInt()}%", fmt.format(calcState.priceAfterDiscount))
                ResultRow("Komisi ${calcState.platformFeePct}%", "- ${fmt.format(calcState.priceAfterDiscount * calcState.platformFeePct / 100.0)}")
                if (calcState.packagingFee > 0) ResultRow("Packaging", "- ${fmt.format(calcState.packagingFee)}")
                ResultRow("Pendapatan Bersih", fmt.format(calcState.priceAfterFee))
                val profitColor = if (calcState.profitAfterPromo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Row(Modifier.fillMaxWidth()) {
                    Text("Profit per Porsi", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(fmt.format(calcState.profitAfterPromo), fontWeight = FontWeight.Bold, color = profitColor)
                }
                if (calcState.profitAfterPromo < 0) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.errorContainer).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RUGI! Naikkan harga atau kurangi diskon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // Tabel perbandingan semua platform (Custom disembunyikan)
    if (calcState.platformComparisons.isNotEmpty() && calcState.suggestedPrice > 0) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("?? Perbandingan Antar Platform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                // Summary platform terbaik
                if (calcState.bestPlatformIndex >= 0) {
                    val best = calcState.platformComparisons.getOrNull(calcState.bestPlatformIndex)
                    if (best != null && !best.isLoss) {
                        Surface(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Platform terbaik: ${best.platform.emoji} ${best.platform.name} — profit ${fmt.format(best.profit)}/porsi",
                                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Text("Profit per porsi di setiap platform (harga: ${fmt.format(calcState.suggestedPrice)})",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                calcState.platformComparisons.forEachIndexed { idx, comp ->
                    // Sembunyikan Custom dari tabel
                    if (comp.platform.name == "Custom") return@forEachIndexed

                    val isBest = idx == calcState.bestPlatformIndex
                    val isSelected = idx == calcState.selectedPlatformIndex
                    val bgColor = when {
                        comp.isLoss -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        isBest -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val profitColor = if (comp.isLoss) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                    Surface(
                        modifier = Modifier.fillMaxWidth().then(
                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier
                        ),
                        shape = RoundedCornerShape(12.dp), color = bgColor
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${comp.platform.emoji} ${comp.platform.name}", fontWeight = FontWeight.SemiBold)
                                    if (isBest) { Spacer(Modifier.width(4.dp)); Text("? Terbaik", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                                    if (isSelected) { Spacer(Modifier.width(4.dp)); Text("?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                                }
                                Text("Komisi: ${comp.feePct}%" + if (comp.packagingFee > 0) " + Pkg ${fmt.format(comp.packagingFee)}" else "",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(fmt.format(comp.profit), fontWeight = FontWeight.Bold, color = profitColor)
                                Text(if (comp.isLoss) "RUGI" else "profit", style = MaterialTheme.typography.labelSmall, color = profitColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------
// HELPER
// -----------------------------------------------------------
@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
