# DEV_PROGRESS.md — Kalkulator HPP
> Dokumen progres aktif dan handoff resmi antar-agent/session.
> **Selalu baca SYSTEM_MAP.md terlebih dahulu, baru dokumen ini.**
> **Perbarui dokumen ini setiap ada perubahan signifikan — jangan tunggu akhir sesi.**

---

## 📌 Info Proyek

| Field | Value |
|-------|-------|
| Repo | `https://github.com/dannycawan/kalkulator-hpp` |
| Platform | Android (Kotlin + Jetpack Compose) |
| Package | `com.kalkulator.hpp` |
| Peta Arsitektur | [`SYSTEM_MAP.md`](./SYSTEM_MAP.md) |

---

## Active Task

Upload dan push semua perubahan (analisis fitur harga, implementasi UI baru, GuideScreen) ke GitHub.

Task sebelumnya: Tambah fitur panduan penggunaan (GuideScreen) agar user mudah memahami cara pakai aplikasi.

---

## Current Status

**Status:** `Completed` — Build CI GitHub Actions berhasil (commit `c59c444`). Semua perubahan sudah di-push dan terverifikasi.

Fix yang dilakukan: `ElevatedCard(onClick=...)` di `GuideScreen.kt` diganti dengan `Modifier.clickable` karena versi Material3 project tidak mendukung `ElevatedCard` clickable.

---

## What Has Been Confirmed

Fakta teknis yang sudah diverifikasi langsung dari kode dan config:

- **Entrypoint:** `AppModule.onCreate()` (Application class, manual DI) → `MainActivity.onCreate()` → `NavGraph()` (Jetpack Compose)
- **Arsitektur:** MVVM + Repository pattern + manual DI (tanpa Hilt/Koin)
- **Database:** Room v2, file `kalkulator_hpp.db`, 6 tabel, `fallbackToDestructiveMigration()` aktif
- **Core formula HPP:** `HppCalculator.kt` — pure Kotlin object, tidak ada Android dependency
- **Navigasi:** 5 bottom nav + 6 secondary screen, semua route terdefinisi di `NavGraph.kt` via `sealed class Screen`
- **Settings storage:** DataStore Preferences (`settingsDataStore`), 4 key: `dark_mode`, `follow_system_theme`, `daily_production`, `currency_symbol`
- **AdMob:** Saat ini menggunakan TEST IDs (`ca-app-pub-3940256099942544~...`) — belum production
- **Signing:** Release build dikonfigurasi menggunakan debug keystore
- **CI:** GitHub Actions (`android-build.yml`) — build APK + AAB dari branch `main`, menggunakan Gradle 8.7 + JDK 17
- **Depresiasi equipment:** dihitung per bulan: `(purchasePrice - residualValue) / usefulLifeMonths`, di-inject ke CalculatorScreen dari NavGraph
- **Export:** CSV via OpenCSV 5.9, PDF via iText7 7.2.5, Backup via SAF ContentResolver
- **Platform comparison:** 7 platform hardcoded di `MerchantPlatforms` object dalam `CalculatorViewModel.kt`

---

## Work Completed

- [x] Buat `SYSTEM_MAP.md` + `DEV_PROGRESS.md`
- [x] Analisis fitur rekomendasi harga — 9 masalah, 12 rekomendasi
- [x] Implementasi perbaikan fitur harga (HppCalculator, CalculatorViewModel, CalculatorScreen 3 tab)
- [x] Buat `GuideScreen.kt` — 7 langkah + 5 FAQ
- [x] Tambah route `Screen.Guide` di `NavGraph.kt`
- [x] Tambah menu item "Panduan Penggunaan" di `MoreMenuScreen.kt`
- [x] Update `DEV_PROGRESS.md` (sesi ini)

---

## In Progress

Menunggu verifikasi build dan test manual di device/emulator oleh user.

---

## Next Exact Steps

1. **Verifikasi build** — jalankan `gradle assembleDebug` (Gradle cache lokal perlu di-clear dulu jika error). Alternatif: push ke GitHub dan cek CI pipeline.
2. **Test manual di emulator/device** — verifikasi 3 tab berpindah dengan benar, semua nilai hitungan sama, platform terbaik ter-highlight.
3. **Jika ada compile error** — cek import `IngredientWithQuantity` di InputTab (menggunakan fully-qualified name, harus di-verify).
4. **Risiko teknis selanjutnya** — R1 (Room migration), R2 (AdMob prod IDs), R3 (release keystore).

---

## Files Already Read

