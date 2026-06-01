package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.repository.SubscriptionRepository
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as? TrackifyApplication
    private val repository: SubscriptionRepository = app?.subscriptionRepository
        ?: run {
            val database = AppDatabase.getDatabase(application)
            SubscriptionRepository(database.subscriptionDao(), database.categoryDao(), application.applicationContext)
        }
    private val errorHandler: ErrorHandler? = app?.errorHandler

    val allSubscriptions: LiveData<List<Subscription>> = repository.allSubscriptions

    fun insert(subscription: Subscription) = viewModelScope.launch {
        try {
            val result = repository.insert(subscription)

            if (result is Result.Error) {
                errorHandler?.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler?.handleError(e, true)
        }
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        try {
            val result = repository.update(subscription)

            if (result is Result.Error) {
                errorHandler?.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler?.handleError(e, true)
        }
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        try {
            val result = repository.delete(subscription)

            if (result is Result.Error) {
                errorHandler?.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler?.handleError(e, true)
        }
    }

    fun getSubscriptionById(id: Long): MediatorLiveData<Subscription?> {
        return repository.getSubscriptionById(id)
    }
}