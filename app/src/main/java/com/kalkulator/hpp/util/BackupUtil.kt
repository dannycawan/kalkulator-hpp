package com.kalkulator.hpp.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Backup & restore the Room database file.
 */
object BackupUtil {

    private const val DB_NAME = "kalkulator_hpp.db"

    fun backupDatabase(context: Context, destinationUri: Uri) {
        // Close database connections first
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) throw IllegalStateException("Database file not found")

        context.contentResolver.openOutputStream(destinationUri)?.use { output ->
            FileInputStream(dbFile).use { input ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open output stream")
    }

    fun restoreDatabase(context: Context, sourceUri: Uri) {
        val dbFile = context.getDatabasePath(DB_NAME)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            // Ensure parent directory exists
            dbFile.parentFile?.mkdirs()
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open input stream")
    }

    fun getDatabaseSize(context: Context): Long {
        val dbFile = context.getDatabasePath(DB_NAME)
        return if (dbFile.exists()) dbFile.length() else 0L
    }
}
