package com.onlive.trackify.utils

import com.onlive.trackify.data.model.BillingFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.util.Calendar
import java.util.Date
import org.junit.Test

class PaymentScheduleCalculatorTest {

    private fun date(year: Int, month1Based: Int, day: Int): Date {
        return Calendar.getInstance().apply {
            clear()
            set(year, month1Based - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun nextPayment(
        start: Date,
        today: Date,
        frequency: BillingFrequency = BillingFrequency.MONTHLY,
        endDate: Date? = null
    ): Date? = PaymentScheduleCalculator.calculateNextPaymentDate(start, frequency, endDate, today)

    // --- Start in the past ---

    @Test
    fun monthly_startInPast_returnsThisMonthOccurrence() {
        val start = date(2026, 1, 10)
        val today = date(2026, 6, 5)
        assertEquals(date(2026, 6, 10), nextPayment(start, today))
    }

    @Test
    fun monthly_startInPast_paymentToday_returnsToday() {
        val start = date(2026, 1, 15)
        val today = date(2026, 6, 15)
        assertEquals(date(2026, 6, 15), nextPayment(start, today))
    }

    @Test
    fun yearly_startInPast_returnsThisYearOccurrence() {
        val start = date(2020, 3, 20)
        val today = date(2026, 1, 1)
        assertEquals(date(2026, 3, 20), nextPayment(start, today, BillingFrequency.YEARLY))
    }

    @Test
    fun yearly_startInPast_afterAnniversary_returnsNextYear() {
        val start = date(2020, 3, 20)
        val today = date(2026, 4, 1)
        assertEquals(date(2027, 3, 20), nextPayment(start, today, BillingFrequency.YEARLY))
    }

    // --- Start in the future ---

    @Test
    fun monthly_startInFuture_returnsStartDate() {
        val start = date(2026, 8, 1)
        val today = date(2026, 6, 5)
        assertEquals(date(2026, 8, 1), nextPayment(start, today))
    }

    @Test
    fun yearly_startInFuture_returnsStartDate() {
        val start = date(2027, 2, 14)
        val today = date(2026, 6, 5)
        assertEquals(date(2027, 2, 14), nextPayment(start, today, BillingFrequency.YEARLY))
    }

    // --- endDate handling ---

    @Test
    fun monthly_endDateBeforeNextPayment_returnsNull() {
        val start = date(2026, 1, 10)
        val today = date(2026, 6, 5)
        // ends before the upcoming June 10 payment
        assertNull(nextPayment(start, today, endDate = date(2026, 6, 1)))
    }

    @Test
    fun monthly_endDateOnNextPayment_returnsThatPayment() {
        val start = date(2026, 1, 10)
        val today = date(2026, 6, 5)
        assertEquals(date(2026, 6, 10), nextPayment(start, today, endDate = date(2026, 6, 10)))
    }

    @Test
    fun futureStart_afterEndDate_returnsNull() {
        val start = date(2026, 8, 1)
        val today = date(2026, 6, 5)
        assertNull(nextPayment(start, today, endDate = date(2026, 7, 1)))
    }

    // --- Day-drift edge cases (the bug this calculator fixes) ---

    @Test
    fun monthly_31st_clampsInShortMonthButRecoversLater() {
        val start = date(2026, 1, 31)

        // February only has 28 days in 2026 -> clamps to Feb 28
        assertEquals(date(2026, 2, 28), nextPayment(start, date(2026, 2, 15)))

        // March has 31 days -> must recover to the 31st, NOT stay on the 28th
        assertEquals(date(2026, 3, 31), nextPayment(start, date(2026, 3, 15)))

        // April has 30 days -> clamps to the 30th
        assertEquals(date(2026, 4, 30), nextPayment(start, date(2026, 4, 15)))

        // May recovers to the 31st again
        assertEquals(date(2026, 5, 31), nextPayment(start, date(2026, 5, 15)))
    }

    @Test
    fun monthly_30th_recoversAfterFebruary() {
        val start = date(2026, 1, 30)
        assertEquals(date(2026, 2, 28), nextPayment(start, date(2026, 2, 10)))
        assertEquals(date(2026, 3, 30), nextPayment(start, date(2026, 3, 10)))
    }

    @Test
    fun monthly_endOfFebruary_leapYear() {
        val start = date(2023, 1, 31)
        // 2024 is a leap year: February has 29 days
        assertEquals(date(2024, 2, 29), nextPayment(start, date(2024, 2, 10)))
    }

    @Test
    fun yearly_feb29_clampsInNonLeapAndRecoversOnNextLeapYear() {
        val start = date(2024, 2, 29)

        // 2025 is not a leap year -> Feb 28
        assertEquals(date(2025, 2, 28), nextPayment(start, date(2025, 1, 1), BillingFrequency.YEARLY))

        // 2026 still not leap -> Feb 28 (must not drift to Feb 27 etc.)
        assertEquals(date(2026, 2, 28), nextPayment(start, date(2026, 1, 1), BillingFrequency.YEARLY))

        // 2028 is a leap year -> recovers to Feb 29
        assertEquals(date(2028, 2, 29), nextPayment(start, date(2028, 1, 1), BillingFrequency.YEARLY))
    }
}
