package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.data.model.CategoryWithGroup

@Dao
interface CategoryGroupDao {
    @Insert
    suspend fun insert(group: CategoryGroup): Long

    @Insert
    fun insertSync(group: CategoryGroup): Long

    @Update
    suspend fun update(group: CategoryGroup)

    @Delete
    suspend fun delete(group: CategoryGroup)

    @Query("SELECT * FROM category_groups ORDER BY name ASC")
    fun getAllGroups(): LiveData<List<CategoryGroup>>

    @Query("SELECT * FROM category_groups ORDER BY name ASC")
    fun getAllGroupsSync(): List<CategoryGroup>

    @Query("SELECT * FROM category_groups WHERE groupId = :id")
    fun getGroupById(id: Long): LiveData<CategoryGroup>

    @Query("SELECT * FROM category_groups WHERE groupId = :id")
    fun getGroupByIdSync(id: Long): CategoryGroup?

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithGroups(): LiveData<List<CategoryWithGroup>>

    @Query("SELECT COUNT(*) FROM category_groups")
    suspend fun getGroupsCountSync(): Int

    @Query("DELETE FROM category_groups")
    suspend fun deleteAllSync()
}