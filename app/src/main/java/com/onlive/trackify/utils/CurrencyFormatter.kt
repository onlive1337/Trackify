package com.onlive.trackify.utils

import android.content.Context

object CurrencyFormatter {
    fun formatAmount(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        val preferenceManager = PreferenceManager(context)
        val currency = preferenceManager.getCurrentCurrency()
        return currency.format(amount, includeSymbol)
    }
}