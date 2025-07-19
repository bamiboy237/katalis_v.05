package com.katalis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.katalis.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?

    @Query("SELECT * FROM categories WHERE parent_category_id IS NULL AND is_active = 1 ORDER BY display_order ASC")
    fun getRootCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parent_category_id = :parentId AND is_active = 1 ORDER BY display_order ASC")
    fun getSubCategories(parentId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' AND is_active = 1 ORDER BY name ASC")
    suspend fun searchCategories(query: String): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()
}