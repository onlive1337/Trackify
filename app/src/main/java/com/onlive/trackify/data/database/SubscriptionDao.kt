package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Subscription

@Dao
interface SubscriptionDao {
    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Insert
    fun insertSync(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    fun getAllSubscriptions(): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    fun getAllSubscriptionsSync(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE subscriptionId = :id")
    fun getSubscriptionById(id: Long): LiveData<Subscription>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptionsForWorker(): List<Subscription>

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAllSync()
}