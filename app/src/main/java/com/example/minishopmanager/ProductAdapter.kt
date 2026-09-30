package com.example.minishopmanager

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
            // Affiche "Produit 1 : Téléphone", etc.
            tvProductName.text = "Produit ${position + 1} : ${currentProduct.name}"
            imgProduct.setImageResource(currentProduct.imageResId)

            // Gestion du clic sur le bouton Acheter
            btnBuy.setOnClickListener {
                Toast.makeText(context, "Produit sélectionné : ${currentProduct.name}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}