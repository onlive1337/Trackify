package com.onlive.trackify.utils

object InputValidator {
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MAX_NOTES_LENGTH = 1000

    fun validateSubscriptionName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Введите название подписки")
            name.length > MAX_NAME_LENGTH -> ValidationResult.Error("Название слишком длинное (максимум $MAX_NAME_LENGTH символов)")
            containsOnlyWhitespace(name) -> ValidationResult.Error("Название не может состоять только из пробелов")
            containsControlCharacters(name) -> ValidationResult.Error("Название содержит недопустимые символы")
            else -> ValidationResult.Success(name.trim())
        }
    }

    fun validateDescription(description: String?): ValidationResult {
        if (description.isNullOrBlank()) {
            return ValidationResult.Success("")
        }

        return when {
            description.length > MAX_DESCRIPTION_LENGTH ->
                ValidationResult.Error("Описание слишком длинное (максимум $MAX_DESCRIPTION_LENGTH символов)")
            containsControlCharacters(description) ->
                ValidationResult.Error("Описание содержит недопустимые символы")
            else -> ValidationResult.Success(description.trim())
        }
    }

    fun validateNotes(notes: String?): ValidationResult {
        if (notes.isNullOrBlank()) {
            return ValidationResult.Success("")
        }

        return when {
            notes.length > MAX_NOTES_LENGTH ->
                ValidationResult.Error("Примечания слишком длинные (максимум $MAX_NOTES_LENGTH символов)")
            containsControlCharacters(notes) ->
                ValidationResult.Error("Примечания содержат недопустимые символы")
            else -> ValidationResult.Success(notes.trim())
        }
    }

    fun validateCategoryName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Введите название категории")
            name.length > MAX_NAME_LENGTH -> ValidationResult.Error("Название слишком длинное (максимум $MAX_NAME_LENGTH символов)")
            containsOnlyWhitespace(name) -> ValidationResult.Error("Название не может состоять только из пробелов")
            containsControlCharacters(name) -> ValidationResult.Error("Название содержит недопустимые символы")
            else -> ValidationResult.Success(name.trim())
        }
    }

    private fun containsOnlyWhitespace(text: String): Boolean {
        return text.isNotBlank() && text.trim().isEmpty()
    }

    private fun containsControlCharacters(text: String): Boolean {
        return text.any { char ->
            char.isISOControl() && char != '\n' && char != '\r' && char != '\t'
        }
    }

    sealed class ValidationResult {
        data class Success(val value: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}