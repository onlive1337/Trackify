package com.example.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.trackify.data.database.AppDatabase
import com.example.trackify.data.model.Subscription
import com.example.trackify.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    val allActiveSubscriptions: LiveData<List<Subscription>>

    init {
        val subscriptionDao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(subscriptionDao)
        allActiveSubscriptions = repository.allActiveSubscriptions
    }

    fun insert(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(subscription)
    }

    fun update(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(subscription)
    }

    fun delete(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(subscription)
    }

    fun deactivateSubscription(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        val updatedSubscription = subscription.copy(active = false)
        repository.update(updatedSubscription)
    }

    fun getSubscriptionById(id: Long): LiveData<Subscription> {
        return repository.getSubscriptionById(id)
    }

    fun getSubscriptionsByCategory(categoryId: Long): LiveData<List<Subscription>> {
        return repository.getSubscriptionsByCategory(categoryId)
    }

    fun getUpcomingExpirations(days: Int): LiveData<List<Subscription>> {
        return repository.getUpcomingExpirations(days)
    }
}