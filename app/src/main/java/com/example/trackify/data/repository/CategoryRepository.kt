package com.example.trackify.data.repository

import androidx.lifecycle.LiveData
import com.example.trackify.data.database.CategoryDao
import com.example.trackify.data.model.Category

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

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
}