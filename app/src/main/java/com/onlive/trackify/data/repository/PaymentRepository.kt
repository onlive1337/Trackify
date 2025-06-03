package com.onlive.trackify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.R
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.PaymentDao
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val context: Context
) {

    private val cacheService = CacheService.getInstance()
    private val cacheTime = TimeUnit.MINUTES.toMillis(5)

    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()

    val pendingPayments: LiveData<List<Payment>> = paymentDao.getPaymentsByStatus(PaymentStatus.PENDING)
    val pendingPaymentsCount: LiveData<Int> = paymentDao.getPaymentsCountByStatus(PaymentStatus.PENDING)

    private val _totalMonthlyAmount = MediatorLiveData<Double>()
    val totalMonthlyAmount: LiveData<Double> = _totalMonthlyAmount

    init {
        updateTotalMonthlyAmount()
    }

    private fun updateTotalMonthlyAmount() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val totalAmount = getTotalAmountForMonth(year, month)

        _totalMonthlyAmount.addSource(totalAmount) { amount ->
            _totalMonthlyAmount.value = amount ?: 0.0
        }
    }

    suspend fun insert(payment: Payment): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val id = paymentDao.insert(payment)
            clearPaymentCaches(payment)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_creating_payment), e)
        }
    }

    suspend fun update(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.update(payment)
            clearPaymentCaches(payment)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_updating_payment), e)
        }
    }

    suspend fun delete(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.delete(payment)
            clearPaymentCaches(payment)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_deleting_payment), e)
        }
    }

    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>> {
        return paymentDao.getPaymentsBySubscription(subscriptionId)
    }

    fun getPaymentsForMonth(year: Int, month: Int): LiveData<List<Payment>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.time

        return paymentDao.getPaymentsBetweenDates(startDate, endDate)
    }

    fun getTotalAmountForMonth(year: Int, month: Int): LiveData<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.time

        return paymentDao.getTotalAmountBetweenDatesNullSafe(startDate, endDate)
    }

    fun getRecentPendingPayments(): LiveData<List<Payment>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        return paymentDao.getPendingPaymentsBeforeDate(PaymentStatus.PENDING, calendar.time)
    }

    suspend fun confirmPayment(paymentId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            paymentDao.updatePaymentStatus(paymentId, PaymentStatus.CONFIRMED)
            cacheService.clearCache("pending_payments")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_confirming_payment), e)
        }
    }

    suspend fun confirmPayment(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val updatedPayment = payment.copy(status = PaymentStatus.CONFIRMED)
            paymentDao.update(updatedPayment)
            clearPaymentCaches(updatedPayment)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_confirming_payment), e)
        }
    }

    suspend fun getPaymentsPage(limit: Int, offset: Int): Result<List<Payment>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheKey = "payments_page_${limit}_${offset}"

            val cachedData = cacheService.getList<Payment>(cacheKey)
            if (cachedData != null) {
                return@withContext Result.Success(cachedData)
            }

            val page = paymentDao.getPaymentsPage(limit, offset)
            cacheService.putList(cacheKey, page, cacheTime)
            Result.Success(page)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_loading_payments_page), e)
        }
    }

    private fun clearPaymentCaches(payment: Payment) {
        cacheService.clearCache("all_payments")
        cacheService.clearCache("payment_${payment.paymentId}")
        cacheService.clearCache("subscription_payments_${payment.subscriptionId}")

        if (payment.status == PaymentStatus.PENDING) {
            cacheService.clearCache("pending_payments")
        }

        val calendar = Calendar.getInstance()
        calendar.time = payment.date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        cacheService.clearCache("payments_${year}_${month}")
    }
}