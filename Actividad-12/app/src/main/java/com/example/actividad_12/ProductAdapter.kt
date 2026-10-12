package com.example.actividad_12

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/** Shows each product's name, price and stock quantity in the inventory ListView. */
class ProductAdapter(context: Context) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private var products: List<Product> = emptyList()

    fun submitList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun getCount() = products.size

    override fun getItem(position: Int) = products[position]

    override fun getItemId(position: Int) = products[position].id

    override fun hasStableIds() = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_product, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(view).also { view.tag = it }
        val product = products[position]
        val resources = view.resources

        holder.name.text = product.name
        holder.price.text = resources.getString(R.string.product_price, product.price)
        holder.stock.text =
            resources.getQuantityString(R.plurals.product_stock, product.quantity, product.quantity)
        return view
    }

    private class ViewHolder(view: View) {
        val name: TextView = view.findViewById(R.id.textProductName)
        val price: TextView = view.findViewById(R.id.textProductPrice)
        val stock: TextView = view.findViewById(R.id.textProductStock)
    }
}
