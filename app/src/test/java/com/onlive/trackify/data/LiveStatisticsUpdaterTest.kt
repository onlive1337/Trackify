package com.onlive.trackify.data

import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionWithCategory
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LiveStatisticsUpdaterTest {

    @Test
    fun `calculateStatistics sums active monthly and yearly subscriptions and groups by category`() {
        val today = LocalDate.of(2026, 11, 2)
        val mediaCategory = Category(categoryId = 1L, name = "Media", colorCode = "#FF5252")
        val workCategory = Category(categoryId = 2L, name = "Work", colorCode = "#448AFF")

        val subscriptions = listOf(
            SubscriptionWithCategory(
                subscription = Subscription(
                    subscriptionId = 1L,
                    name = "Music",
                    description = null,
                    price = 10.0,
                    billingFrequency = BillingFrequency.MONTHLY,
                    startDate = LocalDate.of(2026, 1, 1),
                    endDate = null,
                    categoryId = 1L
                ),
                category = mediaCategory
            ),
            SubscriptionWithCategory(
                subscription = Subscription(
                    subscriptionId = 2L,
                    name = "IDE",
                    description = null,
                    price = 120.0,
                    billingFrequency = BillingFrequency.YEARLY,
                    startDate = LocalDate.of(2026, 1, 1),
                    endDate = null,
                    categoryId = 2L
                ),
                category = workCategory
            ),
            SubscriptionWithCategory(
                subscription = Subscription(
                    subscriptionId = 3L,
                    name = "Uncategorized VPN",
                    description = null,
                    price = 5.0,
                    billingFrequency = BillingFrequency.MONTHLY,
                    startDate = LocalDate.of(2026, 5, 1),
                    endDate = null,
                    categoryId = null
                ),
                category = null
            ),
            SubscriptionWithCategory(
                subscription = Subscription(
                    subscriptionId = 4L,
                    name = "Expired Trial",
                    description = null,
                    price = 50.0,
                    billingFrequency = BillingFrequency.MONTHLY,
                    startDate = LocalDate.of(2026, 9, 1),
                    endDate = LocalDate.of(2026, 10, 15),
                    categoryId = 1L
                ),
                category = mediaCategory
            )
        )

        val state = LiveStatisticsUpdater.calculateStatistics(
            subscriptions = subscriptions,
            referenceDate = today
        )

        assertFalse(state.isLoading)
        assertEquals(25.0, state.totalMonthlySpending, 0.001)
        assertEquals(300.0, state.totalYearlySpending, 0.001)
        assertEquals(3, state.spendingByCategory.size)
        assertEquals(5.0, state.spendingByCategory.last().amount, 0.001)
        assertNull(state.spendingByCategory.last().categoryName)
    }
}
