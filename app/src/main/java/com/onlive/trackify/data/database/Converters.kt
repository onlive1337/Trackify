package com.onlive.trackify.data.database

import androidx.room.TypeConverter
import com.onlive.trackify.data.model.BillingFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        if (value == null) return null
        return if (abs(value) > LEGACY_MILLIS_THRESHOLD) {
            Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.ofEpochDay(value)
        }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromBillingFrequency(frequency: BillingFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toBillingFrequency(value: String): BillingFrequency {
        return runCatching { BillingFrequency.valueOf(value) }.getOrDefault(BillingFrequency.MONTHLY)
    }

    private companion object {
        const val LEGACY_MILLIS_THRESHOLD = 100_000_000_000L
    }
}
