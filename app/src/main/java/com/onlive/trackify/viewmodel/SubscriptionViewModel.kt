package com.onlive.trackify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.data.repository.SubscriptionRepository
import com.onlive.trackify.utils.ErrorReporter
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val errorReporter: ErrorReporter,
    private val onSubscriptionMutated: () -> Unit = {},
    private val onSubscriptionDeleted: (Long) -> Unit = {}
) : ViewModel() {

    val allSubscriptions: StateFlow<List<SubscriptionWithCategory>?> = repository.allSubscriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun insert(
        subscription: Subscription,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.insert(subscription)) {
                is Result.Success -> {
                    onSubscriptionMutated()
                    onSuccess()
                }
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun update(
        subscription: Subscription,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.update(subscription)) {
                is Result.Success -> {
                    onSubscriptionMutated()
                    onSuccess()
                }
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun delete(
        subscription: Subscription,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.delete(subscription)) {
                is Result.Success -> {
                    onSubscriptionDeleted(subscription.subscriptionId)
                    onSuccess()
                }
                is Result.Error -> {
                    errorReporter.reportError(result)
                    onError()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.reportException(e)
            onError()
        }
    }

    fun getSubscriptionById(id: Long): Flow<SubscriptionWithCategory?> {
        return repository.getSubscriptionById(id)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackifyApplication
                SubscriptionViewModel(
                    repository = app.subscriptionRepository,
                    errorReporter = app.errorHandler,
                    onSubscriptionMutated = {
                        if (PreferenceManager(app).areNotificationsEnabled()) {
                            val scheduler = NotificationScheduler(app)
                            scheduler.scheduleNotifications()
                            scheduler.triggerImmediateCheck()
                        }
                    },
                    onSubscriptionDeleted = { deletedId ->
                        val prefs = PreferenceManager(app)
                        NotificationHelper(app).cancelNotificationsForSubscription(deletedId)
                        prefs.clearNotificationRecordsForSubscription(deletedId)
                        if (prefs.areNotificationsEnabled()) {
                            NotificationScheduler(app).scheduleNotifications()
                        }
                    }
                )
            }
        }
    }
}
