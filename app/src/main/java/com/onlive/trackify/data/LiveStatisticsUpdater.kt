package com.onlive.trackify.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.viewmodel.StatisticsViewModel
import java.util.Calendar
import java.util.Date

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

    private var subscriptions = listOf<Subscription>()
    private var payments = listOf<Payment>()
    private var categories = listOf<Category>()

    init {
        totalMonthlySpending.addSource(subscriptionsLiveData) { subs ->
            subscriptions = subs
            calculateMonthlySpending()
        }

        totalYearlySpending.addSource(subscriptionsLiveData) { subs ->
            subscriptions = subs
            calculateYearlySpending()
        }

        spendingByCategory.apply {
            addSource(subscriptionsLiveData) { subs ->
                subscriptions = subs
                calculateSpendingByCategory()
            }
            addSource(categoriesLiveData) { cats ->
                categories = cats
                calculateSpendingByCategory()
            }
        }

        monthlySpendingHistory.addSource(subscriptionsLiveData) { subs ->
            subscriptions = subs
            calculateMonthlySpendingHistory()
        }

        subscriptionTypeSpending.addSource(subscriptionsLiveData) { subs ->
            subscriptions = subs
            calculateSpendingBySubscriptionType()
        }
    }

    private fun calculateMonthlySpending() {
        var monthlyCost = 0.0

        for (subscription in subscriptions) {
            if (!subscription.active) continue

            monthlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price
                BillingFrequency.YEARLY -> subscription.price / 12
            }
        }

        totalMonthlySpending.postValue(monthlyCost)
    }

    private fun calculateYearlySpending() {
        var yearlyCost = 0.0

        for (subscription in subscriptions) {
            if (!subscription.active) continue

            yearlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price * 12
                BillingFrequency.YEARLY -> subscription.price
            }
        }

        totalYearlySpending.postValue(yearlyCost)
    }

    private fun calculateSpendingByCategory() {
        val categorySpendingMap = mutableMapOf<Long?, Double>()

        categorySpendingMap[null] = 0.0

        categories.forEach { category ->
            categorySpendingMap[category.categoryId] = 0.0
        }

        for (subscription in subscriptions) {
            if (!subscription.active) continue

            val monthlyCost = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price
                BillingFrequency.YEARLY -> subscription.price / 12
            }

            val currentAmount = categorySpendingMap[subscription.categoryId] ?: 0.0
            categorySpendingMap[subscription.categoryId] = currentAmount + monthlyCost
        }

        val result = categorySpendingMap.map { (categoryId, amount) ->
            val category = if (categoryId != null) categories.find { it.categoryId == categoryId } else null
            StatisticsViewModel.CategorySpending(
                categoryId = categoryId,
                categoryName = category?.name ?: "Без категории",
                colorCode = category?.colorCode ?: "#808080",
                amount = amount
            )
        }.filter { it.amount > 0 }
            .sortedByDescending { it.amount }

        spendingByCategory.postValue(result)
    }

    private fun calculateMonthlySpendingHistory(monthsCount: Int = 6) {
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

            for (subscription in subscriptions) {
                if (!subscription.active) continue

                val subscriptionStart = Calendar.getInstance().apply { time = subscription.startDate }
                val startYear = subscriptionStart.get(Calendar.YEAR)
                val startMonth = subscriptionStart.get(Calendar.MONTH)

                if ((year > startYear) || (year == startYear && month >= startMonth)) {
                    if (subscription.endDate != null) {
                        val subscriptionEnd = Calendar.getInstance().apply { time = subscription.endDate!! }
                        val endYear = subscriptionEnd.get(Calendar.YEAR)
                        val endMonth = subscriptionEnd.get(Calendar.MONTH)

                        if (year > endYear || (year == endYear && month > endMonth)) {
                            continue
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
    }

    private fun calculateSpendingBySubscriptionType() {
        var monthlyTotal = 0.0
        var yearlyTotal = 0.0

        for (subscription in subscriptions) {
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