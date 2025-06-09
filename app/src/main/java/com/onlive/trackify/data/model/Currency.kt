package com.onlive.trackify.data.model

import java.text.NumberFormat
import java.util.Currency as JavaCurrency
import java.util.Locale

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val format: CurrencyFormat = CurrencyFormat.SYMBOL_BEFORE
) {
    companion object {
        val POPULAR_CURRENCIES = listOf(
            Currency("RUB", "₽", "Russian Ruble", CurrencyFormat.SYMBOL_AFTER),
            Currency("USD", "$", "US Dollar"),
            Currency("EUR", "€", "Euro"),
            Currency("GBP", "£", "British Pound"),
            Currency("JPY", "¥", "Japanese Yen"),
            Currency("CNY", "¥", "Chinese Yuan"),
            Currency("INR", "₹", "Indian Rupee"),
            Currency("CAD", "C$", "Canadian Dollar"),
            Currency("AUD", "A$", "Australian Dollar"),
            Currency("CHF", "Fr", "Swiss Franc", CurrencyFormat.SYMBOL_AFTER)
        )

        fun getCurrencyByCode(code: String): Currency {
            return POPULAR_CURRENCIES.find { it.code == code } ?: POPULAR_CURRENCIES[0]
        }
    }

    fun format(amount: Double, includeSymbol: Boolean = true, maxDecimals: Int = 0): String {
        try {
            val targetLocale = getLocaleForCurrency()
            val format = NumberFormat.getCurrencyInstance(targetLocale)
            val javaCurrency = JavaCurrency.getInstance(code)
            format.currency = javaCurrency
            format.maximumFractionDigits = maxDecimals

            if (includeSymbol) {
                return format.format(amount)
            } else {
                val formatted = format.format(amount)
                return formatted.replace(symbol, "").trim()
            }
        } catch (e: Exception) {
            val targetLocale = getLocaleForCurrency()
            val formatPattern = if (maxDecimals > 0) "%.${maxDecimals}f" else "%.0f"
            val formattedAmount = String.format(targetLocale, formatPattern, amount)

            return when {
                !includeSymbol -> formattedAmount
                format == CurrencyFormat.SYMBOL_BEFORE -> "$symbol$formattedAmount"
                else -> "$formattedAmount $symbol"
            }
        }
    }

    private fun getLocaleForCurrency(): Locale {
        return when (code) {
            "RUB" -> Locale("ru", "RU")
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            "CNY" -> Locale.CHINA
            "INR" -> Locale("hi", "IN")
            "CAD" -> Locale.CANADA
            "AUD" -> Locale("en", "AU")
            "CHF" -> Locale("de", "CH")
            else -> Locale.getDefault()
        }
    }
}

enum class CurrencyFormat {
    SYMBOL_BEFORE,
    SYMBOL_AFTER
}