package com.example.actividad_12

import android.database.SQLException
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Main screen: lists every product in the inventory and is the entry point for the
 * create, view, edit and delete operations.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: InventoryDatabaseHelper
    private lateinit var productAdapter: ProductAdapter
    private lateinit var textProductCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main).applySystemBarInsets()
        supportActionBar?.subtitle = getString(R.string.business_name)

        databaseHelper = InventoryDatabaseHelper(this)
        productAdapter = ProductAdapter(this)
        textProductCount = findViewById(R.id.textProductCount)

        val listProducts = findViewById<ListView>(R.id.listProducts)
        listProducts.adapter = productAdapter
        listProducts.emptyView = findViewById(R.id.textEmptyInventory)
        listProducts.setOnItemClickListener { _, _, position, _ ->
            showProductDetails(productAdapter.getItem(position))
        }
        listProducts.setOnItemLongClickListener { _, _, position, _ ->
            confirmDelete(productAdapter.getItem(position))
            true
        }

        findViewById<View>(R.id.buttonAddProduct).setOnClickListener {
            startActivity(ProductFormActivity.newIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload every time the screen is shown so changes made in the form are reflected.
        loadProducts()
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }

    private fun loadProducts() {
        val products = try {
            databaseHelper.getAllProducts()
        } catch (e: SQLException) {
            showToast(R.string.message_load_error)
            emptyList()
        }
        productAdapter.submitList(products)
        textProductCount.text =
            resources.getQuantityString(R.plurals.product_count, products.size, products.size)
    }

    private fun showProductDetails(product: Product) {
        val description = product.description.ifBlank { getString(R.string.no_description) }
        MaterialAlertDialogBuilder(this)
            .setTitle(product.name)
            .setMessage(
                getString(
                    R.string.product_details,
                    product.id,
                    description,
                    product.price,
                    product.quantity,
                ),
            )
            .setPositiveButton(R.string.action_edit) { _, _ ->
                startActivity(ProductFormActivity.newIntent(this, product.id))
            }
            .setNegativeButton(R.string.action_delete) { _, _ -> confirmDelete(product) }
            .setNeutralButton(R.string.action_close, null)
            .show()
    }

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, product.name))
            .setPositiveButton(R.string.action_delete) { _, _ -> deleteProduct(product) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        if (databaseHelper.deleteProduct(product.id)) {
            showToast(R.string.message_product_deleted)
        } else {
            showToast(R.string.message_delete_error)
        }
        loadProducts()
    }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}
