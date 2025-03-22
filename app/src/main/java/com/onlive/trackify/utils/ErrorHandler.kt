package com.onlive.trackify.utils

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.onlive.trackify.R

class ErrorHandler private constructor(private val context: Context) {

    companion object {
        private var INSTANCE: ErrorHandler? = null

        fun getInstance(context: Context): ErrorHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = ErrorHandler(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent = _errorEvent

    fun handleError(error: Any?, showToast: Boolean = true) {
        val errorMessage = when (error) {
            is String -> error
            is Exception -> getReadableErrorMessage(error)
            is Result.Error -> error.message
            else -> context.getString(R.string.unknown_error)
        }

        _errorEvent.postValue(Event(errorMessage))
    }

    private fun getReadableErrorMessage(e: Exception): String {
        return e.message ?: context.getString(R.string.unknown_error)
    }

    fun observeErrors(owner: LifecycleOwner, observer: (String) -> Unit) {
        errorEvent.observe(owner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                observer(it)
            }
        })
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