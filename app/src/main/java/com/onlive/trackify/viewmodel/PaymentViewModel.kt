package com.onlive.trackify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.repository.PaymentRepository
import com.onlive.trackify.utils.ErrorReporter
import com.onlive.trackify.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: PaymentRepository,
    private val errorReporter: ErrorReporter
) : ViewModel() {

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun insert(
        payment: Payment,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.insert(payment)) {
                is Result.Success -> onSuccess()
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
        payment: Payment,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.update(payment)) {
                is Result.Success -> onSuccess()
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
        payment: Payment,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            when (val result = repository.delete(payment)) {
                is Result.Success -> onSuccess()
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

    fun getPaymentById(id: Long): Flow<Payment?> {
        return repository.getPaymentById(id)
    }

    fun getPaymentsBySubscription(subscriptionId: Long): Flow<List<Payment>> {
        return repository.getPaymentsBySubscription(subscriptionId)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackifyApplication
                PaymentViewModel(
                    repository = app.paymentRepository,
                    errorReporter = app.errorHandler
                )
            }
        }
    }
}
