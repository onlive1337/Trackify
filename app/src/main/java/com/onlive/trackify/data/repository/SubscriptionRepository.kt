package com.onlive.trackify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.R
import com.onlive.trackify.data.database.CategoryDao
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) {

    private val _allSubscriptions = MediatorLiveData<List<Subscription>>()
    val allSubscriptions: LiveData<List<Subscription>> = _allSubscriptions

    init {
        val subscriptionsLiveData = subscriptionDao.getAllSubscriptions()
        val categoriesLiveData = categoryDao.getAllCategories()

        _allSubscriptions.addSource(subscriptionsLiveData) { subscriptions ->
            combineSubscriptionsWithCategories(subscriptions, categoriesLiveData.value ?: emptyList())
        }

        _allSubscriptions.addSource(categoriesLiveData) { categories ->
            combineSubscriptionsWithCategories(subscriptionsLiveData.value ?: emptyList(), categories)
        }
    }

    private fun combineSubscriptionsWithCategories(subscriptions: List<Subscription>, categories: List<Category>) {
        val result = subscriptions.map { subscription ->
            decorateWithCategory(subscription, categories)
        }
        _allSubscriptions.value = result
    }

    private fun decorateWithCategory(subscription: Subscription, categories: List<Category>): Subscription {
        val category = subscription.categoryId?.let { id ->
            categories.find { it.categoryId == id }
        }
        return subscription.copy().apply {
            categoryName = category?.name ?: context.getString(R.string.without_category)
            categoryColor = category?.colorCode
        }
    }

    suspend fun insert(subscription: Subscription): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = subscriptionDao.insert(subscription)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_creating_subscription), e)
        }
    }

    suspend fun update(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            subscriptionDao.update(subscription)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_updating_subscription), e)
        }
    }

    suspend fun delete(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            subscriptionDao.delete(subscription)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_deleting_subscription), e)
        }
    }

    fun getSubscriptionById(id: Long): MediatorLiveData<Subscription?> {
        val result = MediatorLiveData<Subscription?>()
        val subscription = subscriptionDao.getSubscriptionById(id)
        val categories = categoryDao.getAllCategories()

        result.addSource(subscription) { sub ->
            if (sub != null) {
                result.value = decorateWithCategory(sub, categories.value ?: emptyList())
            }
        }

        result.addSource(categories) { cats ->
            val sub = subscription.value
            if (sub != null) {
                result.value = decorateWithCategory(sub, cats)
            }
        }

        return result
    }
}