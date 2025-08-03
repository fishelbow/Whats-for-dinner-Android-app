package com.example.possiblythelastnewproject.features.pantry.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.dto.PantryItemSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {

    // ————— Flow Queries —————

    @Query("SELECT * FROM PantryItem ORDER BY name")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Query("SELECT * FROM PantryItem WHERE category = :category ORDER BY name")
    fun getItemsByCategory(category: String): Flow<List<PantryItem>>

    // ————— Paging Queries —————

    @Query("SELECT * FROM PantryItem WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' ORDER BY name")
    fun getPagedPantryItems(query: String): PagingSource<Int, PantryItem>

    @Query("SELECT id, name, imageUri, scanCode FROM PantryItem ORDER BY name")
    fun getPagedPantrySummaries(): PagingSource<Int, PantryItemSummary>

    @Query("SELECT * FROM PantryItem WHERE category = :category ORDER BY name")
    fun getPagedItemsByCategory(category: String): PagingSource<Int, PantryItem>

    @Query("SELECT * FROM PantryItem WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun getPagedSearchResults(query: String): PagingSource<Int, PantryItem>

    // ————— Single Queries —————

    @Query("SELECT * FROM PantryItem WHERE scanCode = :code LIMIT 1")
    suspend fun getByScanCode(code: String): PantryItem?

    @Query("SELECT * FROM PantryItem WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByName(name: String): PantryItem?

    @Query("SELECT * FROM PantryItem")
    suspend fun getAllOnce(): List<PantryItem>

    @Query("SELECT * FROM PantryItem ORDER BY name")
    suspend fun getAll(): List<PantryItem>

    @Query("SELECT imageUri FROM PantryItem WHERE imageUri IS NOT NULL")
    suspend fun getAllPantryImageUris(): List<String>

    @Query("SELECT * FROM PantryItem LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<PantryItem>

    @Query("SELECT COUNT(*) FROM RecipePantryItemCrossRef WHERE pantryItemId = :pantryItemId")
    suspend fun countRecipesUsing(pantryItemId: Long): Int

    // ————— Mutators —————

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PantryItem>)

    @Update
    suspend fun updatePantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("DELETE FROM PantryItem")
    suspend fun clearAll()


}