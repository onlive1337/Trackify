package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Subscription
import java.util.Date

@Dao
interface SubscriptionDao {
    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("SELECT * FROM subscriptions WHERE active = 1 ORDER BY name ASC")
    fun getAllActiveSubscriptions(): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE subscriptionId = :id")
    fun getSubscriptionById(id: Long): LiveData<Subscription>

    @Query("SELECT * FROM subscriptions WHERE categoryId = :categoryId AND active = 1")
    fun getSubscriptionsByCategory(categoryId: Long): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE endDate BETWEEN :startDate AND :endDate AND active = 1")
    fun getSubscriptionsExpiringBetween(startDate: Date, endDate: Date): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE active = 1")
    suspend fun getActiveSubscriptionsSync(): List<Subscription>
}