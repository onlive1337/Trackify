package com.onlive.trackify.utils

import android.content.Context

object CurrencyFormatter {
    fun formatAmount(context: Context, amount: Double): String {
        val preferenceManager = PreferenceManager(context)
        val currency = preferenceManager.getCurrentCurrency()
        return currency.format(amount)
    }

    fun formatAmountWithoutSymbol(amount: Double): String {
        return String.format("%.2f", amount)
    }
}