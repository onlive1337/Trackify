package com.onlive.trackify.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DATE_FORMAT = "dd.MM.yyyy"
    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2100

    fun formatDate(date: Date): String {
        return try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            dateFormat.format(date)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    fun getDateAfterDays(days: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, days.coerceIn(-36500, 36500))
            calendar.time
        } catch (e: Exception) {
            Date()
        }
    }

    fun getDateAfterMonths(months: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, months.coerceIn(-1200, 1200))
            calendar.time
        } catch (e: Exception) {
            Date()
        }
    }

    fun getDateAfterYears(years: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, years.coerceIn(-100, 100))
            calendar.time
        } catch (e: Exception) {
            Date()
        }
    }

    fun addMonthsSafely(date: Date, months: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date

            val originalDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.add(Calendar.MONTH, months)

            val maxDayInNewMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (originalDayOfMonth > maxDayInNewMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, maxDayInNewMonth)
            }

            calendar.time
        } catch (e: Exception) {
            date
        }
    }

    fun addYearsSafely(date: Date, years: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date

            val isLeapDay = calendar.get(Calendar.MONTH) == Calendar.FEBRUARY &&
                    calendar.get(Calendar.DAY_OF_MONTH) == 29

            calendar.add(Calendar.YEAR, years)

            if (isLeapDay && !isLeapYear(calendar.get(Calendar.YEAR))) {
                calendar.set(Calendar.DAY_OF_MONTH, 28)
            }

            calendar.time
        } catch (e: Exception) {
            date
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    fun validateDate(date: Date?): Date {
        if (date == null) return Date()

        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)

        return if (year in MIN_YEAR..MAX_YEAR) {
            date
        } else {
            Date()
        }
    }

    fun getDaysDifference(from: Date, to: Date): Int {
        return try {
            val diffMillis = to.time - from.time
            val days = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
            days.coerceIn(-36500, 36500) // ±100 лет
        } catch (e: Exception) {
            0
        }
    }

    fun getMonthName(month: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month.coerceIn(0, 11))
            val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            "Month ${month + 1}"
        }
    }

    fun getShortMonthName(month: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month.coerceIn(0, 11))
            val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            "M${month + 1}"
        }
    }
}