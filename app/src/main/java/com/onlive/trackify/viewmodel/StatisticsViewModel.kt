package com.onlive.trackify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.repository.CategoryRepository
import com.onlive.trackify.data.repository.PaymentRepository
import com.onlive.trackify.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository: SubscriptionRepository
    private val paymentRepository: PaymentRepository
    private val categoryRepository: CategoryRepository

    private val _totalMonthlySpending = MutableLiveData<Double>()
    val totalMonthlySpending: LiveData<Double> = _totalMonthlySpending

    private val _totalYearlySpending = MutableLiveData<Double>()
    val totalYearlySpending: LiveData<Double> = _totalYearlySpending

    private val _spendingByCategory = MutableLiveData<List<CategorySpending>>()
    val spendingByCategory: LiveData<List<CategorySpending>> = _spendingByCategory

    private val _monthlySpendingHistory = MutableLiveData<List<MonthlySpending>>()
    val monthlySpendingHistory: LiveData<List<MonthlySpending>> = _monthlySpendingHistory

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _subscriptionTypeSpending = MutableLiveData<List<SubscriptionTypeSpending>>()
    val subscriptionTypeSpending: LiveData<List<SubscriptionTypeSpending>> = _subscriptionTypeSpending

    init {
        val database = AppDatabase.getDatabase(application)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        paymentRepository = PaymentRepository(database.paymentDao())
        categoryRepository = CategoryRepository(database.categoryDao())

        loadCategories()
    }

    private fun loadCategories() = viewModelScope.launch {
        categoryRepository.allCategories.observeForever { categories ->
            _categories.value = categories
        }
    }

    fun calculateMonthlySpending() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        var monthlyCost = 0.0

        for (subscription in subscriptions) {
            monthlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price
                BillingFrequency.YEARLY -> subscription.price / 12
            }
        }

        _totalMonthlySpending.postValue(monthlyCost)
    }

    fun calculateYearlySpending() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        var yearlyCost = 0.0

        for (subscription in subscriptions) {
            yearlyCost += when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price * 12
                BillingFrequency.YEARLY -> subscription.price
            }
        }

        _totalYearlySpending.postValue(yearlyCost)
    }

    fun calculateSpendingByCategory() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        val categoriesMap = categories.value?.associateBy { it.categoryId } ?: emptyMap()
        val categorySpendingMap = mutableMapOf<Long?, Double>()

        categorySpendingMap[null] = 0.0

        categoriesMap.keys.forEach { categoryId ->
            categorySpendingMap[categoryId] = 0.0
        }

        for (subscription in subscriptions) {
            val monthlyCost = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> subscription.price
                BillingFrequency.YEARLY -> subscription.price / 12
            }

            val currentAmount = categorySpendingMap[subscription.categoryId] ?: 0.0
            categorySpendingMap[subscription.categoryId] = currentAmount + monthlyCost
        }

        val result = categorySpendingMap.map { (categoryId, amount) ->
            val category = if (categoryId != null) categoriesMap[categoryId] else null
            CategorySpending(
                categoryId = categoryId,
                categoryName = category?.name ?: "Без категории",
                colorCode = category?.colorCode ?: "#808080",
                amount = amount
            )
        }.filter { it.amount > 0 }
            .sortedByDescending { it.amount }

        _spendingByCategory.postValue(result)
    }

    fun calculateSpendingBySubscriptionType() = viewModelScope.launch {
        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

        var monthlyTotal = 0.0
        var yearlyTotal = 0.0

        for (subscription in subscriptions) {
            when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> monthlyTotal += subscription.price
                BillingFrequency.YEARLY -> yearlyTotal += subscription.price / 12 // Конвертируем в месячную сумму
            }
        }

        val result = listOf(
            SubscriptionTypeSpending("Ежемесячные", monthlyTotal, "#4285F4"),
            SubscriptionTypeSpending("Ежегодные (в месяц)", yearlyTotal, "#34A853")
        )

        _subscriptionTypeSpending.postValue(result)
    }

    fun calculateMonthlySpendingHistory(monthsCount: Int = 6) = viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val monthlyData = mutableListOf<MonthlySpending>()

        val subscriptions = withContext(Dispatchers.IO) {
            subscriptionRepository.getActiveSubscriptionsSync()
        }

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
                                totalAmount += subscription.price / 12
                            } else {
                                totalAmount += subscription.price / 12
                            }
                        }
                    }
                }
            }

            monthlyData.add(MonthlySpending(monthName, totalAmount))
        }

        monthlyData.reverse()

        _monthlySpendingHistory.postValue(monthlyData)
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