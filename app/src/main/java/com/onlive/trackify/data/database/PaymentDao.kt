package com.onlive.trackify.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlive.trackify.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Insert
    suspend fun insertAll(payments: List<Payment>)

    @Update
    suspend fun update(payment: Payment): Int

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    suspend fun getAllSnapshot(): List<Payment>

    @Query("SELECT * FROM payments WHERE paymentId = :id")
    fun getPaymentById(id: Long): Flow<Payment?>

    @Query("SELECT * FROM payments WHERE subscriptionId = :subscriptionId ORDER BY date DESC")
    fun getPaymentsBySubscription(subscriptionId: Long): Flow<List<Payment>>

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}
