package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.util.Log
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object OrphanHunter {

    suspend fun runAudit(
        context: Context,
        recipeDao: RecipeDao,
        pantryDao: PantryItemDao
    ): List<File> = withContext(Dispatchers.IO) {

        // 🔍 Step 1: Locate image directory and scan disk
        val imageDir = context.filesDir
        val allFiles = imageDir.listFiles()?.toList() ?: emptyList()
        Log.d("OrphanHunter", "📁 Found ${allFiles.size} files in ${imageDir.absolutePath}")
        allFiles.forEach { Log.d("OrphanHunter", "📁 Disk file: ${it.name}") }

        // 🔗 Step 2: Fetch all DB image URIs
        val recipeUris = recipeDao.getAllImageUris()
        val pantryUris = pantryDao.getAllImageUris()
        Log.d("OrphanHunter", "📦 DB URIs → Recipe: ${recipeUris.size}, Pantry: ${pantryUris.size}")

        // 📄 Step 3: Resolve URIs into expected filenames
        val usedFileNames: Set<String> = (recipeUris + pantryUris)
            .filterNotNull()
            .map { ImagePathResolver.resolveFlatFilename(it) }
            .toSet()
        Log.d("OrphanHunter", "🔗 Linked filenames: ${usedFileNames.size}")
        usedFileNames.forEach { Log.d("OrphanHunter", "🔗 Linked: $it") }

        // 🧠 Step 4: Build usage count map
        val usageCount: Map<String, Int> = usedFileNames.groupingBy { it }.eachCount()

        // 🧹 Step 5: Evaluate each disk file
        val deletedOrphans = mutableListOf<File>()
        allFiles.forEach { file ->
            val name = file.name
            val usage = usageCount[name] ?: 0

            when {
                usage > 1 -> Log.d("OrphanHunter", "🧩 Duplicate linked file: $name (x$usage)")
                usage == 1 -> Log.d("OrphanHunter", "✅ File linked to DB: $name")
                else -> {
                    Log.d("OrphanHunter", "🕳️ Unreferenced file: $name")
                    val deleted = file.delete()
                    if (deleted) {
                        Log.d("OrphanHunter", "🗑️ Deleted orphan: $name")
                        deletedOrphans.add(file)
                    } else {
                        Log.d("OrphanHunter", "⚠️ Failed to delete orphan: $name")
                    }
                }
            }
        }

        // ✅ Step 6: Summary
        Log.d("OrphanHunter", "🧼 Cleanup complete. Orphans deleted: ${deletedOrphans.size}")
        return@withContext deletedOrphans
    }
}