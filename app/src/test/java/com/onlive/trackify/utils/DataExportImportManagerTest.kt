package com.onlive.trackify.utils

import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataExportImportManagerTest {

    @Test
    fun `encodeBackup and decodeAndValidateBackup round-trip full payload`() {
        val categories = listOf(
            Category(categoryId = 1L, name = "Entertainment", description = "Streaming", colorCode = "#FF5252")
        )
        val subscriptions = listOf(
            Subscription(
                subscriptionId = 10L,
                name = "Netflix",
                description = "Family plan",
                price = 15.99,
                billingFrequency = BillingFrequency.MONTHLY,
                startDate = LocalDate.of(2026, 1, 15),
                endDate = null,
                categoryId = 1L
            )
        )
        val payments = listOf(
            Payment(
                paymentId = 100L,
                subscriptionId = 10L,
                amount = 15.99,
                date = LocalDate.of(2026, 10, 15),
                notes = "Auto-debit"
            )
        )
        val prefs = DataExportImportManager.UserPreferencesBackupDto(
            notificationsEnabled = true,
            reminderDays = setOf(0, 1, 3),
            notificationHour = 10,
            notificationMinute = 30,
            currencyCode = "UZS",
            languageCode = "ru",
            themeMode = ThemeManager.MODE_DARK,
            dynamicColorEnabled = false,
            subscriptionSort = "NEAREST_PAYMENT"
        )

        val encoded = DataExportImportManager.encodeBackup(
            categories = categories,
            subscriptions = subscriptions,
            payments = payments,
            userPreferences = prefs,
            exportedAt = Instant.parse("2026-11-02T01:45:00Z")
        )

        assertTrue(encoded.contains("\"backupVersion\": 1"))
        assertTrue(encoded.contains("\"2026-01-15\""))

        val decoded = DataExportImportManager.decodeAndValidateBackup(encoded)
        assertNotNull(decoded)
        assertEquals(categories, decoded!!.categories)
        assertEquals(subscriptions, decoded.subscriptions)
        assertEquals(payments, decoded.payments)
        assertEquals(prefs, decoded.userPreferences)
    }

    @Test
    fun `decodeAndValidateBackup supports legacy ISO timestamp dates and rejects orphan foreign keys`() {
        val legacyJson = """
            {
              "categories": [{"categoryId": 1, "name": "Work", "colorCode": "#448AFF"}],
              "subscriptions": [{
                "subscriptionId": 2,
                "name": "GitHub",
                "price": 4.0,
                "billingFrequency": "MONTHLY",
                "startDate": "2026-03-20T00:00:00.000Z",
                "categoryId": 1
              }],
              "payments": [{
                "paymentId": 5,
                "subscriptionId": 2,
                "amount": 4.0,
                "date": "2026-10-20T12:30:00.000Z"
              }]
            }
        """.trimIndent()

        val legacyDecoded = DataExportImportManager.decodeAndValidateBackup(legacyJson)
        assertNotNull(legacyDecoded)
        assertEquals(LocalDate.of(2026, 3, 20), legacyDecoded!!.subscriptions.single().startDate)
        assertEquals(LocalDate.of(2026, 10, 20), legacyDecoded.payments.single().date)

        val orphanPaymentJson = """
            {
              "categories": [{"categoryId": 1, "name": "Work", "colorCode": "#448AFF"}],
              "subscriptions": [{
                "subscriptionId": 2,
                "name": "GitHub",
                "price": 4.0,
                "billingFrequency": "MONTHLY",
                "startDate": "2026-03-20",
                "categoryId": 1
              }],
              "payments": [{
                "paymentId": 5,
                "subscriptionId": 999,
                "amount": 4.0,
                "date": "2026-10-20"
              }]
            }
        """.trimIndent()
        assertNull(DataExportImportManager.decodeAndValidateBackup(orphanPaymentJson))

        val unsupportedVersionJson = """
            {
              "backupVersion": 99,
              "categories": [],
              "subscriptions": [],
              "payments": []
            }
        """.trimIndent()
        assertNull(DataExportImportManager.decodeAndValidateBackup(unsupportedVersionJson))
    }
}
