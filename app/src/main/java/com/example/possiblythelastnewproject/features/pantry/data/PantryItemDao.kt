package com.example.possiblythelastnewproject.features.pantry.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM PantryItem WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): PantryItem?


}