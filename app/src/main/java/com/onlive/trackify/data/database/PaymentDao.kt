package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Payment
import java.util.Date

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

    @Query("SELECT * FROM payments ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaymentsPage(limit: Int, offset: Int): List<Payment>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPaymentsSync(): List<Payment>

    @Query("SELECT * FROM payments WHERE subscriptionId = :subscriptionId ORDER BY date DESC")
    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getPaymentsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalAmountBetweenDatesNullSafe(startDate: Date, endDate: Date): LiveData<Double>

    @Query("DELETE FROM payments")
    suspend fun deleteAllSync()
}