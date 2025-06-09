package com.onlive.trackify.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.R
import com.onlive.trackify.data.database.CategoryDao
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Calendar

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) {

    private val _allActiveSubscriptions = MediatorLiveData<List<Subscription>>()
    val allActiveSubscriptions: LiveData<List<Subscription>> = _allActiveSubscriptions

    val activeSubscriptionsCount: LiveData<Int> = subscriptionDao.getActiveSubscriptionsCount()

    private val dbTimeoutMs = 10_000L

    init {
        try {
            val subscriptionsLiveData = subscriptionDao.getAllActiveSubscriptions()
            val categoriesLiveData = categoryDao.getAllCategories()

            _allActiveSubscriptions.addSource(subscriptionsLiveData) { subscriptions ->
                combineSubscriptionsWithCategories(subscriptions ?: emptyList(), categoriesLiveData.value ?: emptyList())
            }

            _allActiveSubscriptions.addSource(categoriesLiveData) { categories ->
                combineSubscriptionsWithCategories(subscriptionsLiveData.value ?: emptyList(), categories ?: emptyList())
            }
        } catch (e: Exception) {
            _allActiveSubscriptions.value = emptyList()
        }
    }

    private fun combineSubscriptionsWithCategories(subscriptions: List<Subscription>, categories: List<Category>) {
        try {
            val result = subscriptions.map { subscription ->
                subscription.apply {
                    val category = subscription.categoryId?.let { id ->
                        categories.find { it.categoryId == id }
                    }
                    categoryName = category?.name ?: context.getString(R.string.without_category)
                    categoryColor = category?.colorCode
                }
            }
            _allActiveSubscriptions.value = result
        } catch (e: Exception) {
            _allActiveSubscriptions.value = subscriptions.map { subscription ->
                subscription.apply {
                    categoryName = context.getString(R.string.without_category)
                    categoryColor = null
                }
            }
        }
    }

    suspend fun insert(subscription: Subscription): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val validationResult = validateSubscription(subscription)
            if (validationResult is Result.Error) {
                return@withContext validationResult
            }

            withTimeout(dbTimeoutMs) {
                val id = subscriptionDao.insert(subscription)
                Result.Success(id)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error(context.getString(R.string.error_database_timeout), e)
        } catch (e: SQLiteConstraintException) {
            Result.Error(context.getString(R.string.error_constraint_violation), e)
        } catch (e: SQLiteException) {
            Result.Error(context.getString(R.string.error_database_operation), e)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_creating_subscription), e)
        }
    }

    suspend fun update(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val validationResult = validateSubscription(subscription)
            if (validationResult is Result.Error) {
                return@withContext validationResult
            }

            withTimeout(dbTimeoutMs) {
                subscriptionDao.update(subscription)
                Result.Success(Unit)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error(context.getString(R.string.error_database_timeout), e)
        } catch (e: SQLiteConstraintException) {
            Result.Error(context.getString(R.string.error_constraint_violation), e)
        } catch (e: SQLiteException) {
            Result.Error(context.getString(R.string.error_database_operation), e)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_updating_subscription), e)
        }
    }

    suspend fun delete(subscription: Subscription): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            withTimeout(dbTimeoutMs) {
                subscriptionDao.delete(subscription)
                Result.Success(Unit)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error(context.getString(R.string.error_database_timeout), e)
        } catch (e: SQLiteException) {
            Result.Error(context.getString(R.string.error_database_operation), e)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_deleting_subscription), e)
        }
    }

    fun getSubscriptionById(id: Long): MediatorLiveData<Subscription?> {
        val result = MediatorLiveData<Subscription?>()

        try {
            val subscription = subscriptionDao.getSubscriptionById(id)
            val categories = categoryDao.getAllCategories()

            result.addSource(subscription) { sub ->
                try {
                    if (sub != null) {
                        val cats = categories.value ?: emptyList()
                        val category = sub.categoryId?.let { catId ->
                            cats.find { it.categoryId == catId }
                        }
                        sub.categoryName = category?.name ?: context.getString(R.string.without_category)
                        sub.categoryColor = category?.colorCode
                        result.value = sub
                    } else {
                        result.value = null
                    }
                } catch (e: Exception) {
                    result.value = sub
                }
            }

            result.addSource(categories) { cats ->
                try {
                    val sub = subscription.value
                    if (sub != null) {
                        val category = sub.categoryId?.let { catId ->
                            cats?.find { it.categoryId == catId }
                        }
                        sub.categoryName = category?.name ?: context.getString(R.string.without_category)
                        sub.categoryColor = category?.colorCode
                        result.value = sub
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
            result.value = null
        }

        return result
    }

    fun getSubscriptionsByCategory(categoryId: Long): LiveData<List<Subscription>> {
        val result = MediatorLiveData<List<Subscription>>()

        try {
            val subscriptions = subscriptionDao.getSubscriptionsByCategory(categoryId)
            val categories = categoryDao.getAllCategories()

            result.addSource(subscriptions) { subs ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subs ?: emptyList(), categories.value ?: emptyList())
                } catch (e: Exception) {
                    result.value = subs ?: emptyList()
                }
            }

            result.addSource(categories) { cats ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subscriptions.value ?: emptyList(), cats ?: emptyList())
                } catch (e: Exception) {
                    result.value = subscriptions.value ?: emptyList()
                }
            }
        } catch (e: Exception) {
            result.value = emptyList()
        }

        return result
    }

    fun getUpcomingExpirations(days: Int): LiveData<List<Subscription>> {
        val result = MediatorLiveData<List<Subscription>>()

        try {
            val safeDays = days.coerceIn(1, 365)
            val calendar = Calendar.getInstance()
            val startDate = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, safeDays)
            val endDate = calendar.time

            val subscriptions = subscriptionDao.getSubscriptionsExpiringBetween(startDate, endDate)
            val categories = categoryDao.getAllCategories()

            result.addSource(subscriptions) { subs ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subs ?: emptyList(), categories.value ?: emptyList())
                } catch (e: Exception) {
                    result.value = subs ?: emptyList()
                }
            }

            result.addSource(categories) { cats ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subscriptions.value ?: emptyList(), cats ?: emptyList())
                } catch (e: Exception) {
                    result.value = subscriptions.value ?: emptyList()
                }
            }
        } catch (e: Exception) {
            result.value = emptyList()
        }

        return result
    }

    suspend fun getActiveSubscriptionsSync(): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            withTimeout(dbTimeoutMs) {
                val subscriptions = subscriptionDao.getActiveSubscriptionsSync()
                val categories = categoryDao.getAllCategoriesSync()

                val result = subscriptions.map { subscription ->
                    subscription.apply {
                        val category = subscription.categoryId?.let { id ->
                            categories.find { it.categoryId == id }
                        }
                        categoryName = category?.name ?: context.getString(R.string.without_category)
                        categoryColor = category?.colorCode
                    }
                }

                Result.Success(result)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error(context.getString(R.string.error_database_timeout), e)
        } catch (e: SQLiteException) {
            Result.Error(context.getString(R.string.error_database_operation), e)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_loading_subscriptions), e)
        }
    }

    fun searchSubscriptions(query: String): LiveData<List<Subscription>> {
        val result = MediatorLiveData<List<Subscription>>()

        try {
            val safeQuery = query.take(100)
            val subscriptions = subscriptionDao.searchActiveSubscriptions(safeQuery)
            val categories = categoryDao.getAllCategories()

            result.addSource(subscriptions) { subs ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subs ?: emptyList(), categories.value ?: emptyList())
                } catch (e: Exception) {
                    result.value = subs ?: emptyList()
                }
            }

            result.addSource(categories) { cats ->
                try {
                    combineSubscriptionsWithCategoriesForResult(result, subscriptions.value ?: emptyList(), cats ?: emptyList())
                } catch (e: Exception) {
                    result.value = subscriptions.value ?: emptyList()
                }
            }
        } catch (e: Exception) {
            result.value = emptyList()
        }

        return result
    }

    suspend fun getActiveSubscriptionsPage(limit: Int, offset: Int): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val safeLimit = limit.coerceIn(1, 1000)
            val safeOffset = offset.coerceAtLeast(0)

            withTimeout(dbTimeoutMs) {
                val subscriptions = subscriptionDao.getActiveSubscriptionsPage(safeLimit, safeOffset)
                val categories = categoryDao.getAllCategoriesSync()

                val result = subscriptions.map { subscription ->
                    subscription.apply {
                        val category = subscription.categoryId?.let { id ->
                            categories.find { it.categoryId == id }
                        }
                        categoryName = category?.name ?: context.getString(R.string.without_category)
                        categoryColor = category?.colorCode
                    }
                }

                Result.Success(result)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error(context.getString(R.string.error_database_timeout), e)
        } catch (e: SQLiteException) {
            Result.Error(context.getString(R.string.error_database_operation), e)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_loading_subscriptions_page), e)
        }
    }

    private fun combineSubscriptionsWithCategoriesForResult(
        result: MediatorLiveData<List<Subscription>>,
        subscriptions: List<Subscription>,
        categories: List<Category>
    ) {
        try {
            val combinedList = subscriptions.map { subscription ->
                subscription.apply {
                    val category = subscription.categoryId?.let { id ->
                        categories.find { it.categoryId == id }
                    }
                    categoryName = category?.name ?: context.getString(R.string.without_category)
                    categoryColor = category?.colorCode
                }
            }
            result.value = combinedList
        } catch (e: Exception) {
            result.value = subscriptions
        }
    }

    private fun validateSubscription(subscription: Subscription): Result<Unit> {
        return try {
            when {
                subscription.name.isBlank() ->
                    Result.Error(context.getString(R.string.error_subscription_name_empty))

                subscription.name.length > 100 ->
                    Result.Error(context.getString(R.string.error_subscription_name_too_long))

                subscription.name.trim() != subscription.name ->
                    Result.Error(context.getString(R.string.error_subscription_name_whitespace))

                containsControlCharacters(subscription.name) ->
                    Result.Error(context.getString(R.string.error_subscription_name_invalid_chars))

                subscription.price < 0 ->
                    Result.Error(context.getString(R.string.error_subscription_price_negative))

                subscription.price > 999_999_999.99 ->
                    Result.Error(context.getString(R.string.error_subscription_price_too_large))

                !subscription.price.isFinite() ->
                    Result.Error(context.getString(R.string.error_subscription_price_invalid))

                subscription.description != null && subscription.description.length > 500 ->
                    Result.Error(context.getString(R.string.error_subscription_description_too_long))

                subscription.description != null && containsControlCharacters(subscription.description) ->
                    Result.Error(context.getString(R.string.error_subscription_description_invalid_chars))

                subscription.startDate.after(subscription.endDate) && subscription.endDate != null ->
                    Result.Error(context.getString(R.string.error_subscription_date_invalid))

                isDateTooFarInFuture(subscription.startDate) ->
                    Result.Error(context.getString(R.string.error_subscription_date_too_far))

                isDateTooFarInPast(subscription.startDate) ->
                    Result.Error(context.getString(R.string.error_subscription_date_too_old))

                subscription.endDate != null && isDateTooFarInFuture(subscription.endDate) ->
                    Result.Error(context.getString(R.string.error_subscription_end_date_too_far))

                subscription.categoryId != null && subscription.categoryId <= 0 ->
                    Result.Error(context.getString(R.string.error_subscription_invalid_category))

                else -> Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_subscription_validation), e)
        }
    }

    private fun containsControlCharacters(text: String): Boolean {
        return try {
            text.any { char ->
                char.isISOControl() && char != '\n' && char != '\r' && char != '\t'
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun isDateTooFarInFuture(date: java.util.Date): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, 50)
            date.after(calendar.time)
        } catch (e: Exception) {
            false
        }
    }

    private fun isDateTooFarInPast(date: java.util.Date): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -50)
            date.before(calendar.time)
        } catch (e: Exception) {
            false
        }
    }
}