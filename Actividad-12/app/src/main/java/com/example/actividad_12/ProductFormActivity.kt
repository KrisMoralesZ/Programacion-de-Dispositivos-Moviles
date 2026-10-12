package com.example.actividad_12

import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividad_12.ProductValidator.FieldError
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

/**
 * Form used both to register a new product and to edit an existing one.
 *
 * Edit mode is selected by passing the product's database id in [EXTRA_PRODUCT_ID].
 */
class ProductFormActivity : AppCompatActivity() {

    private lateinit var databaseHelper: InventoryDatabaseHelper

    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutDescription: TextInputLayout
    private lateinit var layoutPrice: TextInputLayout
    private lateinit var layoutQuantity: TextInputLayout
    private lateinit var editName: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var editPrice: TextInputEditText
    private lateinit var editQuantity: TextInputEditText

    private var productId = Product.NO_ID
    private val isEditMode get() = productId != Product.NO_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)
        findViewById<View>(R.id.formRoot).applySystemBarInsets()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHelper = InventoryDatabaseHelper(this)
        productId = intent.getLongExtra(EXTRA_PRODUCT_ID, Product.NO_ID)

        layoutName = findViewById(R.id.layoutName)
        layoutDescription = findViewById(R.id.layoutDescription)
        layoutPrice = findViewById(R.id.layoutPrice)
        layoutQuantity = findViewById(R.id.layoutQuantity)
        editName = findViewById(R.id.editName)
        editDescription = findViewById(R.id.editDescription)
        editPrice = findViewById(R.id.editPrice)
        editQuantity = findViewById(R.id.editQuantity)

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        setTitle(if (isEditMode) R.string.title_edit_product else R.string.title_add_product)
        buttonSave.setText(if (isEditMode) R.string.action_save_changes else R.string.action_save)
        buttonSave.setOnClickListener { saveProduct() }
        findViewById<Button>(R.id.buttonCancel).setOnClickListener { finish() }
        editQuantity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveProduct()
                true
            } else {
                false
            }
        }

        // After a configuration change the EditTexts restore what the user typed by themselves.
        if (isEditMode && savedInstanceState == null) {
            loadProduct()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }

    private fun loadProduct() {
        val product = try {
            databaseHelper.getProductById(productId)
        } catch (e: SQLException) {
            null
        }
        if (product == null) {
            showToast(R.string.message_product_not_found)
            finish()
            return
        }
        editName.setText(product.name)
        editDescription.setText(product.description)
        // Locale.ROOT keeps "." as the decimal separator so the value parses back correctly.
        editPrice.setText(String.format(Locale.ROOT, "%.2f", product.price))
        editQuantity.setText(product.quantity.toString())
    }

    private fun saveProduct() {
        val result = ProductValidator.validate(
            name = editName.text.toString(),
            description = editDescription.text.toString(),
            price = editPrice.text.toString(),
            quantity = editQuantity.text.toString(),
        )
        when (result) {
            is ProductValidator.Result.Invalid -> showValidationErrors(result)
            is ProductValidator.Result.Valid -> {
                clearValidationErrors()
                val product = Product(
                    id = productId,
                    name = result.name,
                    description = result.description,
                    price = result.price,
                    quantity = result.quantity,
                )
                val saved = if (isEditMode) {
                    databaseHelper.updateProduct(product)
                } else {
                    databaseHelper.insertProduct(product) != -1L
                }
                if (saved) {
                    showToast(if (isEditMode) R.string.message_product_updated else R.string.message_product_saved)
                    finish()
                } else {
                    showToast(R.string.message_save_error)
                }
            }
        }
    }

    private fun showValidationErrors(result: ProductValidator.Result.Invalid) {
        layoutName.error = result.nameError?.let { nameErrorMessage(it) }
        layoutDescription.error = result.descriptionError?.let {
            getString(R.string.error_description_too_long, ProductValidator.MAX_DESCRIPTION_LENGTH)
        }
        layoutPrice.error = result.priceError?.let { priceErrorMessage(it) }
        layoutQuantity.error = result.quantityError?.let { quantityErrorMessage(it) }

        // Move the cursor to the first field that needs fixing.
        when {
            result.nameError != null -> editName.requestFocus()
            result.descriptionError != null -> editDescription.requestFocus()
            result.priceError != null -> editPrice.requestFocus()
            else -> editQuantity.requestFocus()
        }
        showToast(R.string.message_fix_errors)
    }

    private fun clearValidationErrors() {
        layoutName.error = null
        layoutDescription.error = null
        layoutPrice.error = null
        layoutQuantity.error = null
    }

    private fun nameErrorMessage(error: FieldError) = when (error) {
        FieldError.TOO_LONG -> getString(R.string.error_name_too_long, ProductValidator.MAX_NAME_LENGTH)
        else -> getString(R.string.error_name_required)
    }

    private fun priceErrorMessage(error: FieldError) = when (error) {
        FieldError.REQUIRED -> getString(R.string.error_price_required)
        FieldError.MUST_BE_POSITIVE -> getString(R.string.error_price_not_positive)
        FieldError.TOO_MANY_DECIMALS -> getString(R.string.error_price_decimals)
        FieldError.TOO_LARGE -> getString(R.string.error_price_too_large, ProductValidator.MAX_PRICE.toDouble())
        else -> getString(R.string.error_price_invalid)
    }

    private fun quantityErrorMessage(error: FieldError) = when (error) {
        FieldError.REQUIRED -> getString(R.string.error_quantity_required)
        FieldError.MUST_NOT_BE_NEGATIVE -> getString(R.string.error_quantity_negative)
        FieldError.NOT_A_WHOLE_NUMBER -> getString(R.string.error_quantity_whole)
        FieldError.TOO_LARGE -> getString(R.string.error_quantity_too_large, ProductValidator.MAX_QUANTITY)
        else -> getString(R.string.error_quantity_invalid)
    }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "com.example.actividad_12.extra.PRODUCT_ID"

        /** Opens the form to add a product, or to edit the product with [productId] if given. */
        fun newIntent(context: Context, productId: Long = Product.NO_ID): Intent =
            Intent(context, ProductFormActivity::class.java).putExtra(EXTRA_PRODUCT_ID, productId)
    }
}
