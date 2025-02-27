package com.onlive.trackify.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log

object MemoryUtils {

    fun handleLowMemory(context: Context) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            if (memoryInfo.availMem < memoryInfo.totalMem * 0.2) {
                Log.d("MemoryUtils", "Low memory detected, cleaning up resources")

                System.gc()

                activityManager.getRunningAppProcesses()?.find {
                    it.pid == Process.myPid()
                }?.let {
                    activityManager.setProcessImportance(it.pid, ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
                }

                activityManager.trimMemory(ActivityManager.TRIM_MEMORY_RUNNING_MODERATE)
            }
        } catch (e: Exception) {
            Log.e("MemoryUtils", "Error handling memory management", e)
        }
    }
}