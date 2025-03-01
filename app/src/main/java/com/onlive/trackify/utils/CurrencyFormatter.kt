package com.onlive.trackify.utils

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatAmount(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        val preferenceManager = PreferenceManager(context)
        val currency = preferenceManager.getCurrentCurrency()
        return currency.format(amount, includeSymbol)
    }

    fun formatTwoDecimalPlaces(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        val preferenceManager = PreferenceManager(context)
        val currency = preferenceManager.getCurrentCurrency()

        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        format.currency = java.util.Currency.getInstance(currency.code)

        val formatted = format.format(amount)

        return if (includeSymbol) {
            formatted
        } else {
            formatted.replace(currency.symbol, "").trim()
        }
    }
}