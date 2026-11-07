package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.onlive.trackify.data.model.Currency
import java.math.BigDecimal

val LocalCurrency = compositionLocalOf { Currency.POPULAR_CURRENCIES.first() }

@Composable
fun ProvideCurrency(
    context: Context,
    content: @Composable () -> Unit
) {
    val appContext = context.applicationContext
    val preferenceManager = remember(appContext) { PreferenceManager(appContext) }
    var currentCurrency by remember(preferenceManager) {
        mutableStateOf(preferenceManager.getCurrentCurrency())
    }

    DisposableEffect(preferenceManager) {
        currentCurrency = preferenceManager.getCurrentCurrency()
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PreferenceManager.KEY_CURRENCY_CODE || key == null) {
                currentCurrency = preferenceManager.getCurrentCurrency()
            }
        }
        preferenceManager.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferenceManager.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    CompositionLocalProvider(
        LocalCurrency provides currentCurrency,
        content = content
    )
}

object CurrencyFormatter {
    const val MAX_AMOUNT: Double = 1_000_000_000_000.0
    const val MAX_AMOUNT_INPUT_LENGTH: Int = 16
    private val AMOUNT_INPUT_REGEX = Regex("""^\d*\.?\d*$""")

    fun isValidPositiveAmount(value: Double): Boolean {
        return value.isFinite() && value > 0.0 && value <= MAX_AMOUNT
    }

    fun parseValidPositiveAmount(input: String): Double? {
        val parsed = input.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return parsed.takeIf { isValidPositiveAmount(it) }
    }

    fun sanitizeAmountInput(input: String): String? {
        val sanitized = input.replace(',', '.')
        if (sanitized.length > MAX_AMOUNT_INPUT_LENGTH) return null
        if (sanitized.isEmpty() || sanitized == ".") return sanitized
        return sanitized.takeIf { AMOUNT_INPUT_REGEX.matches(it) }
    }

    fun formatPlainAmount(amount: Double): String {
        if (!amount.isFinite() || amount <= 0.0) return ""
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
    }

    fun formatAmount(context: Context, amount: Double, includeSymbol: Boolean = true): String {
        val preferenceManager = PreferenceManager(context)
        val currency = preferenceManager.getCurrentCurrency()
        return currency.format(amount, includeSymbol)
    }

    @Composable
    fun formatAmount(amount: Double, includeSymbol: Boolean = true): String {
        return LocalCurrency.current.format(amount, includeSymbol)
    }
}