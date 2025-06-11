package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onlive.trackify.R
import java.lang.ref.WeakReference

class ErrorHandler private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: ErrorHandler? = null
        private const val TAG = "ErrorHandler"

        fun getInstance(context: Context): ErrorHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = ErrorHandler(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val contextRef = WeakReference(context.applicationContext)

    fun handleError(error: Any?, showToast: Boolean = true, context: String? = null) {
        val appContext = contextRef.get() ?: return

        val errorMessage = when (error) {
            is String -> error
            is Exception -> getReadableErrorMessage(error, appContext)
            is Result.Error -> error.message
            else -> appContext.getString(R.string.unknown_error)
        }

        try {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("error_context", context ?: "unknown")
                setCustomKey("show_toast", showToast)
                setCustomKey("error_message", errorMessage)

                when (error) {
                    is Exception -> {
                        recordException(error)
                    }
                    is Result.Error -> {
                        error.exception?.let { recordException(it) }
                            ?: log("Error without exception: $errorMessage")
                    }
                    else -> {
                        log("String error: $errorMessage")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error to Crashlytics", e)
        }

        Log.e(TAG, "Error handled: $errorMessage", error as? Throwable)
    }

    private fun getReadableErrorMessage(e: Exception, context: Context): String {
        return when (e) {
            is java.net.UnknownHostException -> context.getString(R.string.no_internet_connection)
            is java.net.SocketTimeoutException -> context.getString(R.string.connection_timeout)
            is android.database.sqlite.SQLiteConstraintException -> context.getString(R.string.database_constraint_error)
            is java.io.FileNotFoundException -> context.getString(R.string.file_not_found_error)
            else -> e.message ?: context.getString(R.string.unknown_error)
        }
    }
}