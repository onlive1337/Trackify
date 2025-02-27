package com.onlive.trackify.utils

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()

    object Loading : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isLoading: Boolean
        get() = this is Loading

    fun getOrNull(): T? = when(this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when(this) {
        is Success -> data
        else -> defaultValue
    }

    fun errorMessage(): String? = when(this) {
        is Error -> message
        else -> null
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (String) -> R,
        onLoading: () -> R = { throw IllegalStateException("Loading state not handled") }
    ): R {
        return when(this) {
            is Success -> onSuccess(data)
            is Error -> onError(message)
            is Loading -> onLoading()
        }
    }

    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when(this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
}