package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import com.onlive.trackify.data.database.CategoryGroupDao
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.data.model.CategoryWithGroup

class CategoryGroupRepository(private val categoryGroupDao: CategoryGroupDao) {

    val allGroups: LiveData<List<CategoryGroup>> = categoryGroupDao.getAllGroups()

    suspend fun insert(group: CategoryGroup): Long {
        return categoryGroupDao.insert(group)
    }

    suspend fun update(group: CategoryGroup) {
        categoryGroupDao.update(group)
    }

    suspend fun delete(group: CategoryGroup) {
        categoryGroupDao.delete(group)
    }

    fun getGroupById(id: Long): LiveData<CategoryGroup> {
        return categoryGroupDao.getGroupById(id)
    }

    fun getCategoriesWithGroups(): LiveData<List<CategoryWithGroup>> {
        return categoryGroupDao.getCategoriesWithGroups()
    }
}