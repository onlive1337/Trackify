package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.data.model.CategoryWithGroup
import com.onlive.trackify.data.repository.CategoryGroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryGroupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryGroupRepository
    val allGroups: LiveData<List<CategoryGroup>>
    val categoriesWithGroups: LiveData<List<CategoryWithGroup>>

    init {
        val categoryGroupDao = AppDatabase.getDatabase(application).categoryGroupDao()
        repository = CategoryGroupRepository(categoryGroupDao)
        allGroups = repository.allGroups
        categoriesWithGroups = repository.getCategoriesWithGroups()
    }

    fun insert(group: CategoryGroup) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(group)
    }

    fun update(group: CategoryGroup) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(group)
    }

    fun delete(group: CategoryGroup) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(group)
    }

    fun getGroupById(id: Long): LiveData<CategoryGroup> {
        return repository.getGroupById(id)
    }
}