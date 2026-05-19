# SYSTEM_MAP.md — Kalkulator HPP

> **Generated:** 2026-05-20 | **Platform:** Android (Kotlin + Jetpack Compose)  
> **Package:** `com.kalkulator.hpp` | **Min SDK:** 24 | **Target SDK:** 34 | **DB Version:** 2

---

## 1. Entrypoint & Boot Sequence

```
Android OS
  └── AppModule.onCreate()          ← Application class (manual DI container)
        ├── Room DB init             → kalkulator_hpp.db (v2, fallbackToDestructiveMigration)
        ├── Repository singletons   → 5x repos wired from DAO
        └── CsvUtil / PdfUtil init
              ↓
  └── MainActivity.onCreate()
        ├── AdMobManager init        → loads Banner + Interstitial
        ├── settingsDataStore        → reads theme prefs (DataStore)
        └── setContent {
              KalkulatorHPPTheme
                CompositionLocalProvider(LocalAdMobManager)
                  NavGraph(appModule)   ← seluruh navigasi & VM dimulai di sini
            }
```

**File:** `MainActivity.kt` → `AppModule.kt` → `NavGraph.kt`

---

## 2. Arsitektur Keseluruhan

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer                               │
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────────┐  │
│  │   Screen/    │  │  ViewModel    │  │  NavGraph.kt    │  │
│  │  Composable  │◄─│  (StateFlow)  │  │  (NavHost +     │  │
│  │  (ui/screen) │  │(ui/viewmodel) │  │  BottomNavBar)  │  │
│  └──────┬───────┘  └──────┬────────┘  └─────────────────┘  │
│         └────────────────┘                                  │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  HppCalculator.kt   (pure Kotlin, no Android deps)  │   │
│  │  IngredientWithQuantity.kt  |  TemplateData.kt       │   │
│  └─────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                     Data Layer                              │
│  ┌───────────────┐  ┌─────────────┐  ┌──────────────────┐  │
│  │  Repository   │  │  Room DAO   │  │ Entity / Schema  │  │
│  │  (5 repos)    │──│  (6 DAOs)   │──│ (6 tables)       │  │
│  └───────────────┘  └─────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure                            │
│  AdMobManager  |  BackupUtil  |  CsvUtil  |  PdfUtil        │
│  DataStore (settings)  |  AppModule (DI)                    │
└─────────────────────────────────────────────────────────────┘
```

**Pattern:** MVVM + Repository + manual DI (no Hilt/Koin)

---

## 3. Navigasi & Modul UI

### Bottom Navigation (5 tab utama)

| Tab | Route | Screen | ViewModel |
|-----|-------|--------|-----------|
| Home | `dashboard` | `DashboardScreen` | `DashboardViewModel` |
| Bahan | `ingredients` | `IngredientScreen` | `IngredientViewModel` |
| Resep | `recipes` | `RecipeScreen` | `RecipeViewModel` |
| HPP | `calculator` | `CalculatorScreen` | `CalculatorViewModel` |
| Lainnya | `more` | `MoreMenuScreen` | — |

### Secondary Screens (tidak di bottom nav)

| Route | Screen | ViewModel | Akses dari |
|-------|--------|-----------|------------|
| `equipment` | `EquipmentScreen` | `EquipmentViewModel` | More menu |
| `overhead` | `OverheadScreen` | `OverheadViewModel` | More menu |
| `history` | `HistoryScreen` | `HistoryViewModel` | More menu |
| `templates` | `TemplateScreen` | — | More menu |
| `settings` | `SettingsScreen` | `SettingsViewModel` | Dashboard / More |
| `recipe/{recipeId}` | `RecipeDetailScreen` | `RecipeViewModel` + `IngredientViewModel` | RecipeScreen |

---

## 4. Flow Kalkulator HPP (Core Business Logic)

```
User Input (CalculatorScreen.kt)
  │
  ├── Pilih Resep / Input Manual
  │     ├── Ingredients list  ──────┐
  │     ├── Labor cost              │
  │     ├── Depreciation cost  ─────┤──► CalculatorViewModel.recalculate()
  │     ├── Overhead cost           │         │
  │     └── Yield (porsi)      ─────┘         ▼
  │                                    HppCalculator.materialCost()
  │                                    = Σ (pricePerUnit × qty)
  │                                         │
  │                                    HppCalculator.hpp()
  │                                    = (material + labor + depr + overhead) / yield
  │                                         │
  │                                    HppCalculator.suggestedPrice()
  │                                    = hpp / (1 - marginPct/100)
  │                                         │
  │                                   ┌─────┴──────────────────────────────┐
  │                                   │  Multi-Margin: 30%, 40%, 50%        │
  │                                   │  BEP: fixedCost / (price - hpp) + 1 │
  │                                   │  Promo Sim: diskon % + platform fee  │
  │                                   │  Platform Compare: 7 platform        │
  │                                   └─────────────────────────────────────┘
  │
  └── saveCalculation()
        └── CalculationRepository.insert(CalculationResult) → Room DB
