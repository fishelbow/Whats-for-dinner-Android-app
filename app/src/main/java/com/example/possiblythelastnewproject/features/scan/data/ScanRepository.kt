package com.example.possiblythelastnewproject.features.scan.data

import com.example.possiblythelastnewproject.features.pantry.data.Category
import com.example.possiblythelastnewproject.features.pantry.data.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.PantryItemDao
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val pantryItemDao: PantryItemDao,
    private val categoryDao: CategoryDao

) {
    suspend fun findByScanCode(code: String): PantryItem? {
        // This assumes PantryItem has a `scanCode` property
        return pantryItemDao.getByScanCode(code)
    }

    suspend fun insert(item: PantryItem) {
        pantryItemDao.insertPantryItem(item)
    }

    suspend fun updateItem(item: PantryItem) {
        pantryItemDao.updatePantryItem(item)
    }
    suspend fun findByName(name: String): PantryItem? {
        return pantryItemDao.getByName(name)
    }

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }
}