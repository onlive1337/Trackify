package com.onlive.trackify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlive.trackify.utils.AlarmScheduler
import com.onlive.trackify.utils.NotificationChecker
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    private val tag = "NotificationReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Alarm received")

        val preferenceManager = PreferenceManager(context)
        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, skipping check")
            return
        }

        AlarmScheduler(context).scheduleNextAlarm()

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        scope.launch {
            try {
                NotificationChecker(appContext).checkNotifications()
            } catch (e: Exception) {
                Log.e(tag, "Error checking notifications", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
