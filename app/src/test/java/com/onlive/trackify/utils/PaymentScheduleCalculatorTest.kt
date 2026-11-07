package com.onlive.trackify.utils

import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionSortOption
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentScheduleCalculatorTest {

    private fun subscription(
        id: Long,
        name: String,
        price: Double,
        startDate: LocalDate,
        frequency: BillingFrequency = BillingFrequency.MONTHLY,
        endDate: LocalDate? = null
    ) = Subscription(
        subscriptionId = id,
        name = name,
        description = null,
        price = price,
        billingFrequency = frequency,
        startDate = startDate,
        endDate = endDate,
        categoryId = null
    )

    @Test
    fun `resolveDueStatus classifies today tomorrow soon upcoming ending and expired`() {
        val today = LocalDate.of(2026, 11, 1)

        val dueToday = subscription(1, "Today Sub", 10.0, LocalDate.of(2026, 10, 1))
        assertTrue(PaymentScheduleCalculator.resolveDueStatus(dueToday, today) is SubscriptionDueStatus.DueToday)

        val dueTomorrow = subscription(2, "Tomorrow Sub", 10.0, LocalDate.of(2026, 10, 2))
        assertTrue(PaymentScheduleCalculator.resolveDueStatus(dueTomorrow, today) is SubscriptionDueStatus.DueTomorrow)

        val dueInThreeDays = subscription(3, "Soon Sub", 10.0, LocalDate.of(2026, 10, 4))
        val soonStatus = PaymentScheduleCalculator.resolveDueStatus(dueInThreeDays, today)
        assertTrue(soonStatus is SubscriptionDueStatus.DueSoon)
        assertEquals(3, (soonStatus as SubscriptionDueStatus.DueSoon).daysUntil)

        val upcoming = subscription(4, "Later Sub", 10.0, LocalDate.of(2026, 10, 15))
        val upcomingStatus = PaymentScheduleCalculator.resolveDueStatus(upcoming, today)
        assertTrue(upcomingStatus is SubscriptionDueStatus.Upcoming)
        assertEquals(14, (upcomingStatus as SubscriptionDueStatus.Upcoming).daysUntil)

        val endingNoRenewal = subscription(
            5,
            "Ending Sub",
            10.0,
            startDate = LocalDate.of(2026, 10, 20),
            endDate = LocalDate.of(2026, 11, 10)
        )
        assertTrue(
            PaymentScheduleCalculator.resolveDueStatus(endingNoRenewal, today) is SubscriptionDueStatus.EndingWithoutRenewal
        )

        val expired = subscription(
            6,
            "Expired Sub",
            10.0,
            startDate = LocalDate.of(2026, 8, 1),
            endDate = LocalDate.of(2026, 10, 15)
        )
        assertTrue(PaymentScheduleCalculator.resolveDueStatus(expired, today) is SubscriptionDueStatus.Expired)
    }

    @Test
    fun `calculateNextPaymentDate preserves month-end and leap-day anchors`() {
        val jan31 = LocalDate.of(2026, 1, 31)
        assertEquals(
            LocalDate.of(2026, 2, 28),
            PaymentScheduleCalculator.calculateNextPaymentDate(
                startDate = jan31,
                billingFrequency = BillingFrequency.MONTHLY,
                endDate = null,
                today = LocalDate.of(2026, 2, 15)
            )
        )
        assertEquals(
            LocalDate.of(2026, 3, 31),
            PaymentScheduleCalculator.calculateNextPaymentDate(
                startDate = jan31,
                billingFrequency = BillingFrequency.MONTHLY,
                endDate = null,
                today = LocalDate.of(2026, 3, 1)
            )
        )

        val leapDay = LocalDate.of(2024, 2, 29)
        assertEquals(
            LocalDate.of(2025, 2, 28),
            PaymentScheduleCalculator.calculateNextPaymentDate(
                startDate = leapDay,
                billingFrequency = BillingFrequency.YEARLY,
                endDate = null,
                today = LocalDate.of(2025, 2, 1)
            )
        )
        assertEquals(
            LocalDate.of(2028, 2, 29),
            PaymentScheduleCalculator.calculateNextPaymentDate(
                startDate = leapDay,
                billingFrequency = BillingFrequency.YEARLY,
                endDate = null,
                today = LocalDate.of(2028, 2, 28)
            )
        )
    }

    @Test
    fun `sortSubscriptions orders nearest payments first and expired last`() {
        val today = LocalDate.of(2026, 11, 1)

        val expired = subscription(1, "Old Service", 5.0, LocalDate.of(2026, 7, 1), endDate = LocalDate.of(2026, 9, 1))
        val upcoming = subscription(2, "Cloud Storage", 9.99, LocalDate.of(2026, 10, 15))
        val tomorrow = subscription(3, "Music", 6.99, LocalDate.of(2026, 10, 2))
        val dueToday = subscription(4, "Video", 14.99, LocalDate.of(2026, 10, 1))
        val endingSoon = subscription(5, "Trial", 4.99, LocalDate.of(2026, 10, 20), endDate = LocalDate.of(2026, 11, 5))

        val sorted = PaymentScheduleCalculator.sortSubscriptions(
            listOf(expired, upcoming, tomorrow, dueToday, endingSoon),
            SubscriptionSortOption.NEAREST_PAYMENT,
            today
        )

        assertEquals(
            listOf("Video", "Music", "Cloud Storage", "Trial", "Old Service"),
            sorted.map { it.name }
        )
    }

    @Test
    fun `sortSubscriptions supports name and price ordering`() {
        val today = LocalDate.of(2026, 11, 1)
        val subs = listOf(
            subscription(1, "Spotify", 9.99, today),
            subscription(2, "Apple One", 19.95, today),
            subscription(3, "iCloud", 2.99, today)
        )

        assertEquals(
            listOf("Apple One", "iCloud", "Spotify"),
            PaymentScheduleCalculator.sortSubscriptions(subs, SubscriptionSortOption.NAME_ASC, today).map { it.name }
        )
        assertEquals(
            listOf("Apple One", "Spotify", "iCloud"),
            PaymentScheduleCalculator.sortSubscriptions(subs, SubscriptionSortOption.PRICE_DESC, today).map { it.name }
        )
        assertEquals(
            listOf("iCloud", "Spotify", "Apple One"),
            PaymentScheduleCalculator.sortSubscriptions(subs, SubscriptionSortOption.PRICE_ASC, today).map { it.name }
        )
    }
}
