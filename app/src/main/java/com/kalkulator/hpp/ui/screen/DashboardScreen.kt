package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.ui.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRecipes: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val recipeCount by viewModel.recipeCount.collectAsState()
    val avgHpp by viewModel.avgHpp.collectAsState()
    val avgMargin by viewModel.avgMargin.collectAsState()
    val estimatedProfit by viewModel.estimatedMonthlyProfit.collectAsState()
    val topProfitable by viewModel.topProfitable.collectAsState()
    val topExpensive by viewModel.topExpensiveHpp.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kalkulator HPP", fontWeight = FontWeight.Bold)
                        Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Resep",
                    value = "$recipeCount",
                    icon = Icons.Default.MenuBook,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "HPP Rata-rata",
                    value = currencyFormat.format(avgHpp),
                    icon = Icons.Default.Calculate,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Margin Rata-rata",
                    value = "${String.format("%.1f", avgMargin)}%",
                    icon = Icons.Default.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Est. Profit/Bulan",
                    value = currencyFormat.format(estimatedProfit),
                    icon = Icons.Default.AccountBalance,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            // Quick Actions
            Text("Aksi Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    label = "Buat Resep",
                    onClick = onNavigateToRecipes
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Calculate,
                    label = "Hitung HPP",
                    onClick = onNavigateToCalculator
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Backup,
                    label = "Backup",
                    onClick = onNavigateToSettings
                )
            }

            // Top 5 Profitable
            if (topProfitable.isNotEmpty()) {
                Text("Top 5 Paling Menguntungkan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                topProfitable.forEachIndexed { index, item ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.recipeName, fontWeight = FontWeight.SemiBold)
                                Text("Profit: ${currencyFormat.format(item.suggestedPrice - item.hppPerUnit)}/porsi",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(currencyFormat.format(item.suggestedPrice),
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Top 5 Most Expensive HPP
            if (topExpensive.isNotEmpty()) {
                Text("Top 5 HPP Tertinggi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                topExpensive.forEachIndexed { index, item ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.recipeName, fontWeight = FontWeight.SemiBold)
                                Text("HPP: ${currencyFormat.format(item.hppPerUnit)}/porsi",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Empty state
            if (topProfitable.isEmpty() && topExpensive.isEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Mulai dengan menambahkan bahan baku dan resep", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("lalu hitung HPP untuk melihat ringkasan di sini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}
