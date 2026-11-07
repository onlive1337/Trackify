package com.onlive.trackify.utils

import com.onlive.trackify.data.database.Converters
import com.onlive.trackify.data.model.BillingFrequency
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyAndLocaleUtilsTest {

    @Test
    fun `formatPlainAmount avoids scientific notation for large amounts and strips trailing zeros`() {
        assertEquals("15", CurrencyFormatter.formatPlainAmount(15.0))
        assertEquals("15.99", CurrencyFormatter.formatPlainAmount(15.99))
        assertEquals("15000000", CurrencyFormatter.formatPlainAmount(15_000_000.0))
        assertEquals("125000000.5", CurrencyFormatter.formatPlainAmount(125_000_000.50))
        assertEquals("", CurrencyFormatter.formatPlainAmount(0.0))
        assertEquals("", CurrencyFormatter.formatPlainAmount(-5.0))
        assertEquals("", CurrencyFormatter.formatPlainAmount(Double.POSITIVE_INFINITY))
        assertEquals("", CurrencyFormatter.formatPlainAmount(Double.NaN))
    }

    @Test
    fun `sanitizeAmountInput and parseValidPositiveAmount enforce length and finite bounds`() {
        assertEquals("12.50", CurrencyFormatter.sanitizeAmountInput("12,50"))
        assertEquals("", CurrencyFormatter.sanitizeAmountInput(""))
        assertEquals(".", CurrencyFormatter.sanitizeAmountInput(","))
        assertNull(CurrencyFormatter.sanitizeAmountInput("12.3.4"))
        assertNull(CurrencyFormatter.sanitizeAmountInput("12345678901234567"))

        assertEquals(9.99, CurrencyFormatter.parseValidPositiveAmount("9,99")!!, 0.0001)
        assertEquals(15_000_000.0, CurrencyFormatter.parseValidPositiveAmount("15000000")!!, 0.0001)
        assertNull(CurrencyFormatter.parseValidPositiveAmount("0"))
        assertNull(CurrencyFormatter.parseValidPositiveAmount("-10"))
        assertNull(CurrencyFormatter.parseValidPositiveAmount("10000000000000"))
        assertFalse(CurrencyFormatter.isValidPositiveAmount(Double.POSITIVE_INFINITY))
        assertTrue(CurrencyFormatter.isValidPositiveAmount(CurrencyFormatter.MAX_AMOUNT))
    }

    @Test
    fun `LocaleHelper normalizes valid tags and never crashes on ill-formed tags`() {
        assertEquals("en", LocaleHelper.normalizeLanguageCode("en"))
        assertEquals("en", LocaleHelper.normalizeLanguageCode("en-US"))
        assertEquals("ru", LocaleHelper.normalizeLanguageCode("RU_RU"))
        assertNull(LocaleHelper.normalizeLanguageCode("de"))
        assertNull(LocaleHelper.normalizeLanguageCode("???_invalid_locale!!!"))
        assertNull(LocaleHelper.normalizeLanguageCode(""))
        assertNull(LocaleHelper.normalizeLanguageCode(null))

        val fallbackLocale = LocaleHelper.toSafeLocale("???_invalid_locale!!!")
        assertNotNull(fallbackLocale)
        assertTrue(fallbackLocale.language in LocaleHelper.SUPPORTED_LANGUAGE_CODES)
    }

    @Test
    fun `Room Converters safely handle unknown billing frequency and epoch-day roundtrips`() {
        val converters = Converters()
        assertEquals(BillingFrequency.MONTHLY, converters.toBillingFrequency("MONTHLY"))
        assertEquals(BillingFrequency.YEARLY, converters.toBillingFrequency("YEARLY"))
        assertEquals(BillingFrequency.MONTHLY, converters.toBillingFrequency("WEEKLY_FUTURE"))
        assertEquals(BillingFrequency.MONTHLY, converters.toBillingFrequency(""))

        val sampleDate = LocalDate.of(2026, 11, 7)
        val epochDay = converters.localDateToEpochDay(sampleDate)
        assertEquals(sampleDate, converters.fromEpochDay(epochDay))
        assertNull(converters.fromEpochDay(null))
    }
}
