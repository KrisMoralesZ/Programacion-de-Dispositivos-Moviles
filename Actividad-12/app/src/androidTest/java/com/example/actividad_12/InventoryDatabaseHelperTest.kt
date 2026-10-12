package com.example.actividad_12

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs every CRUD operation of [InventoryDatabaseHelper] against a real SQLite database. */
@RunWith(AndroidJUnit4::class)
class InventoryDatabaseHelperTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var helper: InventoryDatabaseHelper

    @Before
    fun setUp() {
        // In-memory database, so the tests never touch the app's real inventory.db.
        helper = InventoryDatabaseHelper(context, databaseName = null)
    }

    @After
    fun tearDown() {
        helper.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun insert_thenReadById_returnsSameData() {
        val id = helper.insertProduct(Product(name = "Rice 1 kg", description = "White rice", price = 25.5, quantity = 10))

        assertNotEquals(-1L, id)
        assertEquals(Product(id, "Rice 1 kg", "White rice", 25.5, 10), helper.getProductById(id))
    }

    @Test
    fun getAllProducts_isEmptyInitially_andSortedByName() {
        assertTrue(helper.getAllProducts().isEmpty())

        helper.insertProduct(Product(name = "sugar", description = "", price = 30.0, quantity = 5))
        helper.insertProduct(Product(name = "Beans", description = "", price = 22.0, quantity = 8))

        assertEquals(listOf("Beans", "sugar"), helper.getAllProducts().map { it.name })
    }

    @Test
    fun emptyDescription_roundTripsAsEmptyString() {
        val id = helper.insertProduct(Product(name = "Salt", description = "", price = 8.0, quantity = 0))

        assertEquals("", helper.getProductById(id)?.description)
    }

    @Test
    fun update_changesOnlyTheSelectedRecord() {
        val riceId = helper.insertProduct(Product(name = "Rice", description = "", price = 25.0, quantity = 10))
        val beansId = helper.insertProduct(Product(name = "Beans", description = "", price = 22.0, quantity = 8))

        val updated = Product(riceId, "Rice 2 kg", "Bigger bag", 48.0, 4)
        assertTrue(helper.updateProduct(updated))

        assertEquals(updated, helper.getProductById(riceId))
        assertEquals(Product(beansId, "Beans", "", 22.0, 8), helper.getProductById(beansId))
    }

    @Test
    fun delete_removesOnlyTheSelectedRecord() {
        val riceId = helper.insertProduct(Product(name = "Rice", description = "", price = 25.0, quantity = 10))
        val beansId = helper.insertProduct(Product(name = "Beans", description = "", price = 22.0, quantity = 8))

        assertTrue(helper.deleteProduct(riceId))

        assertNull(helper.getProductById(riceId))
        assertEquals(listOf(beansId), helper.getAllProducts().map { it.id })
    }

    @Test
    fun updateOrDeleteOfMissingId_returnsFalse() {
        assertFalse(helper.updateProduct(Product(999, "Ghost", "", 1.0, 1)))
        assertFalse(helper.deleteProduct(999))
    }

    @Test
    fun records_persistAfterClosingAndReopeningTheDatabaseFile() {
        context.deleteDatabase(TEST_DATABASE)
        val first = InventoryDatabaseHelper(context, TEST_DATABASE)
        val id = first.insertProduct(Product(name = "Coffee", description = "Instant", price = 95.0, quantity = 6))
        first.close()

        val reopened = InventoryDatabaseHelper(context, TEST_DATABASE)
        try {
            assertEquals(Product(id, "Coffee", "Instant", 95.0, 6), reopened.getProductById(id))
        } finally {
            reopened.close()
        }
    }

    private companion object {
        const val TEST_DATABASE = "inventory_test.db"
    }
}
