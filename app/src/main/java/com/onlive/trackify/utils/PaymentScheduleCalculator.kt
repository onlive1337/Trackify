package com.onlive.trackify.utils

import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionSortOption
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface SubscriptionDueStatus {
    data class DueToday(val date: LocalDate) : SubscriptionDueStatus
    data class DueTomorrow(val date: LocalDate) : SubscriptionDueStatus
    data class DueSoon(val date: LocalDate, val daysUntil: Int) : SubscriptionDueStatus
    data class Upcoming(val date: LocalDate, val daysUntil: Int) : SubscriptionDueStatus
    data class EndingWithoutRenewal(val endDate: LocalDate, val daysUntilEnd: Int) : SubscriptionDueStatus
    data class Expired(val endDate: LocalDate) : SubscriptionDueStatus
}

object PaymentScheduleCalculator {

    fun calculateNextPaymentDate(
        startDate: LocalDate,
        billingFrequency: BillingFrequency,
        endDate: LocalDate?,
        today: LocalDate
    ): LocalDate? {
        if (startDate.isAfter(today)) {
            return if (endDate != null && startDate.isAfter(endDate)) null else startDate
        }

        val nextPaymentDate = when (billingFrequency) {
            BillingFrequency.MONTHLY -> {
                var months = ChronoUnit.MONTHS.between(
                    startDate.withDayOfMonth(1),
                    today.withDayOfMonth(1)
                )
                var candidate = startDate.plusMonths(months)
                if (candidate.isBefore(today)) {
                    months += 1
                    candidate = startDate.plusMonths(months)
                }
                candidate
            }

            BillingFrequency.YEARLY -> {
                var years = (today.year - startDate.year).toLong()
                var candidate = startDate.plusYears(years)
                if (candidate.isBefore(today)) {
                    years += 1
                    candidate = startDate.plusYears(years)
                }
                candidate
            }
        }

        if (endDate != null && nextPaymentDate.isAfter(endDate)) {
            return null
        }
        return nextPaymentDate
    }

    fun getDaysDifference(from: LocalDate, to: LocalDate): Int {
        return ChronoUnit.DAYS.between(from, to).toInt()
    }

    fun resolveDueStatus(subscription: Subscription, today: LocalDate): SubscriptionDueStatus {
        val nextPaymentDate = calculateNextPaymentDate(
            startDate = subscription.startDate,
            billingFrequency = subscription.billingFrequency,
            endDate = subscription.endDate,
            today = today
        )

        if (nextPaymentDate != null) {
            val daysUntil = getDaysDifference(today, nextPaymentDate)
            return when {
                daysUntil <= 0 -> SubscriptionDueStatus.DueToday(nextPaymentDate)
                daysUntil == 1 -> SubscriptionDueStatus.DueTomorrow(nextPaymentDate)
                daysUntil in 2..3 -> SubscriptionDueStatus.DueSoon(nextPaymentDate, daysUntil)
                else -> SubscriptionDueStatus.Upcoming(nextPaymentDate, daysUntil)
            }
        }

        val endDate = subscription.endDate
            ?: return SubscriptionDueStatus.Upcoming(subscription.startDate, 0)
        return if (endDate.isBefore(today)) {
            SubscriptionDueStatus.Expired(endDate)
        } else {
            SubscriptionDueStatus.EndingWithoutRenewal(endDate, getDaysDifference(today, endDate))
        }
    }

    fun sortSubscriptions(
        subscriptions: List<Subscription>,
        sortOption: SubscriptionSortOption,
        today: LocalDate
    ): List<Subscription> {
        return sortSubscriptionsBy(
            items = subscriptions,
            sortOption = sortOption,
            today = today,
            selector = { it }
        )
    }

    fun <T> sortSubscriptionsBy(
        items: List<T>,
        sortOption: SubscriptionSortOption,
        today: LocalDate,
        selector: (T) -> Subscription
    ): List<T> {
        return when (sortOption) {
            SubscriptionSortOption.NEAREST_PAYMENT -> {
                items
                    .map { item ->
                        val sub = selector(item)
                        Triple(item, sub, resolveDueStatus(sub, today))
                    }
                    .sortedWith(
                        compareBy<Triple<T, Subscription, SubscriptionDueStatus>>(
                            { (_, _, status) ->
                                when (status) {
                                    is SubscriptionDueStatus.DueToday,
                                    is SubscriptionDueStatus.DueTomorrow,
                                    is SubscriptionDueStatus.DueSoon,
                                    is SubscriptionDueStatus.Upcoming -> 0
                                    is SubscriptionDueStatus.EndingWithoutRenewal -> 1
                                    is SubscriptionDueStatus.Expired -> 2
                                }
                            },
                            { (_, _, status) ->
                                when (status) {
                                    is SubscriptionDueStatus.DueToday -> 0L
                                    is SubscriptionDueStatus.DueTomorrow -> 1L
                                    is SubscriptionDueStatus.DueSoon -> status.daysUntil.toLong()
                                    is SubscriptionDueStatus.Upcoming -> status.daysUntil.toLong()
                                    is SubscriptionDueStatus.EndingWithoutRenewal -> status.daysUntilEnd.toLong()
                                    is SubscriptionDueStatus.Expired -> -status.endDate.toEpochDay()
                                }
                            }
                        ).thenBy(String.CASE_INSENSITIVE_ORDER) { (_, sub, _) -> sub.name }
                    )
                    .map { (item, _, _) -> item }
            }

            SubscriptionSortOption.NAME_ASC -> {
                items.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { selector(it).name })
            }

            SubscriptionSortOption.PRICE_DESC -> {
                items.sortedWith(
                    compareByDescending<T> { selector(it).price }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { selector(it).name }
                )
            }

            SubscriptionSortOption.PRICE_ASC -> {
                items.sortedWith(
                    compareBy<T> { selector(it).price }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { selector(it).name }
                )
            }
        }
    }
}