```

### Parameter Depresiasi (Equipment)
```
monthlyDepreciation = (purchasePrice - residualValue) / usefulLifeMonths
totalDepreciation   = Σ monthlyDepreciation semua equipment
```
> Nilai `totalDepreciation` & `totalOverhead` di-inject langsung ke `CalculatorScreen`
> dari `EquipmentViewModel` dan `OverheadViewModel` melalui `NavGraph`.

### Platform Comparison (built-in)

| Platform | Komisi Default | Packaging |
|----------|---------------|-----------|
| Langsung 🏪 | 0% | — |
| GoFood 🟢 | 20% | Rp 1.000 |
| GrabFood 🟩 | 20% | Rp 1.000 |
| ShopeeFood 🟠 | 15% | Rp 500 |
| Tokopedia 🟢 | 5,5% | — |
| Shopee 🟠 | 6,5% | — |
| Custom ⚙️ | user-defined | — |

---

## 5. Database Schema (Room v2)

```
kalkulator_hpp.db
  ├── ingredients          (id, name, unit, pricePerUnit, category)
  ├── recipes              (id, name, description, category, photoUri, notes,
  │                         laborCost, overheadCost, yield, createdAt)
  ├── recipe_ingredient_cross_ref  (recipeId, ingredientId, quantity)
  ├── calculation_results  (id, recipeName, category, totalMaterialCost,
  │                         laborCost, depreciationCost, overheadCost,
  │                         yield, hppPerUnit, marginPct, suggestedPrice, timestamp)
  ├── equipment            (id, name, purchasePrice, purchaseDate,
  │                         usefulLifeMonths, residualValue, notes)
  └── overhead_items       (id, ...)
```

**Migrasi:** `fallbackToDestructiveMigration()` — **data hilang saat upgrade schema.**

---

## 6. Konfigurasi & Settings

### DataStore Keys (`SettingsKeys`)

| Key | Type | Default | Fungsi |
|-----|------|---------|--------|
| `dark_mode` | Boolean | false | Dark/light theme manual |
| `follow_system_theme` | Boolean | true | Ikuti tema sistem |
| `daily_production` | Int | — | Produksi harian (untuk BEP & depresiasi per batch) |
| `currency_symbol` | String | — | Simbol mata uang tampilan |

### Build Config

| Item | Nilai |
|------|-------|
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |
| versionCode | 1 |
| versionName | 1.0 |
| JVM Target | 17 |
| Compose Compiler | 1.5.14 |
| Room DB | 2.6.1 |
| AdMob | 23.2.0 |

### AdMob IDs (saat ini: **TEST IDs**)

| Slot | ID |
|------|----|
| App ID | `ca-app-pub-3940256099942544~3347511713` |
| Banner | `ca-app-pub-3940256099942544/6300978111` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |

> ⚠️ Harus diganti dengan ID produksi sebelum publish ke Play Store.

---

## 7. Utilitas & Infrastruktur

| File | Fungsi |
|------|--------|
| `BackupUtil.kt` | Backup/restore file `.db` Room via `ContentResolver` (SAF) |
| `CsvUtil.kt` | Export/import data ke format CSV (OpenCSV 5.9) |
| `PdfUtil.kt` | Generate laporan PDF (iText7 7.2.5) |
| `AdMobManager.kt` | Wrapper Banner + Interstitial AdMob, init via `CompositionLocal` |
| `TemplateData.kt` | Hardcoded template resep siap pakai (11,9 KB — terbesar di domain layer) |

---

## 8. CI/CD — GitHub Actions

**File:** `.github/workflows/android-build.yml`

```
Trigger: push/PR ke branch `main`, atau workflow_dispatch manual

Steps:
  1. Checkout repo
  2. Setup JDK 17 (Temurin)
  3. Setup Android SDK
  4. Setup Gradle 8.7
  5. gradle assembleRelease   → APK
  6. gradle bundleRelease     → AAB
  7. Upload artifact: app-release.apk
  8. Upload artifact: app-release.aab
