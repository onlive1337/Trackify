package com.onlive.trackify.data.model

import androidx.annotation.StringRes
import com.onlive.trackify.R

enum class SubscriptionSortOption(@StringRes val labelRes: Int) {
    NEAREST_PAYMENT(R.string.sort_nearest_payment),
    NAME_ASC(R.string.sort_name),
    PRICE_DESC(R.string.sort_price_high_to_low),
    PRICE_ASC(R.string.sort_price_low_to_high);

    companion object {
        fun fromName(value: String?): SubscriptionSortOption {
            return entries.find { it.name == value } ?: NEAREST_PAYMENT
        }
    }
}
