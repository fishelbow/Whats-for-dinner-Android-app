package com.example.possiblythelastnewproject.core.data.csvBackup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.possiblythelastnewproject.core.data.AppDatabase
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipInputStream

class RoomCsvImporter<PantryItem : Any>(
    private val context: Context,
    private val db: AppDatabase
) {
    suspend fun importFromZip(zipUri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var backup = ParsedBackup()
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val reader = BufferedReader(InputStreamReader(zipIn))
                        val lines = reader.readLines()

                        if (lines.isNotEmpty()) {
                            val tableName = entry.name.removeSuffix(".csv")
                            backup = when (tableName) {
                                "PantryItem" -> backup.copy(pantryItems = parsePantryItems(lines))
                                "ShoppingList" -> backup.copy(shoppingLists = parseShoppingLists(lines))
                                "ShoppingListItem" -> backup.copy(shoppingListItems = parseShoppingListItems(lines))
                                "Recipe" -> backup.copy(recipes = parseRecipes(lines))
                                "RecipePantryItemCrossRef" -> backup.copy(recipePantryRefs = parseRecipeRefs(lines))
                                "Category" -> backup.copy(categories = parseCategories(lines))
                                else -> backup
                            }
                        }

                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }

                    db.withTransaction {
                        db.pantryItemDao().insertAll(backup.pantryItems)
                        db.shoppingListDao().insertAll(backup.shoppingLists)
                        db.shoppingListEntryDao().insertAll(backup.shoppingListItems)
                        db.recipeDao().insertAll(backup.recipes)
                        db.recipeIngredientDao().insertAll(backup.recipePantryRefs)
                        db.categoryDao().insertAll(backup.categories)
                    }
                }
            }
        }
    }

    private fun parsePantryItems(lines: List<String>): List<com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                PantryItem(
                    id = values[0].toLongOrNull() ?: 0L,
                    name = values[1],
                    quantity = values[2].toIntOrNull() ?: 0,
                    imageData = null,
                    shouldTrack = values[4].toBooleanStrictOrNull() ?: true,
                    addToShoppingList = values[5].toBooleanStrictOrNull() ?: true,
                    scanCode = values[6].ifBlank { null },
                    category = values[7],
                    uuid = values.getOrNull(8) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseShoppingLists(lines: List<String>): List<ShoppingList> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                ShoppingList(
                    id = values[0].toLongOrNull() ?: 0L,
                    name = values[1],
                    createdAt = values.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis(),
                    uuid = values.getOrNull(3) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseShoppingListItems(lines: List<String>): List<ShoppingListItem> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                ShoppingListItem(
                    id = values[0].toLongOrNull() ?: 0L,
                    listId = values[1].toLongOrNull() ?: 0L,
                    pantryItemId = values[2].toLongOrNull() ?: 0L,
                    name = values[3],
                    quantity = values[4],
                    isChecked = values[5].toBooleanStrictOrNull() ?: false,
                    isGenerated = values[6].toBooleanStrictOrNull() ?: false,
                    recipeId = values[7].toLongOrNull(),
                    uuid = values.getOrNull(8) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseRecipes(lines: List<String>): List<Recipe> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                Recipe(
                    id = values[0].toLongOrNull() ?: 0L,
                    name = values[1],
                    temp = values[2],
                    prepTime = values[3],
                    cookTime = values[4],
                    category = values[5],
                    instructions = values[6],
                    imageData = null,
                    color = values.getOrNull(8)?.toIntOrNull() ?: 0,
                    uuid = values.getOrNull(9) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseRecipeRefs(lines: List<String>): List<RecipePantryItemCrossRef> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                RecipePantryItemCrossRef(
                    recipeId = values[0].toLongOrNull() ?: 0L,
                    pantryItemId = values[1].toLongOrNull() ?: 0L,
                    required = values.getOrNull(2)?.toBooleanStrictOrNull() ?: false,
                    amountNeeded = values.getOrNull(3) ?: "",
                    uuid = values.getOrNull(4) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseCategories(lines: List<String>): List<Category> =
        lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            try {
                Category(
                    id = values[0].toLongOrNull() ?: 0L,
                    name = values[1],
                    uuid = values.getOrNull(2) ?: UUID.randomUUID().toString()
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun parseCsvLine(line: String): List<String> =
        line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            .map { it.trim().removeSurrounding("\"") }
}