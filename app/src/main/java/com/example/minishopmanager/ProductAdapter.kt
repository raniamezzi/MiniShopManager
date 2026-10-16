package com.example.minishopmanager

import android.content.ContentValues
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class ProductAdapter(
    context: Context,
    private val products: List<Product>
) : ArrayAdapter<Product>(context, 0, products) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)

        val product = getItem(position)

        val imgProduct = view.findViewById<ImageView>(R.id.imgProduct)
        val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
        val btnBuy = view.findViewById<Button>(R.id.btnBuy)

        product?.let { currentProduct ->
            tvProductName.text = "Produit ${position + 1} : ${currentProduct.name}"
            imgProduct.setImageResource(currentProduct.imageResId)

            btnBuy.setOnClickListener {
                // 1. Insertion SQLite dans la table panier
                val dbHelper = DBHelper(context)
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("nom", currentProduct.name)
                }
                db.insert("panier", null, values)
                db.close()

                // 2. Toast d'information
                Toast.makeText(context, "Produit ajouté au panier : ${currentProduct.name}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}