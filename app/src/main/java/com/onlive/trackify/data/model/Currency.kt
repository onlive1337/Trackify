package com.onlive.trackify.data.model

import androidx.annotation.StringRes
import com.onlive.trackify.R
import java.text.NumberFormat
import java.util.Currency as JavaCurrency
import java.util.Locale

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    @StringRes val nameRes: Int,
    val format: CurrencyFormat = CurrencyFormat.SYMBOL_BEFORE
) {
    companion object {
        val POPULAR_CURRENCIES = listOf(
            Currency("USD", "$", "US Dollar", R.string.currency_usd),
            Currency("EUR", "€", "Euro", R.string.currency_eur),
            Currency("GBP", "£", "British Pound", R.string.currency_gbp),
            Currency("RUB", "₽", "Russian Ruble", R.string.currency_rub, CurrencyFormat.SYMBOL_AFTER),
            Currency("JPY", "¥", "Japanese Yen", R.string.currency_jpy),
            Currency("CNY", "¥", "Chinese Yuan", R.string.currency_cny),
            Currency("INR", "₹", "Indian Rupee", R.string.currency_inr),
            Currency("CAD", "C$", "Canadian Dollar", R.string.currency_cad),
            Currency("AUD", "A$", "Australian Dollar", R.string.currency_aud),
            Currency("CHF", "Fr", "Swiss Franc", R.string.currency_chf, CurrencyFormat.SYMBOL_AFTER),
            Currency("UZS", "UZS", "Uzbekistani Som", R.string.currency_uzs, CurrencyFormat.SYMBOL_AFTER)
        )

        fun getCurrencyByCode(code: String): Currency {
            return POPULAR_CURRENCIES.find { it.code == code } ?: POPULAR_CURRENCIES[0]
        }

        fun isSupportedCurrencyCode(code: String): Boolean {
            return POPULAR_CURRENCIES.any { it.code == code }
        }
    }

    fun format(amount: Double, includeSymbol: Boolean = true, maxDecimals: Int = 2): String {
        try {
            val targetLocale = getLocaleForCurrency()
            val format = NumberFormat.getCurrencyInstance(targetLocale)
            if (format is java.text.DecimalFormat) {
                val symbols = format.decimalFormatSymbols
                symbols.currencySymbol = symbol
                format.decimalFormatSymbols = symbols
            } else {
                val javaCurrency = JavaCurrency.getInstance(code)
                format.currency = javaCurrency
            }
            format.maximumFractionDigits = maxDecimals

            if (includeSymbol) {
                return format.format(amount)
            } else {
                val formatted = format.format(amount)
                return formatted.replace(symbol, "").trim()
            }
        } catch (_: Exception) {
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
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "RUB" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
            "JPY" -> Locale.JAPAN
            "CNY" -> Locale.CHINA
            "INR" -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
            "CAD" -> Locale.CANADA
            "AUD" -> Locale.Builder().setLanguage("en").setRegion("AU").build()
            "CHF" -> Locale.Builder().setLanguage("de").setRegion("CH").build()
            "UZS" -> Locale.Builder().setLanguage("uz").setRegion("UZ").build()
            else -> Locale.getDefault()
        }
    }
}

enum class CurrencyFormat {
    SYMBOL_BEFORE,
    SYMBOL_AFTER
}