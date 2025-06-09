package com.onlive.trackify.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getDateAfterDays(days: Int): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    fun getDateAfterMonths(months: Int): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, months)
        return calendar.time
    }

    fun getDateAfterYears(years: Int): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.YEAR, years)
        return calendar.time
    }

    fun getMonthName(month: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.MONTH, month)
        val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getShortMonthName(month: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.MONTH, month)
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}