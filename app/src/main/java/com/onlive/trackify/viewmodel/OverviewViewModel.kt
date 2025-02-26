package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.repository.PaymentRepository
import com.onlive.trackify.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository: SubscriptionRepository
    private val paymentRepository: PaymentRepository

    private val _totalMonthlySpending = MutableLiveData<Double>()
    val totalMonthlySpending: LiveData<Double> = _totalMonthlySpending

    private val _activeSubscriptionsCount = MutableLiveData<Int>()
    val activeSubscriptionsCount: LiveData<Int> = _activeSubscriptionsCount

    private val _expiringSubscriptionsCount = MutableLiveData<Int>()
    val expiringSubscriptionsCount: LiveData<Int> = _expiringSubscriptionsCount

    init {
        val database = AppDatabase.getDatabase(application)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        paymentRepository = PaymentRepository(database.paymentDao())

        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            calculateTotalMonthlySpending()
            calculateActiveSubscriptionsCount()
            calculateExpiringSubscriptionsCount()
        }
    }

    private fun calculateTotalMonthlySpending() {
        viewModelScope.launch(Dispatchers.IO) {
            val subscriptions = subscriptionRepository.getActiveSubscriptionsSync()

            var monthlyCost = 0.0
            for (subscription in subscriptions) {
                monthlyCost += when (subscription.billingFrequency) {
                    BillingFrequency.MONTHLY -> subscription.price
                    BillingFrequency.YEARLY -> subscription.price / 12
                }
            }

            _totalMonthlySpending.postValue(monthlyCost)
        }
    }

    private fun calculateActiveSubscriptionsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            val subscriptions = subscriptionRepository.getActiveSubscriptionsSync()
            _activeSubscriptionsCount.postValue(subscriptions.size)
        }
    }

    private fun calculateExpiringSubscriptionsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val today = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 30)
            val thirtyDaysLater = calendar.time

            val subscriptions = subscriptionRepository.getActiveSubscriptionsSync()
            val expiringCount = subscriptions.count { subscription ->
                subscription.endDate != null && subscription.endDate.after(today) && subscription.endDate.before(thirtyDaysLater)
            }

            _expiringSubscriptionsCount.postValue(expiringCount)
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}