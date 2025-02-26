package com.onlive.trackify.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
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

    @Query("SELECT SUM(amount) FROM payments WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalAmountBetweenDates(startDate: Date, endDate: Date): LiveData<Double?>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalAmountBetweenDatesNullSafe(startDate: Date, endDate: Date): LiveData<Double>

    @Query("SELECT * FROM payments WHERE subscriptionId = :subscriptionId ORDER BY date DESC LIMIT 1")
    suspend fun getLastPaymentForSubscriptionSync(subscriptionId: Long): Payment?

    @Query("SELECT * FROM payments WHERE subscriptionId = :subscriptionId AND date BETWEEN :startDate AND :endDate")
    suspend fun getPaymentsForSubscriptionBetweenDatesSync(subscriptionId: Long, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY date DESC")
    fun getPaymentsByStatus(status: PaymentStatus): LiveData<List<Payment>>

    @Query("SELECT COUNT(*) FROM payments WHERE status = :status")
    fun getPaymentsCountByStatus(status: PaymentStatus): LiveData<Int>

    @Query("UPDATE payments SET status = :newStatus WHERE paymentId = :paymentId")
    suspend fun updatePaymentStatus(paymentId: Long, newStatus: PaymentStatus)

    @Query("SELECT * FROM payments WHERE status = :status AND date <= :date ORDER BY date DESC")
    fun getPendingPaymentsBeforeDate(status: PaymentStatus = PaymentStatus.PENDING, date: Date): LiveData<List<Payment>>

    @Query("SELECT COUNT(*) FROM payments WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth")
    fun getPaymentsCountByMonth(yearMonth: String): LiveData<Int>

    @Query("DELETE FROM payments")
    suspend fun deleteAllSync()
}