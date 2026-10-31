package com.onlive.trackify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationCheckerTest {

    @Test
    fun `exact reminder days resolve to matching threshold`() {
        val configured = setOf(0, 1, 3, 7)

        assertEquals(7, NotificationChecker.resolveReminderThreshold(7, configured))
        assertEquals(3, NotificationChecker.resolveReminderThreshold(3, configured))
        assertEquals(1, NotificationChecker.resolveReminderThreshold(1, configured))
        assertEquals(0, NotificationChecker.resolveReminderThreshold(0, configured))
    }

    @Test
    fun `one-day catch-up window resolves to missed threshold`() {
        val configured = setOf(0, 1, 3, 7)

        assertEquals(7, NotificationChecker.resolveReminderThreshold(6, configured))
        assertEquals(3, NotificationChecker.resolveReminderThreshold(2, configured))
    }

    @Test
    fun `unconfigured gap days return null instead of firing early or repeatedly`() {
        val defaultConfigured = setOf(0, 1, 3)
        assertNull(NotificationChecker.resolveReminderThreshold(5, defaultConfigured))
        assertNull(NotificationChecker.resolveReminderThreshold(4, defaultConfigured))

        val sevenDaysOnly = setOf(7)
        assertNull(NotificationChecker.resolveReminderThreshold(5, sevenDaysOnly))
        assertNull(NotificationChecker.resolveReminderThreshold(2, sevenDaysOnly))
        assertNull(NotificationChecker.resolveReminderThreshold(0, sevenDaysOnly))
    }

    @Test
    fun `negative days or empty reminder configuration return null`() {
        assertNull(NotificationChecker.resolveReminderThreshold(-1, setOf(0, 1, 3)))
        assertNull(NotificationChecker.resolveReminderThreshold(0, emptySet()))
    }
}
