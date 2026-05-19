/*
 * Tujuan: Panduan penggunaan aplikasi Kalkulator HPP — layar bantuan step-by-step
 * Caller: NavGraph.kt composable(Screen.Guide.route), akses via MoreMenuScreen
 * Dependensi: Tidak ada dependensi data/repository
 * Main Functions: GuideScreen(), GuideSection(), GuideTip()
 * Side Effects: -
 */
package com.kalkulator.hpp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panduan Penggunaan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
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
            // Header
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Selamat datang di Kalkulator HPP!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aplikasi ini membantu kamu menghitung Harga Pokok Produksi (HPP) " +
                                "dan menentukan harga jual yang tepat untuk usaha kuliner.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Alur Penggunaan
            Text(
                "Alur Penggunaan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ikuti langkah-langkah berikut untuk menghitung HPP dan harga jual:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Step 1
            GuideSection(
                stepNumber = 1,
                title = "Tambahkan Bahan Baku",
                icon = Icons.Default.Inventory2,
                description = "Buka tab Bahan di menu bawah. Tambahkan semua bahan baku yang kamu gunakan " +
                        "lengkap dengan harga per satuan (per kg, per liter, dll).",
                tips = listOf(
                    "Gunakan harga beli terbaru agar HPP akurat",
                    "Pisahkan bahan per satuan yang berbeda (misal: gula pasir per kg, gula cair per liter)"
                )
            )

            // Step 2
            GuideSection(
                stepNumber = 2,
                title = "Buat Resep Produk",
                icon = Icons.Default.MenuBook,
                description = "Buka tab Resep. Buat resep baru dengan nama produk, lalu tambahkan bahan-bahan " +
                        "dari daftar bahan yang sudah kamu input beserta jumlah yang dipakai per resep.",
                tips = listOf(
                    "Satu resep = satu produk yang kamu jual",
                    "Isi jumlah yield (porsi) yang dihasilkan dari satu resep",
                    "Tambahkan biaya tenaga kerja langsung di halaman resep"
                )
            )

            // Step 3
            GuideSection(
                stepNumber = 3,
                title = "Catat Alat & Overhead (opsional)",
                icon = Icons.Default.Build,
                description = "Di menu Lainnya → Biaya Alat & Peralatan, catat semua peralatan yang kamu beli " +
                        "(oven, blender, dll) beserta harga dan umur ekonomisnya. " +
                        "Lalu di Biaya Overhead, catat biaya tetap bulanan (sewa, listrik, gas, internet).",
                tips = listOf(
                    "Depresiasi alat dihitung otomatis: (Harga Beli - Nilai Sisa) ÷ Umur Bulan",
                    "Overhead per porsi dihitung otomatis berdasarkan produksi harian di Pengaturan",
                    "Langkah ini opsional tapi sangat direkomendasikan agar HPP lebih akurat"
                )
            )

            // Step 4
            GuideSection(
                stepNumber = 4,
                title = "Hitung HPP",
                icon = Icons.Default.Calculate,
                description = "Buka tab HPP di menu bawah. Pilih resep yang sudah dibuat. " +
                        "Biaya bahan, depresiasi, dan overhead akan terisi otomatis. " +
                        "Atur margin keuntungan yang kamu inginkan.",
                tips = listOf(
                    "HPP = (Bahan + Tenaga Kerja + Depresiasi + Overhead) ÷ Yield",
                    "Margin 30% artinya 30% dari harga jual, bukan dari HPP",
                    "Geser ke tab Harga untuk lihat rekomendasi lengkap"
                )
            )

            // Step 5
            GuideSection(
                stepNumber = 5,
                title = "Lihat Rekomendasi Harga",
                icon = Icons.Default.PriceCheck,
                description = "Di tab Harga, kamu akan melihat rekomendasi harga jual berdasarkan segmen pasar " +
                        "dan harga psikologis yang sudah dibulatkan.",
                tips = listOf(
                    "Warung/Offline (30%) — margin standar pasar kuliner",
                    "Online/GoFood (40%) — lebih tinggi karena ada potongan komisi platform",
                    "Premium/Kafe (50%) — untuk produk bernilai tinggi",
                    "Harga Psikologis sudah dibulatkan ke Rp 500 terdekat ke atas"
                )
            )

            // Step 6
            GuideSection(
                stepNumber = 6,
                title = "Simulasi Platform (opsional)",
                icon = Icons.Default.Storefront,
                description = "Di tab Platform, simulasikan berapa profit bersih kamu di berbagai platform " +
                        "(GoFood, GrabFood, ShopeeFood, Tokopedia, Shopee) setelah dipotong komisi dan diskon promo.",
                tips = listOf(
                    "Platform terbaik akan otomatis ditandai dengan bintang ⭐",
                    "Jika profit minus (RUGI), naikkan harga atau kurangi diskon",
                    "Gunakan fitur ini sebelum ikut promo untuk cek apakah masih untung"
                )
            )

            // Step 7
            GuideSection(
                stepNumber = 7,
                title = "Simpan & Ekspor",
                icon = Icons.Default.Save,
                description = "Tekan tombol Simpan Hasil untuk menyimpan perhitungan ke riwayat. " +
                        "Kamu bisa melihat semua riwayat di menu Lainnya → Riwayat & Arsip.",
                tips = listOf(
                    "Riwayat berguna untuk membandingkan HPP antar waktu",
                    "Backup data secara berkala di Pengaturan → Backup Database"
                )
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // FAQ Section
            Text(
                "Pertanyaan Umum (FAQ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FaqItem(
                question = "Apa bedanya Margin dan Markup?",
                answer = "Margin dihitung dari harga jual: Harga = HPP ÷ (1 - Margin/100). " +
                        "Markup dihitung dari HPP: Harga = HPP × (1 + Markup/100). " +
                        "Aplikasi ini menggunakan Margin. Contoh: margin 30% berarti harga = HPP ÷ 0.7"
            )

            FaqItem(
                question = "Kenapa BEP hanya menghitung depresiasi dan overhead?",
                answer = "BEP (Break Even Point) menghitung berapa porsi yang perlu dijual untuk " +
                        "menutupi biaya tetap. Biaya bahan dan tenaga kerja dianggap sebagai biaya variabel " +
                        "yang sudah masuk ke HPP per unit."
            )

            FaqItem(
                question = "Bagaimana cara mengubah produksi harian?",
                answer = "Buka Lainnya → Pengaturan → Produksi Harian. " +
                        "Angka ini dipakai untuk menghitung depresiasi dan overhead per porsi, " +
                        "serta estimasi hari pada BEP."
            )

            FaqItem(
                question = "Data saya aman tidak?",
                answer = "Semua data tersimpan lokal di perangkat kamu. Untuk keamanan, " +
                        "lakukan backup berkala di Pengaturan → Backup Database. " +
                        "File backup bisa disimpan ke Google Drive atau penyimpanan lain."
            )

            FaqItem(
                question = "Apa itu Harga Psikologis?",
                answer = "Harga yang dibulatkan ke angka yang lebih menarik bagi pembeli. " +
                        "Misalnya Rp 14.286 dibulatkan menjadi Rp 14.500. " +
                        "Pembulatan ke kelipatan Rp 500 ke atas agar kamu tidak rugi."
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GuideSection(
    stepNumber: Int,
    title: String,
    icon: ImageVector,
    description: String,
    tips: List<String> = emptyList()
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Step number badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$stepNumber",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            Text(description, style = MaterialTheme.typography.bodyMedium)

            if (tips.isNotEmpty()) {
                Surface(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💡 Tips:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                        tips.forEach { tip ->
                            Text("• $tip", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(question, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
