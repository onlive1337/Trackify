package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import com.onlive.trackify.data.database.CategoryDao
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryWithGroup

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    val categoriesWithGroups: LiveData<List<CategoryWithGroup>> = categoryDao.getCategoriesWithGroups()

    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    fun getCategoryById(id: Long): LiveData<Category> {
        return categoryDao.getCategoryById(id)
    }

    fun getCategoriesByGroup(groupId: Long): LiveData<List<Category>> {
        return categoryDao.getCategoriesByGroup(groupId)
    }

    fun getCategoriesWithoutGroup(): LiveData<List<Category>> {
        return categoryDao.getCategoriesWithoutGroup()
    }
}