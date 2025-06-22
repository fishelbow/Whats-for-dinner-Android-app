package com.example.possiblythelastnewproject.features.scan.data

import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.PantryItemDao

import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val pantryItemDao: PantryItemDao
) {
    suspend fun findByScanCode(code: String): PantryItem? {
        // This assumes PantryItem has a `scanCode` property
        return pantryItemDao.getByScanCode(code)
    }

    suspend fun insert(item: PantryItem) {
        pantryItemDao.insertPantryItem(item)

    }
}