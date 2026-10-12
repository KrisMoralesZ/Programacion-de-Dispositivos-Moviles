package com.example.actividad_12

import java.math.BigDecimal

/**
 * Validates the raw text typed in the product form before it is saved to SQLite.
 *
 * Kept free of Android classes so it can be covered by plain JVM unit tests.
 */
object ProductValidator {

    const val MAX_NAME_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 500
    val MAX_PRICE = BigDecimal("999999.99")
    const val MAX_QUANTITY = 1_000_000
    private const val PRICE_DECIMALS = 2

    enum class FieldError {
        REQUIRED,
        TOO_LONG,
        NOT_A_NUMBER,
        NOT_A_WHOLE_NUMBER,
        TOO_MANY_DECIMALS,
        MUST_BE_POSITIVE,
        MUST_NOT_BE_NEGATIVE,
        TOO_LARGE,
    }

    sealed interface Result {
        data class Valid(
            val name: String,
            val description: String,
            val price: Double,
            val quantity: Int,
        ) : Result

        /** Each property holds the error of that field, or null when the field is valid. */
        data class Invalid(
            val nameError: FieldError?,
            val descriptionError: FieldError?,
            val priceError: FieldError?,
            val quantityError: FieldError?,
        ) : Result
    }

    fun validate(name: String, description: String, price: String, quantity: String): Result {
        val trimmedName = name.trim()
        val trimmedDescription = description.trim()
        // Some keyboards type a comma as the decimal separator.
        val parsedPrice = price.trim().replace(',', '.').toBigDecimalOrNull()
        val parsedQuantity = quantity.trim().toBigDecimalOrNull()

        val nameError = when {
            trimmedName.isEmpty() -> FieldError.REQUIRED
            trimmedName.length > MAX_NAME_LENGTH -> FieldError.TOO_LONG
            else -> null
        }
        val descriptionError =
            if (trimmedDescription.length > MAX_DESCRIPTION_LENGTH) FieldError.TOO_LONG else null
        val priceError = when {
            price.isBlank() -> FieldError.REQUIRED
            parsedPrice == null -> FieldError.NOT_A_NUMBER
            parsedPrice.signum() <= 0 -> FieldError.MUST_BE_POSITIVE
            parsedPrice.stripTrailingZeros().scale() > PRICE_DECIMALS -> FieldError.TOO_MANY_DECIMALS
            parsedPrice > MAX_PRICE -> FieldError.TOO_LARGE
            else -> null
        }
        val quantityError = when {
            quantity.isBlank() -> FieldError.REQUIRED
            parsedQuantity == null -> FieldError.NOT_A_NUMBER
            parsedQuantity.signum() < 0 -> FieldError.MUST_NOT_BE_NEGATIVE
            parsedQuantity.stripTrailingZeros().scale() > 0 -> FieldError.NOT_A_WHOLE_NUMBER
            parsedQuantity > BigDecimal(MAX_QUANTITY) -> FieldError.TOO_LARGE
            else -> null
        }

        if (nameError != null || descriptionError != null || priceError != null || quantityError != null) {
            return Result.Invalid(nameError, descriptionError, priceError, quantityError)
        }
        return Result.Valid(
            name = trimmedName,
            description = trimmedDescription,
            price = parsedPrice!!.toDouble(),
            quantity = parsedQuantity!!.toInt(),
        )
    }
}
