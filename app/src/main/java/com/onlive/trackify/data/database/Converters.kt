package com.onlive.trackify.data.database

import androidx.room.TypeConverter
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.PaymentStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBillingFrequency(frequency: BillingFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toBillingFrequency(value: String): BillingFrequency {
        return BillingFrequency.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String {
        return status.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return try {
            PaymentStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PaymentStatus.MANUAL
        }
    }
}