```

**Signing:** Menggunakan debug keystore (tidak ada signing produksi di CI).

---

## 9. Risiko & Blind Spot

### 🔴 KRITIS

| # | Isu | Lokasi | Dampak |
|---|-----|--------|--------|
| R1 | `fallbackToDestructiveMigration()` aktif | `AppModule.kt:52` | **Seluruh data pengguna terhapus** jika DB schema berubah tanpa migrasi manual |
| R2 | AdMob TEST IDs masih terpasang | `app/build.gradle.kts:19-21` | Pelanggaran kebijakan AdMob jika rilis ke Play Store |
| R3 | Release build menggunakan **debug keystore** | `app/build.gradle.kts:34` | APK tidak bisa diupdate di Play Store (key mismatch) |

### 🟡 SEDANG

| # | Isu | Lokasi | Dampak |
|---|-----|--------|--------|
| R4 | `BackupUtil` tidak menutup Room sebelum backup | `BackupUtil.kt:16-26` | Potensi korupsi file `.db` (WAL mode aktif by default) |
| R5 | `TemplateData.kt` — hardcoded, 11,9 KB | `domain/model/TemplateData.kt` | Template tidak bisa di-update tanpa rilis app baru |
| R6 | BEP formula menggunakan `fixedCost / profitPerUnit` | `CalculatorViewModel.kt:122-124` | **Blind spot:** `laborCost` tidak dihitung sebagai fixed cost, padahal bisa jadi overhead tetap |
| R7 | `CalculatorScreen` satu file, 25,6 KB | `ui/screen/CalculatorScreen.kt` | Sulit di-maintain dan test; kandidat untuk dipecah |
| R8 | Tidak ada `@Schema` export pada Room | `AppDatabase.kt:18` | Tidak ada versi kontrol skema; sulit audit perubahan DB |

### 🟢 MINOR

| # | Isu | Lokasi | Dampak |
|---|-----|--------|--------|
| R9 | ViewModels dibuat di `NavGraph`, bukan di activity/fragment scope | `NavGraph.kt:46-53` | VM tidak survive config change antar rekomposisi (minor risk dengan `rememberNavController`) |
| R10 | `isMinifyEnabled = false` pada release | `app/build.gradle.kts:33` | APK tidak di-minify/obfuscate → ukuran lebih besar, kode terbuka |
| R11 | Tidak ada unit test untuk `HppCalculator` | `domain/model/` | Formula inti tanpa test coverage |
| R12 | `INTERNET` + `ACCESS_NETWORK_STATE` permission ada, tanpa permission handling UI | `AndroidManifest.xml:4-5` | Aman untuk AdMob, tapi tidak ada feedback ke user jika offline |

---

## 10. Dependency Tree (ringkas)

```
androidx.core:core-ktx:1.13.1
androidx.compose.ui:ui:1.6.8
androidx.compose.material3:material3:1.2.1
androidx.compose.material:material-icons-extended:1.6.8
androidx.navigation:navigation-compose:2.7.7
androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2
androidx.room:room-runtime:2.6.1
androidx.datastore:datastore-preferences:1.1.1
kotlinx-coroutines-android:1.8.1
com.opencsv:opencsv:5.9
com.github.PhilJay:MPAndroidChart:v3.1.0  ← via JitPack
com.itextpdf:itext7-core:7.2.5
com.google.android.gms:play-services-ads:23.2.0
```

---

## 11. File Index (semua source, sorted by size)

| File | Bytes | Keterangan |
|------|-------|------------|
| `CalculatorScreen.kt` | 25.686 | Screen HPP utama — terbesar |
| `TemplateData.kt` | 11.952 | Hardcoded template resep |
| `IngredientScreen.kt` | 11.733 | CRUD bahan baku |
| `RecipeDetailScreen.kt` | 10.967 | Detail & manajemen resep |
| `DashboardScreen.kt` | 10.486 | Halaman home |
| `RecipeScreen.kt` | 10.340 | List resep |
| `HistoryScreen.kt` | 10.182 | Riwayat kalkulasi |
| `EquipmentScreen.kt` | 9.743 | Manajemen alat & depresiasi |
| `SettingsScreen.kt` | 8.844 | Pengaturan aplikasi |
| `CalculatorViewModel.kt` | 8.352 | VM inti kalkulator HPP |
| `NavGraph.kt` | 7.960 | Navigasi & routing |
| `OverheadScreen.kt` | 7.793 | Manajemen overhead |
| `CsvUtil.kt` | 2.677 | Export/import CSV |
| `AppModule.kt` | 2.244 | DI container |
| `DashboardViewModel.kt` | 2.244 | VM dashboard |
| `PdfUtil.kt` | 2.369 | Generate PDF |
| `AdMobManager.kt` | 2.276 | AdMob wrapper |
| `RecipeViewModel.kt` | 3.214 | VM resep |
| `Theme.kt` | 3.360 | Material3 theme |
| `MoreMenuScreen.kt` | 3.356 | Menu lainnya |
| `BackupUtil.kt` | 1.434 | Backup/restore DB |
| `MainActivity.kt` | 1.629 | Entrypoint Activity |
| `HppCalculator.kt` | 992 | **Core formula HPP** |

---

*Last updated: 2026-05-20 — auto-generated via static analysis*
