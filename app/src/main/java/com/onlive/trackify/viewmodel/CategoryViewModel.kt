package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryRepository
    val allCategories: LiveData<List<Category>>

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories
    }

    fun insert(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(category)
    }

    fun update(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(category)
    }

    fun delete(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(category)
    }

    fun getCategoryById(id: Long): LiveData<Category> {
        return repository.getCategoryById(id)
    }
}