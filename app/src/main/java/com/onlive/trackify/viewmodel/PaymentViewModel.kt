package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PaymentRepository
    val allPayments: LiveData<List<Payment>>
    val pendingPayments: LiveData<List<Payment>>
    val pendingPaymentsCount: LiveData<Int>
    val recentPendingPayments: LiveData<List<Payment>>

    init {
        val paymentDao = AppDatabase.getDatabase(application).paymentDao()
        repository = PaymentRepository(paymentDao, application.applicationContext)
        allPayments = repository.allPayments
        pendingPayments = repository.pendingPayments
        pendingPaymentsCount = repository.pendingPaymentsCount
        recentPendingPayments = repository.getRecentPendingPayments()
    }

    fun insert(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(payment)
    }

    fun update(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(payment)
    }

    fun delete(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(payment)
    }

    fun getPaymentsBySubscription(subscriptionId: Long): LiveData<List<Payment>> {
        return repository.getPaymentsBySubscription(subscriptionId)
    }

    fun getPaymentsForMonth(year: Int, month: Int): LiveData<List<Payment>> {
        return repository.getPaymentsForMonth(year, month)
    }

    fun getTotalAmountForMonth(year: Int, month: Int): LiveData<Double> {
        return repository.getTotalAmountForMonth(year, month)
    }

    fun confirmPayment(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        repository.confirmPayment(payment)
    }

    fun confirmPayment(paymentId: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.confirmPayment(paymentId)
    }
}