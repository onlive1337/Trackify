package com.onlive.trackify.data.repository

import com.onlive.trackify.R
import com.onlive.trackify.data.database.CategoryDao
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insert(category: Category): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = categoryDao.insert(category)
            Result.Success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_creating_category, e)
        }
    }

    suspend fun update(category: Category): Result<Unit> = withContext(ioDispatcher) {
        try {
            categoryDao.update(category)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_updating_category, e)
        }
    }

    suspend fun delete(category: Category): Result<Unit> = withContext(ioDispatcher) {
        try {
            categoryDao.delete(category)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_deleting_category, e)
        }
    }

    fun getCategoryById(id: Long): Flow<Category?> {
        if (id == -1L) return flowOf(null)
        return categoryDao.getCategoryById(id)
    }
}
