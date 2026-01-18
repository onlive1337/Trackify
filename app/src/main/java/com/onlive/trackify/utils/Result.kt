package com.onlive.trackify.utils

sealed class Result<out T> {
    @Suppress("unused")
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
}