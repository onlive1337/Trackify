package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Payment

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Insert
    fun insertSync(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): LiveData<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPaymentsSync(): List<Payment>

    @Query("SELECT * FROM payments WHERE subscriptionId = :subscriptionId ORDER BY date DESC")
    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>>

    @Query("DELETE FROM payments")
    suspend fun deleteAllSync()
}