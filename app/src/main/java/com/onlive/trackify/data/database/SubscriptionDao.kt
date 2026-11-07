package com.onlive.trackify.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Insert
    suspend fun insertAll(subscriptions: List<Subscription>)

    @Update
    suspend fun update(subscription: Subscription): Int

    @Delete
    suspend fun delete(subscription: Subscription)

    @Transaction
    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    fun getAllSubscriptionsWithCategory(): Flow<List<SubscriptionWithCategory>>

    @Transaction
    @Query("SELECT * FROM subscriptions WHERE subscriptionId = :id")
    fun getSubscriptionWithCategoryById(id: Long): Flow<SubscriptionWithCategory?>

    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    suspend fun getAllSnapshot(): List<Subscription>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptionsForWorker(): List<Subscription>

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}