| File | Alasan Dibaca |
|------|--------------|
| `MainActivity.kt` | Entrypoint Activity, setup theme & AdMob |
| `NavGraph.kt` | Seluruh routing, VM instantiation, bottom nav config |
| `di/AppModule.kt` | DI container, DB init, semua repository init, DataStore keys |
| `domain/model/HppCalculator.kt` | Core formula HPP — full read, semua fungsi |
| `ui/viewmodel/CalculatorViewModel.kt` | State management, recalculate flow, BEP, platform comparison — full read |
| `ui/screen/CalculatorScreen.kt` | UI layout fitur harga — full read (410 baris) untuk analisis UX |
| `data/local/AppDatabase.kt` | Schema Room, entitas, DAOs |
| `data/local/entity/CalculationResult.kt` | Skema tabel hasil kalkulasi |
| `data/local/entity/Recipe.kt` | Skema tabel resep |
| `data/local/entity/Equipment.kt` | Skema tabel alat, formula depresiasi |
| `ads/AdMobManager.kt` | Wrapper banner + interstitial AdMob |
| `util/BackupUtil.kt` | Backup/restore DB via SAF |
| `app/build.gradle.kts` | Versi SDK, dependencies, signing config, AdMob IDs |
| `settings.gradle.kts` | Plugin management, repo sources (JitPack) |
| `app/src/main/AndroidManifest.xml` | Permissions, Application class, AdMob meta-data |
| `.github/workflows/android-build.yml` | CI pipeline steps |

File yang **belum** dibaca detail:
`DashboardScreen.kt`, `IngredientScreen.kt`, `RecipeScreen.kt`, `RecipeDetailScreen.kt`, `HistoryScreen.kt`, `EquipmentScreen.kt`, `OverheadScreen.kt`, `SettingsScreen.kt`, `TemplateScreen.kt`, `MoreMenuScreen.kt`, semua DAO files, semua Repository files, `CsvUtil.kt`, `PdfUtil.kt`, `TemplateData.kt`, `Theme.kt`, `IngredientWithQuantity.kt`

---

## Files Modified

| File | Perubahan |
|------|----------|
| `SYSTEM_MAP.md` *(baru)* | Dibuat dari nol — peta arsitektur 11 bagian |
| `DEV_PROGRESS.md` *(diperbarui)* | Format mandatory, diperbarui 3x sesi |
| `analisis_fitur_harga.md` *(baru, brain dir)* | Hasil analisis UX — 9 masalah, 12 rekomendasi |
| `HppCalculator.kt` *(dimodifikasi)* | +header doc, +`roundUpToNearest()`, +`actualMarginPct()`, +catatan margin vs markup |
| `CalculatorViewModel.kt` *(ditulis ulang)* | +header doc, +7 field state baru, +`setDailyProduction()`, update `recalculate()` |
| `CalculatorScreen.kt` *(ditulis ulang)* | Refactor ke 3 tab: InputTab, HargaTab, PlatformTab. 506 baris |
| `GuideScreen.kt` *(baru)* | Panduan penggunaan 7 langkah + 5 FAQ expandable |
| `NavGraph.kt` *(dimodifikasi)* | +`Screen.Guide` data object, +`composable(Screen.Guide.route)` |
| `MoreMenuScreen.kt` *(dimodifikasi)* | +menu item "Panduan Penggunaan" dengan route `guide` |

**Tidak ada perubahan pada DB, DAO, Repository, Entity, DI.**

---

## Important Functions / Flows Touched

| Flow / Fungsi | File | Catatan |
|---------------|------|---------|
| `AppModule.onCreate()` | `di/AppModule.kt` | DI root — inisialisasi semua singleton |
| `MainActivity.onCreate()` | `MainActivity.kt` | Theme resolver + AdMob init + NavGraph launch |
| `NavGraph()` | `NavGraph.kt` | Seluruh VM di-instantiate di sini via `viewModel(factory=...)` |
| `HppCalculator.hpp()` | `domain/model/HppCalculator.kt` | Formula inti: `(material + labor + depr + overhead) / yield` |
| `HppCalculator.suggestedPrice()` | `domain/model/HppCalculator.kt` | `hpp / (1 - marginPct/100)` — **margin, bukan markup** |
| `CalculatorViewModel.recalculate()` | `ui/viewmodel/CalculatorViewModel.kt` | Dipanggil setiap input berubah; hitung HPP + BEP + promo + platform compare |
| `CalculatorViewModel.saveCalculation()` | `ui/viewmodel/CalculatorViewModel.kt` | Insert ke `calculation_results` via Repository |
| `CalculatorScreen` (UI layout) | `ui/screen/CalculatorScreen.kt` | 6 kartu input + 4 kartu output + simulasi platform + tabel comparison — satu scroll panjang |
| BEP formula | `CalculatorViewModel.kt:122-124` | `(depresiasi + overhead) / (harga - hpp)` — tenaga kerja TIDAK masuk BEP |
| Multi-margin cards | `CalculatorScreen.kt:192-202` | Hardcoded 30/40/50% — tanpa label konteks bisnis |
| Platform comparison table | `CalculatorScreen.kt:330-394` | 7 platform, profit per porsi, highlight merah jika rugi |
| `BackupUtil.backupDatabase()` | `util/BackupUtil.kt` | Copy file `.db` via ContentResolver — tanpa WAL checkpoint |
| `Equipment.monthlyDepreciation` | `data/local/entity/Equipment.kt` | `(purchasePrice - residualValue) / usefulLifeMonths` |

