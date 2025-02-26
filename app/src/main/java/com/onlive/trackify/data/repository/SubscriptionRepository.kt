package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    private val cacheService = CacheService.getInstance()
    private val cacheTime = TimeUnit.MINUTES.toMillis(5)

    val allActiveSubscriptions: LiveData<List<Subscription>> = subscriptionDao.getAllActiveSubscriptions()

    val activeSubscriptionsCount: LiveData<Int> = subscriptionDao.getActiveSubscriptionsCount()

    suspend fun insert(subscription: Subscription): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = subscriptionDao.insert(subscription)
            cacheService.clearCache("active_subscriptions")
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при создании подписки")
        }
    }

    suspend fun update(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            subscriptionDao.update(subscription)
            cacheService.clearCache("active_subscriptions")
            cacheService.clearCache("subscription_${subscription.subscriptionId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при обновлении подписки")
        }
    }

    suspend fun delete(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            subscriptionDao.delete(subscription)
            cacheService.clearCache("active_subscriptions")
            cacheService.clearCache("subscription_${subscription.subscriptionId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при удалении подписки")
        }
    }

    fun getSubscriptionById(id: Long): LiveData<Subscription> {
        return subscriptionDao.getSubscriptionById(id)
    }

    fun getSubscriptionsByCategory(categoryId: Long): LiveData<List<Subscription>> {
        return subscriptionDao.getSubscriptionsByCategory(categoryId)
    }

    fun getUpcomingExpirations(days: Int): LiveData<List<Subscription>> {
        val calendar = Calendar.getInstance()
        val startDate = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, days)
        val endDate = calendar.time

        return subscriptionDao.getSubscriptionsExpiringBetween(startDate, endDate)
    }

    suspend fun getActiveSubscriptionsSync(): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cachedData = cacheService.getList<Subscription>("active_subscriptions")
            if (cachedData != null) {
                return@withContext Result.Success(cachedData)
            }

            val subscriptions = subscriptionDao.getActiveSubscriptionsSync()
            cacheService.putList("active_subscriptions", subscriptions, cacheTime)
            Result.Success(subscriptions)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении подписок")
        }
    }

    fun searchSubscriptions(query: String): LiveData<List<Subscription>> {
        return subscriptionDao.searchActiveSubscriptions(query)
    }

    suspend fun getActiveSubscriptionsPage(limit: Int, offset: Int): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheKey = "active_subscriptions_page_${limit}_${offset}"

            val cachedData = cacheService.getList<Subscription>(cacheKey)
            if (cachedData != null) {
                return@withContext Result.Success(cachedData)
            }

            val page = subscriptionDao.getActiveSubscriptionsPage(limit, offset)
            cacheService.putList(cacheKey, page, cacheTime)
            Result.Success(page)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при загрузке страницы подписок")
        }
    }
}