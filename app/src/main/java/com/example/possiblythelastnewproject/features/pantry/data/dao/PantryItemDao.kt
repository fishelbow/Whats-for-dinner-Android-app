package com.example.possiblythelastnewproject.features.pantry.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {

    @Query("SELECT * FROM PantryItem ORDER BY name")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Query("SELECT * FROM PantryItem WHERE scanCode = :code LIMIT 1")
    suspend fun getByScanCode(code: String): PantryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem): Long

    @Update
    suspend fun updatePantryItem(item: PantryItem)

    @Query("SELECT COUNT(*) FROM RecipePantryItemCrossRef WHERE pantryItemId = :pantryItemId")
    suspend fun countRecipesUsing(pantryItemId: Long): Int

    @Delete
    suspend fun deletePantryItem(item: PantryItem)

    @Query("SELECT * FROM pantryItem WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByName(name: String): PantryItem?

    @Query("SELECT * FROM pantryItem WHERE category = :category ORDER BY name")
    fun getItemsByCategory(category: String): Flow<List<PantryItem>>

    @Query("SELECT * FROM PantryItem")
    suspend fun getAllOnce(): List<PantryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PantryItem>)


    @Query("DELETE FROM pantryitem")
    suspend fun clearAll()




}