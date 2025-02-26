package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.net.UnknownHostException
import java.sql.SQLException

class ErrorHandler private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ErrorHandler"
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
            else -> "Произошла неизвестная ошибка"
        }

        Log.e(TAG, "Error: $errorMessage")

        if (showToast) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        _errorEvent.postValue(Event(errorMessage))
    }

    private fun getReadableErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "Нет подключения к интернету"
            is SQLException -> "Ошибка при работе с базой данных"
            else -> e.message ?: "Произошла неизвестная ошибка"
        }
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