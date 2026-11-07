package com.onlive.trackify.data.repository

import com.onlive.trackify.R
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allSubscriptions: Flow<List<SubscriptionWithCategory>> =
        subscriptionDao.getAllSubscriptionsWithCategory().catch {
            emit(emptyList())
        }

    suspend fun insert(subscription: Subscription): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = subscriptionDao.insert(subscription)
            Result.Success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_creating_subscription, e)
        }
    }

    suspend fun update(subscription: Subscription): Result<Unit> = withContext(ioDispatcher) {
        try {
            val rowsUpdated = subscriptionDao.update(subscription)
            if (rowsUpdated == 0) {
                Result.Error(R.string.error_updating_subscription, IllegalStateException("Subscription not found"))
            } else {
                Result.Success(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_updating_subscription, e)
        }
    }

    suspend fun delete(subscription: Subscription): Result<Unit> = withContext(ioDispatcher) {
        try {
            subscriptionDao.delete(subscription)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_deleting_subscription, e)
        }
    }

    fun getSubscriptionById(id: Long): Flow<SubscriptionWithCategory?> {
        if (id == -1L) return flowOf(null)
        return subscriptionDao.getSubscriptionWithCategoryById(id).catch {
            emit(null)
        }
    }
}
