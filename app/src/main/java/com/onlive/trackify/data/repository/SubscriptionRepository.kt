package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.Subscription
import java.util.Calendar

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    val allActiveSubscriptions: LiveData<List<Subscription>> = subscriptionDao.getAllActiveSubscriptions()

    suspend fun insert(subscription: Subscription): Long {
        return subscriptionDao.insert(subscription)
    }

    suspend fun update(subscription: Subscription) {
        subscriptionDao.update(subscription)
    }

    suspend fun delete(subscription: Subscription) {
        subscriptionDao.delete(subscription)
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

    suspend fun getActiveSubscriptionsSync(): List<Subscription> {
        return subscriptionDao.getActiveSubscriptionsSync()
    }
}