package com.example.trackify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Subscription::class,
            parentColumns = ["subscriptionId"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subscriptionId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0,
    val subscriptionId: Long,
    val amount: Double,
    val date: Date,
    val notes: String? = null
)