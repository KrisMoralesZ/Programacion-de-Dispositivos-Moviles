package com.example.actividad_12

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Owns the local SQLite database `inventory.db` and every CRUD operation on the `products` table.
 *
 * Activities only talk to this class, so no SQL lives in the UI code. The helper keeps a single
 * database connection open; call [close] when the owning component is destroyed.
 *
 * @param databaseName file name of the database; pass null to get a temporary in-memory database
 * (used by the instrumented tests).
 */
class InventoryDatabaseHelper(
    context: Context,
    databaseName: String? = DATABASE_NAME,
) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PRODUCTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrations are applied one version at a time so that existing inventory records are
        // kept instead of dropping the table. Version 1 is the initial schema, so there are no
        // steps yet; a future version 2 would add, for example:
        //   if (oldVersion < 2) db.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN barcode TEXT")
    }

    /** Inserts [product] (its id is ignored) and returns the new row id, or -1 on failure. */
    fun insertProduct(product: Product): Long = try {
        writableDatabase.insertOrThrow(TABLE_PRODUCTS, null, product.toContentValues())
    } catch (e: SQLException) {
        Log.e(TAG, "Could not insert product '${product.name}'", e)
        -1L
    }

    /** Returns every product ordered alphabetically by name. */
    fun getAllProducts(): List<Product> =
        readableDatabase.query(
            TABLE_PRODUCTS, ALL_COLUMNS, null, null, null, null, "$COLUMN_NAME COLLATE NOCASE ASC",
        ).use { cursor ->
            val products = ArrayList<Product>(cursor.count)
            while (cursor.moveToNext()) {
                products.add(cursor.toProduct())
            }
            products
        }

    /** Returns the product with the given [id], or null if it does not exist. */
    fun getProductById(id: Long): Product? =
        readableDatabase.query(
            TABLE_PRODUCTS, ALL_COLUMNS, "$COLUMN_ID = ?", arrayOf(id.toString()), null, null, null,
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toProduct() else null
        }

    /** Overwrites the record identified by [Product.id]. Returns true if exactly one row changed. */
    fun updateProduct(product: Product): Boolean = try {
        writableDatabase.update(
            TABLE_PRODUCTS,
            product.toContentValues(),
            "$COLUMN_ID = ?",
            arrayOf(product.id.toString()),
        ) == 1
    } catch (e: SQLException) {
        Log.e(TAG, "Could not update product ${product.id}", e)
        false
    }

    /** Deletes the record with the given [id]. Returns true if a row was deleted. */
    fun deleteProduct(id: Long): Boolean = try {
        writableDatabase.delete(TABLE_PRODUCTS, "$COLUMN_ID = ?", arrayOf(id.toString())) == 1
    } catch (e: SQLException) {
        Log.e(TAG, "Could not delete product $id", e)
        false
    }

    private fun Product.toContentValues() = ContentValues().apply {
        put(COLUMN_NAME, name)
        // Store an empty description as SQL NULL, since the column is optional.
        put(COLUMN_DESCRIPTION, description.ifBlank { null })
        put(COLUMN_PRICE, price)
        put(COLUMN_QUANTITY, quantity)
    }

    private fun Cursor.toProduct() = Product(
        id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
        name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
        description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: "",
        price = getDouble(getColumnIndexOrThrow(COLUMN_PRICE)),
        quantity = getInt(getColumnIndexOrThrow(COLUMN_QUANTITY)),
    )

    companion object {
        private const val TAG = "InventoryDatabaseHelper"

        const val DATABASE_NAME = "inventory.db"
        const val DATABASE_VERSION = 1

        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_PRICE = "price"
        const val COLUMN_QUANTITY = "quantity"

        private val ALL_COLUMNS =
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_PRICE, COLUMN_QUANTITY)

        private const val SQL_CREATE_PRODUCTS = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_QUANTITY INTEGER NOT NULL
            )
        """
    }
}
