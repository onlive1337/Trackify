package com.onlive.trackify.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.utils.AlarmScheduler
import com.onlive.trackify.utils.NotificationChecker
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "NotificationRefreshWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val preferenceManager = PreferenceManager(applicationContext)
            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(tag, "Notifications disabled, nothing to refresh")
                return@withContext Result.success()
            }

            AlarmScheduler(applicationContext).scheduleNextAlarm()

            NotificationChecker(applicationContext).checkNotifications()

            Log.d(tag, "Notification refresh completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Notification refresh failed", e)
            Result.retry()
        }
    }
}
