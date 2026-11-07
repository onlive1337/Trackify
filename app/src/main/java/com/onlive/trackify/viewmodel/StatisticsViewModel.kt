package com.onlive.trackify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.LiveStatisticsUpdater
import com.onlive.trackify.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val liveStatisticsUpdater = LiveStatisticsUpdater(
        subscriptionsFlow = subscriptionRepository.allSubscriptions
    )

    val uiState: StateFlow<UiState> = liveStatisticsUpdater.statisticsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(isLoading = true)
        )

    data class UiState(
        val isLoading: Boolean = true,
        val totalMonthlySpending: Double = 0.0,
        val totalYearlySpending: Double = 0.0,
        val spendingByCategory: List<CategorySpending> = emptyList()
    )

    data class CategorySpending(
        val categoryId: Long?,
        val categoryName: String?,
        val colorCode: String,
        val amount: Double
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrackifyApplication
                StatisticsViewModel(subscriptionRepository = app.subscriptionRepository)
            }
        }
    }
}
