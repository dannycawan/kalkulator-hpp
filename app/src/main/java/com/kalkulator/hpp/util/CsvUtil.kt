package com.kalkulator.hpp.util

import android.content.Context
import android.net.Uri
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.kalkulator.hpp.data.local.entity.Ingredient
import com.opencsv.CSVWriter
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class CsvUtil {

    fun exportIngredients(context: Context, uri: Uri, ingredients: List<Ingredient>) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val writer = CSVWriter(OutputStreamWriter(stream))
            writer.writeNext(arrayOf("ID", "Nama", "Satuan", "Harga/Satuan", "Supplier"))
            ingredients.forEach {
                writer.writeNext(arrayOf(it.id.toString(), it.name, it.unit, it.pricePerUnit.toString(), it.supplier ?: ""))
            }
            writer.close()
        }
    }

    fun importIngredients(context: Context, uri: Uri): List<Ingredient> {
        val result = mutableListOf<Ingredient>()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val reader = CSVReader(InputStreamReader(stream))
            reader.readNext() // skip header
            var line = reader.readNext()
            while (line != null) {
                if (line.size >= 4) {
                    result.add(Ingredient(
                        name = line[1],
                        unit = line[2],
                        pricePerUnit = line[3].toDoubleOrNull() ?: 0.0,
                        supplier = line.getOrNull(4)?.ifBlank { null }
                    ))
                }
                line = reader.readNext()
            }
            reader.close()
        }
        return result
    }

    fun exportCalculations(context: Context, uri: Uri, calculations: List<CalculationResult>) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val writer = CSVWriter(OutputStreamWriter(stream))
            writer.writeNext(arrayOf("ID", "Resep", "Biaya Bahan", "Biaya Tenaga Kerja", "Biaya Overhead", "Yield", "HPP/Unit", "Margin %", "Harga Jual", "Tanggal"))
            calculations.forEach {
                writer.writeNext(arrayOf(
                    it.id.toString(), it.recipeName, it.totalMaterialCost.toString(),
                    it.laborCost.toString(), it.overheadCost.toString(), it.yield.toString(),
                    it.hppPerUnit.toString(), it.marginPct.toString(), it.suggestedPrice.toString(),
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("id")).format(java.util.Date(it.timestamp))
                ))
            }
            writer.close()
        }
    }
}
