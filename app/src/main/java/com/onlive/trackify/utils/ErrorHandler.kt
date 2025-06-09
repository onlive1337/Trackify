package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent = _errorEvent

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
        _errorEvent.postValue(Event(errorMessage))
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

    fun observeErrors(owner: LifecycleOwner, observer: (String) -> Unit) {
        errorEvent.observe(owner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                observer(it)
            }
        })
    }

    fun logEvent(event: String, parameters: Map<String, Any> = emptyMap()) {
        try {
            FirebaseCrashlytics.getInstance().apply {
                log("Event: $event")
                parameters.forEach { (key, value) ->
                    setCustomKey(key, value.toString())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event to Crashlytics", e)
        }
    }

    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}