package com.onlive.trackify.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.onlive.trackify.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors

object CrashLogger {

    private const val TAG = "CrashLogger"
    private const val DIR_NAME = "crash_logs"
    private const val MAX_LOG_FILES = 20
    private val FILE_TIMESTAMP_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS", Locale.US)
    private val ENTRY_TIMESTAMP_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private val ioExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "CrashLogger").apply { isDaemon = true }
    }

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeLog(appContext, throwable, "Uncaught exception on thread '${thread.name}'")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to write crash log", e)
            } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun writeLogAsync(context: Context, throwable: Throwable, message: String? = null) {
        val appContext = context.applicationContext
        ioExecutor.execute { writeLog(appContext, throwable, message) }
    }

    fun writeLogAsync(context: Context, message: String, details: String? = null) {
        val appContext = context.applicationContext
        ioExecutor.execute { writeLog(appContext, message, details) }
    }

    fun writeLog(context: Context, throwable: Throwable, message: String? = null): File? {
        val stackTrace = StringWriter().also { sw ->
            PrintWriter(sw).use { throwable.printStackTrace(it) }
        }.toString()
        return writeLog(context, message ?: throwable.message ?: "Error", stackTrace)
    }

    fun writeLog(context: Context, message: String, details: String? = null): File? {
        return try {
            val dir = logDir(context)
            val now = LocalDateTime.now()
            val file = File(dir, "crash_${now.format(FILE_TIMESTAMP_FORMAT)}.txt")
            file.writeText(buildString {
                appendLine("Time: ${now.format(ENTRY_TIMESTAMP_FORMAT)}")
                appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Message: $message")
                if (!details.isNullOrBlank()) {
                    appendLine("---")
                    appendLine(details)
                }
            })
            trimOldLogs(dir)
            file
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to write log", e)
            null
        }
    }

    fun getLatestLog(context: Context): File? {
        return try {
            logDir(context).listFiles()
                ?.filter { it.isFile }
                ?.maxByOrNull { it.lastModified() }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to read logs", e)
            null
        }
    }

    private fun logDir(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun trimOldLogs(dir: File) {
        val files = dir.listFiles()?.filter { it.isFile } ?: return
        if (files.size <= MAX_LOG_FILES) return
        files.sortedByDescending { it.lastModified() }
            .drop(MAX_LOG_FILES)
            .forEach { runCatching { it.delete() } }
    }
}
