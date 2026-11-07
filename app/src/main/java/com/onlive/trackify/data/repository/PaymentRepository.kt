package com.onlive.trackify.data.repository

import com.onlive.trackify.R
import com.onlive.trackify.data.database.PaymentDao
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    suspend fun insert(payment: Payment): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = paymentDao.insert(payment)
            Result.Success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_creating_payment, e)
        }
    }

    suspend fun update(payment: Payment): Result<Unit> = withContext(ioDispatcher) {
        try {
            paymentDao.update(payment)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_updating_payment, e)
        }
    }

    suspend fun delete(payment: Payment): Result<Unit> = withContext(ioDispatcher) {
        try {
            paymentDao.delete(payment)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(R.string.error_deleting_payment, e)
        }
    }

    fun getPaymentById(id: Long): Flow<Payment?> {
        if (id == -1L) return flowOf(null)
        return paymentDao.getPaymentById(id).catch {
            emit(null)
        }
    }

    fun getPaymentsBySubscription(subscriptionId: Long): Flow<List<Payment>> {
        if (subscriptionId == -1L) return flowOf(emptyList())
        return paymentDao.getPaymentsBySubscription(subscriptionId).catch {
            emit(emptyList())
        }
    }
}
