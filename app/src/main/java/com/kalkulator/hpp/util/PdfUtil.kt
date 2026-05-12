package com.kalkulator.hpp.util

import android.content.Context
import android.net.Uri
import com.kalkulator.hpp.data.local.entity.CalculationResult
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.UnitValue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfUtil(private val context: Context) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id"))

    fun exportCalculation(uri: Uri, result: CalculationResult) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val pdfWriter = PdfWriter(stream)
            val pdfDoc = PdfDocument(pdfWriter)
            val doc = Document(pdfDoc)

            doc.add(Paragraph("Laporan Perhitungan HPP").setBold().setFontSize(18f))
            doc.add(Paragraph("Tanggal: ${dateFormat.format(Date(result.timestamp))}").setFontSize(10f))
            doc.add(Paragraph("\n"))

            val table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .useAllAvailableWidth()

            fun addRow(label: String, value: String) {
                table.addCell(Cell().add(Paragraph(label)))
                table.addCell(Cell().add(Paragraph(value)))
            }

            addRow("Nama Resep", result.recipeName)
            addRow("Biaya Bahan Baku", currencyFormat.format(result.totalMaterialCost))
            addRow("Biaya Tenaga Kerja", currencyFormat.format(result.laborCost))
            addRow("Biaya Overhead", currencyFormat.format(result.overheadCost))
            addRow("Total Biaya", currencyFormat.format(result.totalMaterialCost + result.laborCost + result.overheadCost))
            addRow("Yield (jumlah produk)", "${result.yield} unit")
            addRow("HPP per Unit", currencyFormat.format(result.hppPerUnit))
            addRow("Margin", "${result.marginPct}%")
            addRow("Harga Jual Disarankan", currencyFormat.format(result.suggestedPrice))

            doc.add(table)
            doc.close()
        }
    }
}
