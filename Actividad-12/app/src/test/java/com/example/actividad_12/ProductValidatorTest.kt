package com.example.actividad_12

import com.example.actividad_12.ProductValidator.FieldError
import com.example.actividad_12.ProductValidator.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductValidatorTest {

    private fun invalid(name: String = "Rice", description: String = "", price: String = "25.50", quantity: String = "10") =
        ProductValidator.validate(name, description, price, quantity) as Result.Invalid

    @Test
    fun validInput_isTrimmedAndParsed() {
        val result = ProductValidator.validate("  Rice 1 kg ", " White rice ", " 25.50 ", " 10 ")

        assertEquals(Result.Valid("Rice 1 kg", "White rice", 25.50, 10), result)
    }

    @Test
    fun descriptionIsOptional_andZeroStockIsAllowed() {
        val result = ProductValidator.validate("Salt", "", "8", "0")

        assertEquals(Result.Valid("Salt", "", 8.0, 0), result)
    }

    @Test
    fun commaDecimalSeparator_isAccepted() {
        val result = ProductValidator.validate("Beans", "", "12,75", "3")

        assertEquals(12.75, (result as Result.Valid).price, 0.0)
    }

    @Test
    fun missingRequiredFields_areReported() {
        val result = invalid(name = "   ", price = "", quantity = "")

        assertEquals(FieldError.REQUIRED, result.nameError)
        assertEquals(FieldError.REQUIRED, result.priceError)
        assertEquals(FieldError.REQUIRED, result.quantityError)
        assertNull(result.descriptionError)
    }

    @Test
    fun invalidPrices_areRejected() {
        assertEquals(FieldError.NOT_A_NUMBER, invalid(price = "abc").priceError)
        assertEquals(FieldError.NOT_A_NUMBER, invalid(price = "1.2.3").priceError)
        assertEquals(FieldError.MUST_BE_POSITIVE, invalid(price = "0").priceError)
        assertEquals(FieldError.MUST_BE_POSITIVE, invalid(price = "-5").priceError)
        assertEquals(FieldError.TOO_MANY_DECIMALS, invalid(price = "1.999").priceError)
        assertEquals(FieldError.TOO_LARGE, invalid(price = "1000000").priceError)
    }

    @Test
    fun invalidQuantities_areRejected() {
        assertEquals(FieldError.MUST_NOT_BE_NEGATIVE, invalid(quantity = "-1").quantityError)
        assertEquals(FieldError.NOT_A_WHOLE_NUMBER, invalid(quantity = "2.5").quantityError)
        assertEquals(FieldError.NOT_A_NUMBER, invalid(quantity = "ten").quantityError)
        assertEquals(FieldError.TOO_LARGE, invalid(quantity = "1000001").quantityError)
    }

    @Test
    fun overlongTexts_areRejected() {
        val result = invalid(name = "x".repeat(101), description = "y".repeat(501))

        assertEquals(FieldError.TOO_LONG, result.nameError)
        assertEquals(FieldError.TOO_LONG, result.descriptionError)
    }

    @Test
    fun limits_areInclusive() {
        val result = ProductValidator.validate("x".repeat(100), "", "999999.99", "1000000")

        assertTrue(result is Result.Valid)
    }
}
