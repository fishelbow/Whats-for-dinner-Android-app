package com.example.possiblythelastnewproject.features.pantry.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM Category ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM Category WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByName(name: String): Category?

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM Category")
    suspend fun getAllCategoriesOnce(): List<Category>


    @Query("SELECT * FROM Category")
    suspend fun getAllOnce(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Category>)

    @Query("DELETE FROM Category")
    suspend fun clearAll()

    @Query("SELECT * FROM Category ORDER BY name LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<Category>
}