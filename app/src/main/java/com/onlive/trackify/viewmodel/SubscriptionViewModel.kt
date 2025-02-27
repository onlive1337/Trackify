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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    private val errorHandler: ErrorHandler = (application as TrackifyApplication).errorHandler

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val allActiveSubscriptions: LiveData<List<Subscription>>

    val activeSubscriptionsCount: LiveData<Int>

    private val _operationResult = MutableLiveData<Result<*>>()
    val operationResult: LiveData<Result<*>> = _operationResult

    init {
        val database = AppDatabase.getDatabase(application)
        val subscriptionDao = database.subscriptionDao()
        val categoryDao = database.categoryDao()

        repository = SubscriptionRepository(subscriptionDao, categoryDao)

        allActiveSubscriptions = repository.allActiveSubscriptions
        activeSubscriptionsCount = repository.activeSubscriptionsCount
    }

    fun insert(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.insert(subscription)
            _operationResult.value = result

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            _operationResult.value = Result.Error("Ошибка при добавлении подписки", e)
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.update(subscription)
            _operationResult.value = result

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            _operationResult.value = Result.Error("Ошибка при обновлении подписки", e)
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.delete(subscription)
            _operationResult.value = result

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            _operationResult.value = Result.Error("Ошибка при удалении подписки", e)
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
    }

    fun deactivateSubscription(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.value = true

        try {
            val updatedSubscription = subscription.copy(active = false)
            val result = repository.update(updatedSubscription)
            _operationResult.value = result

            if (result is Result.Error) {
                errorHandler.handleError(result.message, true)
            }
        } catch (e: Exception) {
            _operationResult.value = Result.Error("Ошибка при деактивации подписки", e)
            errorHandler.handleError(e, true)
        } finally {
            _isLoading.value = false
        }
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

    fun searchSubscriptions(query: String): LiveData<List<Subscription>> {
        return repository.searchSubscriptions(query)
    }

    fun loadSubscriptionsPage(limit: Int, offset: Int) = viewModelScope.launch {
        _isLoading.value = true

        try {
            val result = repository.getActiveSubscriptionsPage(limit, offset)
            _operationResult.value = result

            if (result is Result.Error) {
                errorHandler.handleError(result.message, false)
            }
        } catch (e: Exception) {
            _operationResult.value = Result.Error("Ошибка при загрузке подписок", e)
            errorHandler.handleError(e, false)
        } finally {
            _isLoading.value = false
        }
    }
}