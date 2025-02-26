package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    fun insertSync(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesSync(): List<Category>

    @Query("SELECT * FROM categories WHERE categoryId = :id")
    fun getCategoryById(id: Long): LiveData<Category>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCountSync(): Int

    @Query("DELETE FROM categories")
    suspend fun deleteAllSync()
}