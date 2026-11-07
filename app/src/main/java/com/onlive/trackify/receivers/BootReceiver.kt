package com.onlive.trackify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    private val tag = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d(tag, "System event (${intent.action}) received, rescheduling notifications")

            val preferenceManager = PreferenceManager(context)
            if (preferenceManager.areNotificationsEnabled()) {
                val scheduler = NotificationScheduler(context)
                scheduler.scheduleNotifications()
                scheduler.triggerImmediateCheck()
                Log.d(tag, "Notifications rescheduled and checked")
            }
        }
    }
}
