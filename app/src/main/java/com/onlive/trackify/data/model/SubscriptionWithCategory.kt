package com.onlive.trackify.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SubscriptionWithCategory(
    @Embedded
    val subscription: Subscription,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val category: Category? = null
) {
    val categoryName: String? get() = category?.name
    val categoryColor: String? get() = category?.colorCode
}
