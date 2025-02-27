package com.onlive.trackify.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.viewmodel.StatisticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

import java.util.concurrent.ConcurrentHashMap

class LiveStatisticsUpdater(
    subscriptionsLiveData: LiveData<List<Subscription>>,
    paymentsLiveData: LiveData<List<Payment>>,
    categoriesLiveData: LiveData<List<Category>>
) {
    val totalMonthlySpending = MediatorLiveData<Double>()
    val totalYearlySpending = MediatorLiveData<Double>()

    val spendingByCategory = MediatorLiveData<List<StatisticsViewModel.CategorySpending>>()

    val monthlySpendingHistory = MediatorLiveData<List<StatisticsViewModel.MonthlySpending>>()

    val subscriptionTypeSpending = MediatorLiveData<List<StatisticsViewModel.SubscriptionTypeSpending>>()

    private val subscriptions = ConcurrentHashMap<Long, Subscription>()
    private val payments = ConcurrentHashMap<Long, Payment>()
    private val categories = ConcurrentHashMap<Long, Category>()

    private val calculationScope = CoroutineScope(Dispatchers.Default)

    init {
        totalMonthlySpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            calculateMonthlySpending()
        }

        totalYearlySpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            calculateYearlySpending()
        }

        spendingByCategory.apply {
            addSource(subscriptionsLiveData) { subs ->
                updateSubscriptions(subs)
                calculateSpendingByCategory()
            }
            addSource(categoriesLiveData) { cats ->
                updateCategories(cats)
                calculateSpendingByCategory()
            }
        }

        monthlySpendingHistory.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            calculateMonthlySpendingHistory()
        }

        subscriptionTypeSpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            calculateSpendingBySubscriptionType()
        }
    }

    private fun updateSubscriptions(subs: List<Subscription>) {
        subscriptions.clear()
        subs.forEach { subscription ->
            subscriptions[subscription.subscriptionId] = subscription
        }
    }

    private fun updateCategories(cats: List<Category>) {
        categories.clear()
        cats.forEach { category ->
            category.categoryId.let { categories[it] = category }
        }
    }

    private fun calculateMonthlySpending() {
        calculationScope.launch {
            try {
                var monthlyCost = 0.0

                for (subscription in subscriptions.values) {
                    if (!subscription.active) continue

                    monthlyCost += when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> subscription.price
                        BillingFrequency.YEARLY -> subscription.price / 12
                    }
                }

                totalMonthlySpending.postValue(monthlyCost)
            } catch (e: Exception) {
                Log.e("LiveStatisticsUpdater", "Error calculating monthly spending", e)
                totalMonthlySpending.postValue(0.0)
            }
        }
    }

    private fun calculateYearlySpending() {
        calculationScope.launch {
            try {
                var yearlyCost = 0.0

                for (subscription in subscriptions.values) {
                    if (!subscription.active) continue

                    yearlyCost += when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> subscription.price * 12
                        BillingFrequency.YEARLY -> subscription.price
                    }
                }

                totalYearlySpending.postValue(yearlyCost)
            } catch (e: Exception) {
                Log.e("LiveStatisticsUpdater", "Error calculating yearly spending", e)
                totalYearlySpending.postValue(0.0)
            }
        }
    }

    private fun calculateSpendingByCategory() {
        calculationScope.launch {
            try {
                val categorySpendingMap = mutableMapOf<Long?, Double>()

                categorySpendingMap[null] = 0.0
                categories.values.forEach { category ->
                    categorySpendingMap[category.categoryId] = 0.0
                }

                for (subscription in subscriptions.values) {
                    if (!subscription.active) continue

                    val monthlyCost = when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> subscription.price
                        BillingFrequency.YEARLY -> subscription.price / 12
                    }

                    val currentAmount = categorySpendingMap[subscription.categoryId] ?: 0.0
                    categorySpendingMap[subscription.categoryId] = currentAmount + monthlyCost
                }

                val result = categorySpendingMap.map { (categoryId, amount) ->
                    val category = if (categoryId != null) categories[categoryId] else null
                    StatisticsViewModel.CategorySpending(
                        categoryId = categoryId,
                        categoryName = category?.name ?: "Без категории",
                        colorCode = category?.colorCode ?: "#808080",
                        amount = amount
                    )
                }.filter { it.amount > 0 }
                    .sortedByDescending { it.amount }

                spendingByCategory.postValue(result)
            } catch (e: Exception) {
                Log.e("LiveStatisticsUpdater", "Error calculating spending by category", e)
                spendingByCategory.postValue(emptyList())
            }
        }
    }

    private fun calculateMonthlySpendingHistory(monthsCount: Int = 6) {
        calculationScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)

                val monthlyData = mutableListOf<StatisticsViewModel.MonthlySpending>()

                for (i in 0 until monthsCount) {
                    var month = currentMonth - i
                    var year = currentYear

                    while (month < 0) {
                        month += 12
                        year -= 1
                    }

                    val monthName = getMonthName(month)
                    var totalAmount = 0.0

                    subscriptions.values.forEach { subscription ->
                        if (!subscription.active) return@forEach

                        val subscriptionStart = Calendar.getInstance().apply { time = subscription.startDate }
                        val startYear = subscriptionStart.get(Calendar.YEAR)
                        val startMonth = subscriptionStart.get(Calendar.MONTH)

                        if ((year > startYear) || (year == startYear && month >= startMonth)) {
                            if (subscription.endDate != null) {
                                val subscriptionEnd = Calendar.getInstance().apply { time = subscription.endDate!! }
                                val endYear = subscriptionEnd.get(Calendar.YEAR)
                                val endMonth = subscriptionEnd.get(Calendar.MONTH)

                                if (year > endYear || (year == endYear && month > endMonth)) {
                                    return@forEach
                                }
                            }

                            when (subscription.billingFrequency) {
                                BillingFrequency.MONTHLY -> totalAmount += subscription.price
                                BillingFrequency.YEARLY -> {
                                    val paymentMonth = subscriptionStart.get(Calendar.MONTH)
                                    if (month == paymentMonth) {
                                        totalAmount += subscription.price
                                    } else {
                                        totalAmount += subscription.price / 12
                                    }
                                }
                            }
                        }
                    }

                    monthlyData.add(StatisticsViewModel.MonthlySpending(monthName, totalAmount))
                }

                monthlyData.reverse()
                monthlySpendingHistory.postValue(monthlyData)
            } catch (e: Exception) {
                Log.e("LiveStatisticsUpdater", "Error calculating monthly spending history", e)
                monthlySpendingHistory.postValue(emptyList())
            }
        }
    }

    private fun calculateSpendingBySubscriptionType() {
        calculationScope.launch {
            try {
                var monthlyTotal = 0.0
                var yearlyTotal = 0.0

                for (subscription in subscriptions.values) {
                    if (!subscription.active) continue

                    when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> monthlyTotal += subscription.price
                        BillingFrequency.YEARLY -> yearlyTotal += subscription.price / 12
                    }
                }

                val result = listOf(
                    StatisticsViewModel.SubscriptionTypeSpending("Ежемесячные", monthlyTotal, "#4285F4"),
                    StatisticsViewModel.SubscriptionTypeSpending("Ежегодные (в месяц)", yearlyTotal, "#34A853")
                )

                subscriptionTypeSpending.postValue(result)
            } catch (e: Exception) {
                Log.e("LiveStatisticsUpdater", "Error calculating subscription type spending", e)
                subscriptionTypeSpending.postValue(emptyList())
            }
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "Янв"
            Calendar.FEBRUARY -> "Фев"
            Calendar.MARCH -> "Мар"
            Calendar.APRIL -> "Апр"
            Calendar.MAY -> "Май"
            Calendar.JUNE -> "Июн"
            Calendar.JULY -> "Июл"
            Calendar.AUGUST -> "Авг"
            Calendar.SEPTEMBER -> "Сен"
            Calendar.OCTOBER -> "Окт"
            Calendar.NOVEMBER -> "Ноя"
            Calendar.DECEMBER -> "Дек"
            else -> "???"
        }
    }
}