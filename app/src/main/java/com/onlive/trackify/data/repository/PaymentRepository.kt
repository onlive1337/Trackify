package com.onlive.trackify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.R
import com.onlive.trackify.data.database.PaymentDao
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import android.util.Log

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val context: Context
) {

    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()

    private val _totalMonthlyAmount = MediatorLiveData<Double>()
    val totalMonthlyAmount: LiveData<Double> = _totalMonthlyAmount

    companion object {
        private const val TAG = "PaymentRepository"
    }

    init {
        try {
            updateTotalMonthlyAmount()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PaymentRepository", e)
        }
    }

    private fun updateTotalMonthlyAmount() {
        try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            val totalAmount = getTotalAmountForMonth(year, month)

            _totalMonthlyAmount.addSource(totalAmount) { amount ->
                try {
                    _totalMonthlyAmount.value = amount ?: 0.0
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating total monthly amount", e)
                    _totalMonthlyAmount.value = 0.0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTotalMonthlyAmount", e)
            _totalMonthlyAmount.value = 0.0
        }
    }

    suspend fun insert(payment: Payment): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = paymentDao.insert(payment)
            Log.d(TAG, "Payment inserted successfully with ID: $id")
            Result.Success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting payment", e)
            Result.Error(context.getString(R.string.error_creating_payment), e)
        }
    }

    suspend fun update(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.update(payment)
            Log.d(TAG, "Payment updated successfully: ${payment.paymentId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment: ${payment.paymentId}", e)
            Result.Error(context.getString(R.string.error_updating_payment), e)
        }
    }

    suspend fun delete(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.delete(payment)
            Log.d(TAG, "Payment deleted successfully: ${payment.paymentId}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting payment: ${payment.paymentId}", e)
            Result.Error(context.getString(R.string.error_deleting_payment), e)
        }
    }

    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>> {
        return try {
            paymentDao.getPaymentsBySubscription(subscriptionId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting payments by subscription: $subscriptionId", e)
            MediatorLiveData<List<Payment>>().apply { value = emptyList() }
        }
    }

    fun getPaymentsForMonth(year: Int, month: Int): LiveData<List<Payment>> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            val startDate = calendar.time

            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.time

            paymentDao.getPaymentsBetweenDates(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting payments for month: $year-$month", e)
            MediatorLiveData<List<Payment>>().apply { value = emptyList() }
        }
    }

    fun getTotalAmountForMonth(year: Int, month: Int): LiveData<Double> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            val startDate = calendar.time

            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.time

            paymentDao.getTotalAmountBetweenDatesNullSafe(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total amount for month: $year-$month", e)
            MediatorLiveData<Double>().apply { value = 0.0 }
        }
    }

    suspend fun getPaymentsPage(limit: Int, offset: Int): Result<List<Payment>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val page = paymentDao.getPaymentsPage(limit, offset)
            Log.d(TAG, "Loaded payments page: limit=$limit, offset=$offset, size=${page.size}")
            Result.Success(page)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading payments page: limit=$limit, offset=$offset", e)
            Result.Error(context.getString(R.string.error_loading_payments_page), e)
        }
    }
}