package com.onlive.trackify.utils

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.annotation.StringRes
import com.onlive.trackify.R
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface UiErrorMessage {
    data class Resource(@StringRes val resId: Int) : UiErrorMessage
    data class Dynamic(val text: String) : UiErrorMessage
}

interface ErrorReporter {
    fun reportError(error: Result.Error, showMessage: Boolean = true)
    fun reportException(throwable: Throwable, showMessage: Boolean = true)
}

class ErrorHandler private constructor(context: Context) : ErrorReporter {

    companion object {
        @Volatile
        private var INSTANCE: ErrorHandler? = null
        private const val TAG = "ErrorHandler"

        fun getInstance(context: Context): ErrorHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErrorHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val contextRef = WeakReference(context.applicationContext)

    private val _errors = MutableSharedFlow<UiErrorMessage>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errors: SharedFlow<UiErrorMessage> = _errors.asSharedFlow()

    override fun reportError(error: Result.Error, showMessage: Boolean) {
        if (showMessage) {
            _errors.tryEmit(UiErrorMessage.Resource(error.messageRes))
        }
        val appContext = contextRef.get() ?: return
        val logText = appContext.getString(error.messageRes)
        Log.e(TAG, "Error handled: $logText", error.exception)
        if (error.exception != null) {
            CrashLogger.writeLogAsync(appContext, error.exception, logText)
        } else {
            CrashLogger.writeLogAsync(appContext, logText, null)
        }
    }

    override fun reportException(throwable: Throwable, showMessage: Boolean) {
        val messageRes = getReadableErrorMessageRes(throwable)
        if (showMessage) {
            _errors.tryEmit(UiErrorMessage.Resource(messageRes))
        }
        val appContext = contextRef.get() ?: return
        val logText = throwable.message ?: appContext.getString(messageRes)
        Log.e(TAG, "Exception handled: $logText", throwable)
        CrashLogger.writeLogAsync(appContext, throwable, logText)
    }

    fun handleError(error: Any?, showToast: Boolean = true) {
        when (error) {
            is Result.Error -> reportError(error, showMessage = showToast)
            is Throwable -> reportException(error, showMessage = showToast)
            is String -> {
                if (showToast) {
                    _errors.tryEmit(UiErrorMessage.Dynamic(error))
                }
                Log.e(TAG, "Error handled: $error")
                logToFile(error)
            }
            else -> {
                if (showToast) {
                    _errors.tryEmit(UiErrorMessage.Resource(R.string.unknown_error))
                }
                val appContext = contextRef.get() ?: return
                val fallback = appContext.getString(R.string.unknown_error)
                Log.e(TAG, "Error handled: $fallback")
                logToFile(fallback)
            }
        }
    }

    fun logToFile(message: String, details: String? = null) {
        val appContext = contextRef.get() ?: return
        CrashLogger.writeLogAsync(appContext, message, details)
    }

    @StringRes
    private fun getReadableErrorMessageRes(throwable: Throwable): Int {
        return when (throwable) {
            is UnknownHostException -> R.string.no_internet_connection
            is SocketTimeoutException -> R.string.connection_timeout
            is SQLiteConstraintException -> R.string.database_constraint_error
            is FileNotFoundException -> R.string.file_not_found_error
            else -> R.string.unknown_error
        }
    }
}
