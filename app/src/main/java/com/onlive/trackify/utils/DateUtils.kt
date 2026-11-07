package com.onlive.trackify.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val FULL_DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT)

    fun formatDate(date: LocalDate): String {
        return date.format(FULL_DATE_FORMATTER)
    }

    fun formatShortDate(
        date: LocalDate,
        referenceDate: LocalDate = LocalDate.now(),
        locale: Locale = Locale.getDefault()
    ): String {
        val sameYear = date.year == referenceDate.year
        val pattern = when {
            !sameYear -> "dd.MM.yyyy"
            locale.language == "ru" -> "d MMM"
            else -> "MMM d"
        }
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun toUtcEpochMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    fun fromUtcEpochMillis(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }
}
