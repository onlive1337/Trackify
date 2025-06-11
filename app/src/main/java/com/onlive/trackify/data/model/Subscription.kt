package com.onlive.trackify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

enum class BillingFrequency {
    MONTHLY,
    YEARLY
}

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("endDate"),
        Index("name")
    ]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val subscriptionId: Long = 0,
    val name: String,
    val description: String?,
    val price: Double,
    val billingFrequency: BillingFrequency,
    val startDate: Date,
    val endDate: Date?,
    val categoryId: Long?,
    val image: String? = null
) {
    @Ignore
    var categoryName: String? = null

    @Ignore
    var categoryColor: String? = null
}