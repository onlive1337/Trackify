package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.repository.SubscriptionRepository
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    private val errorHandler: ErrorHandler = (application as TrackifyApplication).errorHandler

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val allActiveSubscriptions: LiveData<List<Subscription>>

    init {
        val database = AppDatabase.getDatabase(application)
        val subscriptionDao = database.subscriptionDao()
        val categoryDao = database.categoryDao()

        repository = SubscriptionRepository(subscriptionDao, categoryDao, application.applicationContext)

        allActiveSubscriptions = repository.allActiveSubscriptions
    }

    fun insert(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.insert(subscription)

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.update(subscription)

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.delete(subscription)

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun getSubscriptionById(id: Long): MediatorLiveData<Subscription?> {
        return repository.getSubscriptionById(id)
    }
}