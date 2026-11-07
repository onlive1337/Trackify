package com.onlive.trackify.data

import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.viewmodel.StatisticsViewModel
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class LiveStatisticsUpdater(
    subscriptionsFlow: Flow<List<SubscriptionWithCategory>>,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    val statisticsFlow: Flow<StatisticsViewModel.UiState> = subscriptionsFlow
        .map { subscriptions -> calculateStatistics(subscriptions = subscriptions) }
        .flowOn(defaultDispatcher)
        .distinctUntilChanged()
        .catch {
            emit(StatisticsViewModel.UiState(isLoading = false))
        }

    companion object {
        private const val DEFAULT_CATEGORY_COLOR = "#808080"

        fun calculateStatistics(
            subscriptions: List<SubscriptionWithCategory>,
            referenceDate: LocalDate = LocalDate.now()
        ): StatisticsViewModel.UiState {
            val activeSubscriptions = subscriptions.filter { item ->
                val endDate = item.subscription.endDate
                endDate == null || !endDate.isBefore(referenceDate)
            }

            var totalMonthly = 0.0
            var totalYearly = 0.0
            val categorySpendingMap = mutableMapOf<Long?, Double>()
            val categoryLookup = mutableMapOf<Long, Category>()

            for ((subscription, category) in activeSubscriptions) {
                val safePrice = subscription.price.takeIf { it.isFinite() && it > 0.0 } ?: continue
                category?.let { categoryLookup[it.categoryId] = it }

                val monthlyCost = when (subscription.billingFrequency) {
                    BillingFrequency.MONTHLY -> safePrice
                    BillingFrequency.YEARLY -> safePrice / 12.0
                }
                val yearlyCost = when (subscription.billingFrequency) {
                    BillingFrequency.MONTHLY -> safePrice * 12.0
                    BillingFrequency.YEARLY -> safePrice
                }

                totalMonthly += monthlyCost
                totalYearly += yearlyCost
                categorySpendingMap[subscription.categoryId] =
                    (categorySpendingMap[subscription.categoryId] ?: 0.0) + monthlyCost
            }

            val spendingByCategory = categorySpendingMap
                .filterValues { it > 0.0 }
                .map { (categoryId, amount) ->
                    val category = categoryId?.let { categoryLookup[it] }
                    StatisticsViewModel.CategorySpending(
                        categoryId = categoryId,
                        categoryName = category?.name,
                        colorCode = category?.colorCode ?: DEFAULT_CATEGORY_COLOR,
                        amount = amount
                    )
                }
                .sortedByDescending { it.amount }

            return StatisticsViewModel.UiState(
                isLoading = false,
                totalMonthlySpending = totalMonthly,
                totalYearlySpending = totalYearly,
                spendingByCategory = spendingByCategory
            )
        }
    }
}
