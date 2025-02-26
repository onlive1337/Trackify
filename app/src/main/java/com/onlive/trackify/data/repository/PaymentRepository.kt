package com.onlive.trackify.data.repository

import androidx.lifecycle.LiveData
import com.onlive.trackify.data.database.PaymentDao
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import java.util.Calendar
import java.util.Date

class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()

    suspend fun insert(payment: Payment): Long {
        return paymentDao.insert(payment)
    }

    suspend fun update(payment: Payment) {
        paymentDao.update(payment)
    }

    suspend fun delete(payment: Payment) {
        paymentDao.delete(payment)
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

        return paymentDao.getTotalAmountBetweenDates(startDate, endDate)
    }

    fun getPendingPayments(): LiveData<List<Payment>> {
        return paymentDao.getPaymentsByStatus(PaymentStatus.PENDING)
    }

    fun getRecentPendingPayments(): LiveData<List<Payment>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        return paymentDao.getPendingPaymentsBeforeDate(PaymentStatus.PENDING, calendar.time)
    }

    fun getPendingPaymentsCount(): LiveData<Int> {
        return paymentDao.getPaymentsCountByStatus(PaymentStatus.PENDING)
    }

    suspend fun confirmPayment(paymentId: Long) {
        paymentDao.updatePaymentStatus(paymentId, PaymentStatus.CONFIRMED)
    }

    suspend fun confirmPayment(payment: Payment) {
        val updatedPayment = payment.copy(status = PaymentStatus.CONFIRMED)
        paymentDao.update(updatedPayment)
    }
}