---

## Decisions Made

| Keputusan | Alasan |
|-----------|--------|
| Struktur 3 tab (bukan satu scroll) | Mengurangi cognitive load; user hanya lihat info relevan per tab |
| Tab vs step wizard | Tab lebih fleksibel, user bisa loncat bebas; step wizard terlalu linear |
| Formula tetap MARGIN (bukan markup) | Tidak mengubah business logic — hanya menambah label penjelasan |
| BEP hanya dari fixed cost | Tenaga kerja tetap dikategorikan variable cost (sesuai implementasi lama) |
| Custom disembunyikan dari tabel comparison | Custom tidak bermakna di tabel kecuali user mengisinya |
| Harga psikologis = roundUpToNearest 500 | Standar IDR, kelipatan Rp 500 paling umum di UMKM kuliner |
| Peringatan margin tipis jika < 15% | Di bawah 15% sangat rentan rugi setelah potongan platform |
| Header doc wajib di semua file yang diubah | Aturan ditetapkan user — sudah diimplementasikan di 3 file |

---

## Errors / Blockers

Tidak ada error atau blocker aktif saat ini.

**Risiko yang teridentifikasi (belum menjadi blocker aktif):**

| ID | Risiko | Severity |
|----|--------|----------|
| R1 | `fallbackToDestructiveMigration()` — data hilang saat schema berubah | 🔴 Kritis |
| R2 | AdMob TEST IDs masih terpasang di build config | 🔴 Kritis |
| R3 | Release build pakai debug keystore | 🔴 Kritis |
| R4 | BackupUtil tidak tutup/checkpoint Room WAL sebelum copy | 🟡 Sedang |
| R5 | TemplateData.kt hardcoded — tidak bisa remote-update | 🟡 Sedang |
| R6 | BEP formula: laborCost tidak dihitung sebagai fixed cost | 🟡 Sedang |
| R7 | CalculatorScreen.kt terlalu besar (25,6 KB) | 🟡 Sedang |
| R8 | Room `exportSchema = false` — tidak ada versi kontrol skema | 🟡 Sedang |
| R9 | VM scope di NavGraph (bukan Activity scope) | 🟢 Minor |
| R10 | `isMinifyEnabled = false` di release build | 🟢 Minor |
| R11 | HppCalculator.kt tanpa unit test coverage | 🟢 Minor |
| R12 | Tidak ada offline feedback ke user | 🟢 Minor |

---

## Validation Status

| Check | Status | Catatan |
|-------|--------|---------|
| Build (CI / GitHub Actions) | ✅ Success | Commit `c59c444`, conclusion: success |
| Unit Test | ❌ Not run | Belum ada eksekusi test |
| Lint | ❌ Not run | Belum dijalankan |
| Manual Check (device/emulator) | ❌ Not run | Menunggu user |

---

## Do Not Repeat

- **Jangan scan ulang seluruh struktur folder** — sudah dipetakan lengkap di SYSTEM_MAP.md §11
- **Jangan baca ulang semua file dari awal** — gunakan SYSTEM_MAP.md untuk menentukan titik mulai
- **Jangan asumsi DB migration sudah ada** — sudah dikonfirmasi: hanya `fallbackToDestructiveMigration()`, tidak ada migrasi manual
- **Jangan asumsi AdMob sudah production** — sudah dikonfirmasi: masih TEST IDs di `build.gradle.kts:19-21`
- **Jangan asumsi ada test coverage** — belum ada unit test untuk `HppCalculator.kt` atau bagian manapun
- **Jangan re-analisis CalculatorScreen dari awal** — sudah dianalisis lengkap, hasil ada di `analisis_fitur_harga.md`
- **Jangan asumsikan formula pakai markup** — sudah dikonfirmasi: formula menggunakan MARGIN (berbasis harga jual)
- **Jangan asumsikan tenaga kerja masuk BEP** — sudah dikonfirmasi: BEP hanya dari fixed cost (depresiasi + overhead)

---

## Resume Note for Next Agent

Build CI berhasil setelah fix compile error di `GuideScreen.kt` (`ElevatedCard` onClick tidak kompatibel → diganti `Modifier.clickable`). Semua fitur (perbaikan rekomendasi harga 3 tab + panduan penggunaan) sudah terpush dan terverifikasi di GitHub Actions. Selanjutnya: test manual di emulator, lalu risiko teknis (R1/R2/R3).

---

*Last updated: 2026-05-20 — Session 5 (Fix CI Build + Push)*
