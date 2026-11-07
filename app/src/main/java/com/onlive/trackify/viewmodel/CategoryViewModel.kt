package com.onlive.trackify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.repository.CategoryRepository
import com.onlive.trackify.utils.ErrorReporter
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val errorReporter: ErrorReporter
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun insert(
        category: Category,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.insert(category)) {
                is Result.Success -> onSuccess()
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun update(
        category: Category,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.update(category)) {
                is Result.Success -> onSuccess()
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun delete(
        category: Category,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.delete(category)) {
                is Result.Success -> onSuccess()
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun getCategoryById(id: Long): Flow<Category?> {
        return repository.getCategoryById(id)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackifyApplication
                CategoryViewModel(
                    repository = app.categoryRepository,
                    errorReporter = app.errorHandler
                )
            }
        }
    }
}
