package com.onlive.trackify.utils

import androidx.annotation.StringRes

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(
        @StringRes val messageRes: Int,
        val exception: Throwable? = null
    ) : Result<Nothing>()
}
