package com.example.minishopmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ProductAdapter(
    private val context: Context,
    private val products: List<Product>
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val dbHelper = DBHelper(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnBuy: Button = itemView.findViewById(R.id.btnBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.imgProduct.setImageResource(product.imageResId)
        holder.tvBrand.text = product.brand
        holder.tvProductName.text = product.name
        holder.tvCategory.text = product.category
        holder.tvDescription.text = product.description
        holder.tvPrice.text = formatPrix(product.price)

        holder.btnBuy.setOnClickListener {
            // Insertion SQLite : produit_id, nom, prix et quantité (1 par défaut)
            dbHelper.ajouterAuPanier(
                produitId = product.id,
                nom = product.name,
                prix = product.price
            )

            Toast.makeText(
                context,
                "Produit ajouté au panier : ${product.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatPrix(prix: Double): String {
        return String.format(Locale.FRANCE, "%,.2f TND", prix)
    }
}