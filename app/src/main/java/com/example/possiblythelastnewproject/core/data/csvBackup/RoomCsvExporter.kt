package com.example.possiblythelastnewproject.core.data.csvBackup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


class RoomCsvExporter(
    private val context: Context,
    private val db: AppDatabase
) {
    suspend fun exportToZip(zipUri: Uri, progress: (Int) -> Unit = {}) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(zipUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    val tables = listOf(
                        async { "PantryItem" to db.pantryItemDao().getAllOnce() },
                        async { "ShoppingList" to db.shoppingListDao().getAllOnce() },
                        async { "ShoppingListItem" to db.shoppingListEntryDao().getAllOnce() },
                        async { "Recipe" to db.recipeDao().getAllOnce() },
                        async { "RecipePantryItemCrossRef" to db.recipeIngredientDao().getAllOnce() },
                        async { "Category" to db.categoryDao().getAllOnce() }
                    ).awaitAll()

                    val total = tables.size
                    tables.forEachIndexed { index, (name, rows) ->
                        Log.d("RoomCsvExporter", "Exporting $name: ${rows.size} rows")
                        val csv = buildCsv(rows)
                        zipOut.putNextEntry(ZipEntry("$name.csv"))
                        zipOut.write(csv.toByteArray())
                        zipOut.closeEntry()
                        progress(((index + 1) * 100) / total)
                    }

                    Log.d("RoomCsvExporter", "Export complete to $zipUri")
                }
            } ?: Log.e("RoomCsvExporter", "Failed to open output stream for $zipUri")
        }
    }

    private inline fun <reified T : Any> buildCsv(rows: List<T>): String {
        val props = T::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        return buildString {
            appendLine(props.joinToString(",") { it.name })
            rows.forEach { row ->
                appendLine(props.joinToString(",") { prop ->
                    prop.get(row)?.toString()?.let { escapeCsv(it) } ?: ""
                })
            }
        }
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}