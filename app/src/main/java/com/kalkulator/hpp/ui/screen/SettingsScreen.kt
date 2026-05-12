package com.kalkulator.hpp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkulator.hpp.ui.viewmodel.SettingsViewModel
import com.kalkulator.hpp.util.BackupUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val followSystem by viewModel.followSystem.collectAsState()
    val dailyProduction by viewModel.dailyProduction.collectAsState()
    var dailyProdStr by remember(dailyProduction) { mutableStateOf(dailyProduction.toString()) }
    val context = LocalContext.current
    var snackMessage by remember { mutableStateOf<String?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            try {
                BackupUtil.backupDatabase(context, it)
                snackMessage = "Backup berhasil disimpan!"
            } catch (e: Exception) {
                snackMessage = "Gagal backup: ${e.message}"
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                BackupUtil.restoreDatabase(context, it)
                snackMessage = "Restore berhasil! Restart app untuk melihat data."
            } catch (e: Exception) {
                snackMessage = "Gagal restore: ${e.message}"
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance
            Text("Tampilan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brightness6, null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Ikuti Tema Sistem", Modifier.weight(1f))
                        Switch(checked = followSystem, onCheckedChange = { viewModel.setFollowSystem(it) })
                    }
                    if (!followSystem) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Mode Gelap", Modifier.weight(1f))
                            Switch(checked = isDarkMode, onCheckedChange = { viewModel.setDarkMode(it) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Production
            Text("Produksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Estimasi Produksi Harian", style = MaterialTheme.typography.bodyMedium)
                    Text("Digunakan untuk menghitung overhead & depresiasi per porsi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dailyProdStr,
                        onValueChange = {
                            dailyProdStr = it
                            it.toIntOrNull()?.let { v -> if (v > 0) viewModel.setDailyProduction(v) }
                        },
                        label = { Text("Jumlah porsi/hari") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Backup & Restore
            Text("Backup & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data disimpan di SQLite lokal. Backup untuk keamanan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { backupLauncher.launch("kalkulator_hpp_backup.db") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Backup Data")
                    }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Restore dari Backup")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // About
            Text("Tentang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Kalkulator HPP", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Versi 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Aplikasi perhitungan Harga Pokok Produksi untuk UMKM kuliner.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("✓ Offline 100% — tanpa akun, tanpa cloud", style = MaterialTheme.typography.bodySmall)
                    Text("✓ Data tersimpan lokal di HP", style = MaterialTheme.typography.bodySmall)
                    Text("✓ Unlimited resep & data", style = MaterialTheme.typography.bodySmall)
                    Text("✓ Gratis selamanya", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(12.dp))
                    Text("Tutorial Singkat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("1. Tambahkan bahan baku di menu Bahan", style = MaterialTheme.typography.bodySmall)
                    Text("2. Buat resep dan tambahkan bahan ke resep", style = MaterialTheme.typography.bodySmall)
                    Text("3. Tambahkan biaya alat & overhead", style = MaterialTheme.typography.bodySmall)
                    Text("4. Buka Kalkulator HPP untuk hitung harga", style = MaterialTheme.typography.bodySmall)
                    Text("5. Lihat riwayat perhitungan di Riwayat", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
