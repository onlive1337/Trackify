package com.onlive.trackify.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    private const val MAX_SAFE_AMOUNT = 999_999_999.99
    private const val MIN_SAFE_AMOUNT = -999_999_999.99

    fun formatAmount(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        return try {
            val safeAmount = amount.coerceIn(MIN_SAFE_AMOUNT, MAX_SAFE_AMOUNT)

            if (!safeAmount.isFinite()) {
                return if (includeSymbol) "∞" else "999999999"
            }

            val preferenceManager = PreferenceManager(context)
            val currency = preferenceManager.getCurrentCurrency()

            return when {
                abs(safeAmount) >= 1_000_000 -> formatLargeAmount(safeAmount, currency.symbol, includeSymbol)
                else -> currency.format(safeAmount, includeSymbol, 0)
            }
        } catch (e: Exception) {
            if (includeSymbol) "0 ₽" else "0"
        }
    }

    fun formatTwoDecimalPlaces(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        return try {
            val safeAmount = amount.coerceIn(MIN_SAFE_AMOUNT, MAX_SAFE_AMOUNT)

            if (!safeAmount.isFinite()) {
                return if (includeSymbol) "∞" else "999999999.99"
            }

            val preferenceManager = PreferenceManager(context)
            val currency = preferenceManager.getCurrentCurrency()

            val locale = when (currency.code) {
                "RUB" -> Locale("ru", "RU")
                "USD" -> Locale.US
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                else -> Locale.getDefault()
            }

            val format = NumberFormat.getCurrencyInstance(locale)
            format.maximumFractionDigits = 2
            format.minimumFractionDigits = 2
            format.currency = java.util.Currency.getInstance(currency.code)

            val formatted = format.format(safeAmount)

            return if (includeSymbol) {
                formatted
            } else {
                formatted.replace(currency.symbol, "").trim()
            }
        } catch (e: Exception) {
            val fallbackFormat = DecimalFormat("#,##0.00")
            val formatted = fallbackFormat.format(amount.coerceIn(MIN_SAFE_AMOUNT, MAX_SAFE_AMOUNT))
            if (includeSymbol) "$formatted ₽" else formatted
        }
    }

    private fun formatLargeAmount(amount: Double, symbol: String, includeSymbol: Boolean): String {
        val absAmount = abs(amount)
        val sign = if (amount < 0) "-" else ""

        return when {
            absAmount >= 1_000_000_000 -> {
                val billions = amount / 1_000_000_000
                val formatted = DecimalFormat("#.#B").format(billions)
                if (includeSymbol) "$sign$formatted $symbol" else "$sign$formatted"
            }
            absAmount >= 1_000_000 -> {
                val millions = amount / 1_000_000
                val formatted = DecimalFormat("#.#M").format(millions)
                if (includeSymbol) "$sign$formatted $symbol" else "$sign$formatted"
            }
            else -> {
                val formatted = DecimalFormat("#,##0").format(amount)
                if (includeSymbol) "$formatted $symbol" else formatted
            }
        }
    }

    fun validateAmount(amount: String): ValidationResult {
        return try {
            if (amount.isBlank()) {
                return ValidationResult.Error("Введите сумму")
            }

            val cleanAmount = amount.replace(",", ".").replace(" ", "")

            if (!cleanAmount.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                return ValidationResult.Error("Недопустимые символы в сумме")
            }

            val doubleAmount = cleanAmount.toDoubleOrNull()
                ?: return ValidationResult.Error("Некорректный формат суммы")

            when {
                !doubleAmount.isFinite() -> ValidationResult.Error("Сумма должна быть числом")
                doubleAmount < MIN_SAFE_AMOUNT -> ValidationResult.Error("Сумма слишком мала")
                doubleAmount > MAX_SAFE_AMOUNT -> ValidationResult.Error("Сумма слишком велика")
                doubleAmount == 0.0 -> ValidationResult.Error("Сумма не может быть нулевой")
                else -> ValidationResult.Success(doubleAmount)
            }
        } catch (e: Exception) {
            ValidationResult.Error("Ошибка при обработке суммы")
        }
    }

    sealed class ValidationResult {
        data class Success(val amount: Double) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}