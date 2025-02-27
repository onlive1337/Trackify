package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.CategoryDao
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao
) {

    private val cacheService = CacheService.getInstance()
    private val cacheTime = TimeUnit.MINUTES.toMillis(5)

    private val _allActiveSubscriptions = MediatorLiveData<List<Subscription>>()
    val allActiveSubscriptions: LiveData<List<Subscription>> = _allActiveSubscriptions

    val activeSubscriptionsCount: LiveData<Int> = subscriptionDao.getActiveSubscriptionsCount()

    init {
        val subscriptionsLiveData = subscriptionDao.getAllActiveSubscriptions()
        val categoriesLiveData = categoryDao.getAllCategories()

        _allActiveSubscriptions.addSource(subscriptionsLiveData) { subscriptions ->
            combineSubscriptionsWithCategories(subscriptions, categoriesLiveData.value ?: emptyList())
        }

        _allActiveSubscriptions.addSource(categoriesLiveData) { categories ->
            combineSubscriptionsWithCategories(subscriptionsLiveData.value ?: emptyList(), categories)
        }
    }

    private fun combineSubscriptionsWithCategories(subscriptions: List<Subscription>, categories: List<Category>) {
        val result = subscriptions.map { subscription ->
            subscription.apply {
                val category = subscription.categoryId?.let { id ->
                    categories.find { it.categoryId == id }
                }
                categoryName = category?.name ?: "Без категории"
                categoryColor = category?.colorCode
            }
        }
        _allActiveSubscriptions.value = result
    }

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
        val result = MediatorLiveData<Subscription>()
        val subscription = subscriptionDao.getSubscriptionById(id)
        val categories = categoryDao.getAllCategories()

        result.addSource(subscription) { sub ->
            if (sub != null) {
                val cats = categories.value ?: emptyList()
                val category = sub.categoryId?.let { catId ->
                    cats.find { it.categoryId == catId }
                }
                sub.categoryName = category?.name ?: "Без категории"
                sub.categoryColor = category?.colorCode
                result.value = sub
            }
        }

        result.addSource(categories) { cats ->
            val sub = subscription.value
            if (sub != null) {
                val category = sub.categoryId?.let { catId ->
                    cats.find { it.categoryId == catId }
                }
                sub.categoryName = category?.name ?: "Без категории"
                sub.categoryColor = category?.colorCode
                result.value = sub
            }
        }

        return result
    }

    fun getSubscriptionsByCategory(categoryId: Long): LiveData<List<Subscription>> {
        val result = MediatorLiveData<List<Subscription>>()
        val subscriptions = subscriptionDao.getSubscriptionsByCategory(categoryId)
        val categories = categoryDao.getAllCategories()

        result.addSource(subscriptions) { subs ->
            combineSubscriptionsWithCategories(subs, categories.value ?: emptyList())
        }

        result.addSource(categories) { cats ->
            combineSubscriptionsWithCategories(subscriptions.value ?: emptyList(), cats)
        }

        return result
    }

    fun getUpcomingExpirations(days: Int): LiveData<List<Subscription>> {
        val calendar = Calendar.getInstance()
        val startDate = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, days)
        val endDate = calendar.time

        val result = MediatorLiveData<List<Subscription>>()
        val subscriptions = subscriptionDao.getSubscriptionsExpiringBetween(startDate, endDate)
        val categories = categoryDao.getAllCategories()

        result.addSource(subscriptions) { subs ->
            combineSubscriptionsWithCategories(subs, categories.value ?: emptyList())
        }

        result.addSource(categories) { cats ->
            combineSubscriptionsWithCategories(subscriptions.value ?: emptyList(), cats)
        }

        return result
    }

    suspend fun getActiveSubscriptionsSync(): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cachedData: List<Subscription>? = cacheService.getList("active_subscriptions")
            if (cachedData != null) {
                return@withContext Result.Success(cachedData)
            }

            val subscriptions = subscriptionDao.getActiveSubscriptionsSync()
            val categories = categoryDao.getAllCategoriesSync()

            val result = subscriptions.map { subscription ->
                subscription.apply {
                    val category = subscription.categoryId?.let { id ->
                        categories.find { it.categoryId == id }
                    }
                    categoryName = category?.name ?: "Без категории"
                    categoryColor = category?.colorCode
                }
            }

            cacheService.putList("active_subscriptions", result, cacheTime)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении подписок")
        }
    }

    fun searchSubscriptions(query: String): LiveData<List<Subscription>> {
        val result = MediatorLiveData<List<Subscription>>()
        val subscriptions = subscriptionDao.searchActiveSubscriptions(query)
        val categories = categoryDao.getAllCategories()

        result.addSource(subscriptions) { subs ->
            combineSubscriptionsWithCategories(subs, categories.value ?: emptyList())
        }

        result.addSource(categories) { cats ->
            combineSubscriptionsWithCategories(subscriptions.value ?: emptyList(), cats)
        }

        return result
    }

    suspend fun getActiveSubscriptionsPage(limit: Int, offset: Int): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheKey = "active_subscriptions_page_${limit}_${offset}"

            val cachedData: List<Subscription>? = cacheService.getList(cacheKey)
            if (cachedData != null) {
                return@withContext Result.Success(cachedData)
            }

            val subscriptions = subscriptionDao.getActiveSubscriptionsPage(limit, offset)
            val categories = categoryDao.getAllCategoriesSync()

            val result = subscriptions.map { subscription ->
                subscription.apply {
                    val category = subscription.categoryId?.let { id ->
                        categories.find { it.categoryId == id }
                    }
                    categoryName = category?.name ?: "Без категории"
                    categoryColor = category?.colorCode
                }
            }

            cacheService.putList(cacheKey, result, cacheTime)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при загрузке страницы подписок")
        }
    }
}