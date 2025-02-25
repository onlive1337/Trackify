package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.repository.PaymentRepository
import com.onlive.trackify.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository: SubscriptionRepository
    private val paymentRepository: PaymentRepository

    private val _totalMonthlySpending = MutableLiveData<Double>()
    val totalMonthlySpending: LiveData<Double> = _totalMonthlySpending

    private val _totalYearlySpending = MutableLiveData<Double>()
    val totalYearlySpending: LiveData<Double> = _totalYearlySpending

    private val _monthlySpendingByCategory = MutableLiveData<Map<String, Double>>()
    val monthlySpendingByCategory: LiveData<Map<String, Double>> = _monthlySpendingByCategory

    init {
        val database = AppDatabase.getDatabase(application)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        paymentRepository = PaymentRepository(database.paymentDao())
    }

    fun calculateMonthlySpending() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        var monthlyCost = 0.0

        for (subscription in subscriptions) {
            monthlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price
                BillingFrequency.YEARLY -> subscription.price / 12
            }
        }

        _totalMonthlySpending.postValue(monthlyCost)
    }

    fun calculateYearlySpending() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        var yearlyCost = 0.0

        for (subscription in subscriptions) {
            yearlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price * 12
                BillingFrequency.YEARLY -> subscription.price
            }
        }

        _totalYearlySpending.postValue(yearlyCost)
    }

    // Функции для получения статистики по категориям, по времени и т.д.
    // можно добавить по мере необходимости
}