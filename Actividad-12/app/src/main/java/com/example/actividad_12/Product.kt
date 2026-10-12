package com.example.actividad_12

/** A product record stored in the `products` table of the inventory database. */
data class Product(
    val id: Long = NO_ID,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
) {
    companion object {
        /** Id used for products that have not been saved to the database yet. */
        const val NO_ID = -1L
    }
}
