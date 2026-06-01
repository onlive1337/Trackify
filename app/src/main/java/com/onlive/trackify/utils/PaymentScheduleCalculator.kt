package com.onlive.trackify.utils

import com.onlive.trackify.data.model.BillingFrequency
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object PaymentScheduleCalculator {

    fun calculateNextPaymentDate(
        startDate: Date,
        billingFrequency: BillingFrequency,
        endDate: Date?,
        today: Date
    ): Date? {
        val todayStart = normalizeToStartOfDay(today)
        val start = normalizeToStartOfDay(startDate)

        if (start.after(todayStart)) {
            return if (endDate != null && start.after(normalizeToStartOfDay(endDate))) null else start
        }

        val calendar = Calendar.getInstance().apply { time = start }
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
        val targetMonth = calendar.get(Calendar.MONTH)

        when (billingFrequency) {
            BillingFrequency.MONTHLY -> {
                while (calendar.time.before(todayStart)) {
                    calendar.add(Calendar.MONTH, 1)
                    restoreDayOfMonth(calendar, targetDay)
                }
            }
            BillingFrequency.YEARLY -> {
                while (calendar.time.before(todayStart)) {
                    calendar.add(Calendar.YEAR, 1)
                    calendar.set(Calendar.MONTH, targetMonth)
                    restoreDayOfMonth(calendar, targetDay)
                }
            }
        }

        val nextPaymentDate = calendar.time
        if (endDate != null && nextPaymentDate.after(normalizeToStartOfDay(endDate))) {
            return null
        }
        return nextPaymentDate
    }

    private fun restoreDayOfMonth(calendar: Calendar, targetDay: Int) {
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDay))
    }

    fun normalizeToStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    fun getDaysDifference(from: Date, to: Date): Int {
        val fromNormalized = normalizeToStartOfDay(from)
        val toNormalized = normalizeToStartOfDay(to)
        val diffMillis = toNormalized.time - fromNormalized.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}
