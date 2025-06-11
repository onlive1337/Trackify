package com.onlive.trackify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.onlive.trackify.data.database.PaymentDao
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val context: Context
) {

    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()

    private val tag = "PaymentRepository"

    suspend fun insert(payment: Payment): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = paymentDao.insert(payment)
            Log.d(tag, "Payment inserted successfully with ID: $id")
            Result.Success(id)
        } catch (e: Exception) {
            Log.e(tag, "Error inserting payment", e)
            Result.Error(context.getString(com.onlive.trackify.R.string.error_creating_payment), e)
        }
    }

    suspend fun update(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.update(payment)
            Log.d(tag, "Payment updated successfully: ${payment.paymentId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error updating payment: ${payment.paymentId}", e)
            Result.Error(context.getString(com.onlive.trackify.R.string.error_updating_payment), e)
        }
    }

    suspend fun delete(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.delete(payment)
            Log.d(tag, "Payment deleted successfully: ${payment.paymentId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error deleting payment: ${payment.paymentId}", e)
            Result.Error(context.getString(com.onlive.trackify.R.string.error_deleting_payment), e)
        }
    }

    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>> {
        return try {
            paymentDao.getPaymentsBySubscription(subscriptionId)
        } catch (e: Exception) {
            Log.e(tag, "Error getting payments by subscription: $subscriptionId", e)
            androidx.lifecycle.MediatorLiveData<List<Payment>>().apply { value = emptyList() }
        }
    }
}