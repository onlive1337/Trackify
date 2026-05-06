package com.onlive.trackify.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "DatabaseCleanupWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            cleanupOldNotificationRecords()
            Log.d(tag, "Cleanup completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Cleanup failed", e)
            Result.failure()
        }
    }

    private fun cleanupOldNotificationRecords() {
        val preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.cleanupOldNotificationRecords()
        Log.d(tag, "Old notification records cleaned up")
    }
}