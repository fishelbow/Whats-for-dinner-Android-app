package com.example.possiblythelastnewproject.features.pantry.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

}