package com.onlive.trackify.data.model

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

    fun format(amount: Double, maxDecimals: Int = 2): String {
        val formatPattern = if (maxDecimals > 0) "%.${maxDecimals}f" else "%.0f"
        val formattedAmount = String.format(formatPattern, amount)
        return when (format) {
            CurrencyFormat.SYMBOL_BEFORE -> "$symbol$formattedAmount"
            CurrencyFormat.SYMBOL_AFTER -> "$formattedAmount $symbol"
        }
    }
}

enum class CurrencyFormat {
    SYMBOL_BEFORE,
    SYMBOL_AFTER
}