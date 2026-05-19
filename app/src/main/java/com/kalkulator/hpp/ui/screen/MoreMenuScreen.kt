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

data class MoreMenuItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(onNavigate: (String) -> Unit) {
    val menuItems = listOf(
        MoreMenuItem("equipment", "Biaya Alat & Peralatan", "Depresiasi peralatan dapur", Icons.Default.Build),
        MoreMenuItem("overhead", "Biaya Overhead", "Sewa, listrik, internet, dll", Icons.Default.Receipt),
        MoreMenuItem("history", "Riwayat & Arsip", "Semua perhitungan tersimpan", Icons.Default.History),
        MoreMenuItem("templates", "Template Resep", "Resep siap pakai dari bawaan", Icons.Default.AutoAwesome),
        MoreMenuItem("settings", "Pengaturan", "Tema, backup, produksi harian", Icons.Default.Settings),
        MoreMenuItem("guide", "Panduan Penggunaan", "Cara pakai aplikasi step-by-step", Icons.Default.HelpOutline)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lainnya") },
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
            items(menuItems) { item ->
                ElevatedCard(
                    Modifier.fillMaxWidth().clickable { onNavigate(item.route) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.SemiBold)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
