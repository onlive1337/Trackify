package com.onlive.trackify.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.viewmodel.StatisticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.onlive.trackify.R
import java.util.concurrent.ConcurrentHashMap

class LiveStatisticsUpdater(
    private val context: Context,
    subscriptionsLiveData: LiveData<List<Subscription>>,
    categoriesLiveData: LiveData<List<Category>>
) {
    val totalMonthlySpending = MediatorLiveData<Double>()
    val totalYearlySpending = MediatorLiveData<Double>()
    val spendingByCategory = MediatorLiveData<List<StatisticsViewModel.CategorySpending>>()
    val monthlySpendingHistory = MediatorLiveData<List<StatisticsViewModel.MonthlySpending>>()
    val subscriptionTypeSpending = MediatorLiveData<List<StatisticsViewModel.SubscriptionTypeSpending>>()

    private val subscriptions = ConcurrentHashMap<Long, Subscription>()
    private val categories = ConcurrentHashMap<Long, Category>()

    private val calculationScope = CoroutineScope(Dispatchers.Default)
    private var debounceJob: Job? = null

    private var cachedMonthlySpending: Double? = null
    private var cachedYearlySpending: Double? = null
    private var lastCalculationTime = 0L
    private val cacheValidTime = 30_000L

    private val tag = "LiveStatisticsUpdater"
    private val debounceDelay = 300L

    init {
        totalMonthlySpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            debouncedCalculateMonthlySpending()
        }

        totalYearlySpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            debouncedCalculateYearlySpending()
        }

        spendingByCategory.apply {
            addSource(subscriptionsLiveData) { subs ->
                updateSubscriptions(subs)
                debouncedCalculateSpendingByCategory()
            }
            addSource(categoriesLiveData) { cats ->
                updateCategories(cats)
                debouncedCalculateSpendingByCategory()
            }
        }

        monthlySpendingHistory.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            debouncedCalculateMonthlySpendingHistory()
        }

        subscriptionTypeSpending.addSource(subscriptionsLiveData) { subs ->
            updateSubscriptions(subs)
            debouncedCalculateSpendingBySubscriptionType()
        }
    }

    private fun updateSubscriptions(subs: List<Subscription>) {
        try {
            subscriptions.clear()
            subs.forEach { subscription ->
                subscriptions[subscription.subscriptionId] = subscription
            }
            invalidateCache()
        } catch (e: Exception) {
            Log.e(tag, "Error updating subscriptions", e)
        }
    }

    private fun updateCategories(cats: List<Category>) {
        try {
            categories.clear()
            cats.forEach { category ->
                categories[category.categoryId] = category
            }
            invalidateCache()
        } catch (e: Exception) {
            Log.e(tag, "Error updating categories", e)
        }
    }

    private fun invalidateCache() {
        cachedMonthlySpending = null
        cachedYearlySpending = null
        lastCalculationTime = 0L
    }

    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - lastCalculationTime < cacheValidTime
    }

    private fun debouncedCalculateMonthlySpending() {
        debounceJob?.cancel()
        debounceJob = calculationScope.launch {
            delay(debounceDelay)
            calculateMonthlySpending()
        }
    }

    private fun debouncedCalculateYearlySpending() {
        debounceJob?.cancel()
        debounceJob = calculationScope.launch {
            delay(debounceDelay)
            calculateYearlySpending()
        }
    }

    private fun debouncedCalculateSpendingByCategory() {
        debounceJob?.cancel()
        debounceJob = calculationScope.launch {
            delay(debounceDelay)
            calculateSpendingByCategory()
        }
    }

    private fun debouncedCalculateMonthlySpendingHistory() {
        debounceJob?.cancel()
        debounceJob = calculationScope.launch {
            delay(debounceDelay)
            calculateMonthlySpendingHistory()
        }
    }

    private fun debouncedCalculateSpendingBySubscriptionType() {
        debounceJob?.cancel()
        debounceJob = calculationScope.launch {
            delay(debounceDelay)
            calculateSpendingBySubscriptionType()
        }
    }

    fun calculateMonthlySpending() {
        calculationScope.launch {
            try {
                if (isCacheValid() && cachedMonthlySpending != null) {
                    totalMonthlySpending.postValue(cachedMonthlySpending!!)
                    return@launch
                }

                var monthlyCost = 0.0

                for (subscription in subscriptions.values) {
                    monthlyCost += when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> subscription.price
                        BillingFrequency.YEARLY -> subscription.price / 12
                    }
                }

                cachedMonthlySpending = monthlyCost
                lastCalculationTime = System.currentTimeMillis()
                totalMonthlySpending.postValue(monthlyCost)
            } catch (e: Exception) {
                Log.e(tag, "Error calculating monthly spending", e)
                totalMonthlySpending.postValue(0.0)
            }
        }
    }

    fun calculateYearlySpending() {
        calculationScope.launch {
            try {
                if (isCacheValid() && cachedYearlySpending != null) {
                    totalYearlySpending.postValue(cachedYearlySpending!!)
                    return@launch
                }

                var yearlyCost = 0.0

                for (subscription in subscriptions.values) {
                    yearlyCost += when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> subscription.price * 12
                        BillingFrequency.YEARLY -> subscription.price
                    }
                }

                cachedYearlySpending = yearlyCost
                lastCalculationTime = System.currentTimeMillis()
                totalYearlySpending.postValue(yearlyCost)
            } catch (e: Exception) {
                Log.e(tag, "Error calculating yearly spending", e)
                totalYearlySpending.postValue(0.0)
            }
        }
    }

    fun calculateSpendingByCategory() {
        calculationScope.launch {
            try {
                val categorySpendingMap = mutableMapOf<Long?, Double>()

                categorySpendingMap[null] = 0.0
                categories.values.forEach { category ->
                    categorySpendingMap[category.categoryId] = 0.0
                }

                for (subscription in subscriptions.values) {
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
                        categoryName = category?.name ?: context.getString(R.string.without_category),
                        colorCode = category?.colorCode ?: "#808080",
                        amount = amount
                    )
                }.filter { it.amount > 0 }
                    .sortedByDescending { it.amount }

                spendingByCategory.postValue(result)
            } catch (e: Exception) {
                Log.e(tag, "Error calculating spending by category", e)
                spendingByCategory.postValue(emptyList())
            }
        }
    }

    fun calculateMonthlySpendingHistory(monthsCount: Int = 6) {
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
                        val subscriptionStart = Calendar.getInstance().apply { time = subscription.startDate }
                        val startYear = subscriptionStart.get(Calendar.YEAR)
                        val startMonth = subscriptionStart.get(Calendar.MONTH)

                        if ((year > startYear) || (year == startYear && month >= startMonth)) {
                            if (subscription.endDate != null) {
                                val subscriptionEnd = Calendar.getInstance().apply { time =
                                    subscription.endDate
                                }
                                val endYear = subscriptionEnd.get(Calendar.YEAR)
                                val endMonth = subscriptionEnd.get(Calendar.MONTH)

                                if (year > endYear || (year == endYear && month > endMonth)) {
                                    return@forEach
                                }
                            }

                            totalAmount += when (subscription.billingFrequency) {
                                BillingFrequency.MONTHLY -> subscription.price
                                BillingFrequency.YEARLY -> {
                                    val paymentMonth = subscriptionStart.get(Calendar.MONTH)
                                    if (month == paymentMonth) {
                                        subscription.price
                                    } else {
                                        subscription.price / 12
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
                Log.e(tag, "Error calculating monthly spending history", e)
                monthlySpendingHistory.postValue(emptyList())
            }
        }
    }

    fun calculateSpendingBySubscriptionType() {
        calculationScope.launch {
            try {
                var monthlyTotal = 0.0
                var yearlyTotal = 0.0

                for (subscription in subscriptions.values) {
                    when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> monthlyTotal += subscription.price
                        BillingFrequency.YEARLY -> yearlyTotal += subscription.price / 12
                    }
                }

                val result = listOf(
                    StatisticsViewModel.SubscriptionTypeSpending(
                        "monthly_spending",
                        monthlyTotal,
                        "#4285F4"
                    ),
                    StatisticsViewModel.SubscriptionTypeSpending(
                        "yearly_type",
                        yearlyTotal,
                        "#34A853"
                    )
                )

                subscriptionTypeSpending.postValue(result)
            } catch (e: Exception) {
                Log.e(tag, "Error calculating subscription type spending", e)
                subscriptionTypeSpending.postValue(emptyList())
            }
        }
    }

    private fun getMonthName(month: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e(tag, "Error getting month name", e)
            context.getString(R.string.month_fallback, month)
        }
    }
}