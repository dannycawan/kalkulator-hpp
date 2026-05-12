# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.kalkulator.hpp.data.local.entity.** { *; }

# Keep iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep OpenCSV
-keep class com.opencsv.** { *; }
-dontwarn com.opencsv.**
