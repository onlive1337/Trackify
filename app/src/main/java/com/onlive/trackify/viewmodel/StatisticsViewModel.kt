package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.TrackifyApplication
import com.onlive.trackify.data.LiveStatisticsUpdater
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.repository.CategoryRepository
import com.onlive.trackify.data.repository.SubscriptionRepository

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as? TrackifyApplication
    private val subscriptionRepository: SubscriptionRepository = app?.subscriptionRepository
        ?: run {
            val database = AppDatabase.getDatabase(application)
            SubscriptionRepository(database.subscriptionDao(), database.categoryDao(), application.applicationContext)
        }
    private val categoryRepository: CategoryRepository = app?.categoryRepository
        ?: CategoryRepository(AppDatabase.getDatabase(application).categoryDao())
    private val liveStatisticsUpdater: LiveStatisticsUpdater

    val isLoading: LiveData<Boolean>

    val totalMonthlySpending: LiveData<Double>
    val totalYearlySpending: LiveData<Double>
    val spendingByCategory: LiveData<List<CategorySpending>>
    val monthlySpendingHistory: LiveData<List<MonthlySpending>>
    val subscriptionTypeSpending: LiveData<List<SubscriptionTypeSpending>>
    val categories: LiveData<List<Category>>

    init {
        categories = categoryRepository.allCategories

        liveStatisticsUpdater = LiveStatisticsUpdater(
            application,
            subscriptionRepository.allSubscriptions,
            categories,
            viewModelScope,
        )

        totalMonthlySpending = liveStatisticsUpdater.totalMonthlySpending
        totalYearlySpending = liveStatisticsUpdater.totalYearlySpending
        spendingByCategory = liveStatisticsUpdater.spendingByCategory
        monthlySpendingHistory = liveStatisticsUpdater.monthlySpendingHistory
        subscriptionTypeSpending = liveStatisticsUpdater.subscriptionTypeSpending
        isLoading = liveStatisticsUpdater.isLoading
    }

    data class CategorySpending(
        val categoryId: Long?,
        val categoryName: String,
        val colorCode: String,
        val amount: Double
    )

    data class MonthlySpending(
        val month: String,
        val amount: Double
    )

    data class SubscriptionTypeSpending(
        val type: String,
        val amount: Double,
        val colorCode: String
    )
}