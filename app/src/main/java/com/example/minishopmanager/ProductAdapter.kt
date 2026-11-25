package com.example.minishopmanager

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val dbHelper: DBHelper,
    private var isGridMode: Boolean = true,
    private val onDataChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val btnWishlist: ImageButton = itemView.findViewById(R.id.btnWishlist)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnQuickAdd: TextView = itemView.findViewById(R.id.btnQuickAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutRes = if (isGridMode) R.layout.item_product_grid else R.layout.item_product_list
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        holder.tvCategory.text = product.category
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = formatPriceDT(product.price)

        val resId = JsonHelper.getDrawableId(context, product.image)
        if (resId != 0) {
            holder.ivProductImage.setImageResource(resId)
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_app_logo)
        }

        val isFav = dbHelper.isFavorite(product.id)
        holder.btnWishlist.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        holder.btnWishlist.setOnClickListener {
            if (dbHelper.isFavorite(product.id)) {
                dbHelper.removeFavorite(product.id)
                holder.btnWishlist.setImageResource(R.drawable.ic_heart_outline)
                Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addFavorite(product.id)
                holder.btnWishlist.setImageResource(R.drawable.ic_heart_filled)
                Toast.makeText(context, "Saved to Wishlist", Toast.LENGTH_SHORT).show()
            }
            onDataChanged?.invoke()
        }

        holder.btnQuickAdd.setOnClickListener {
            dbHelper.addToCart(product.id, 1)
            Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            (context as? MainActivity)?.updateCartBadge()
            onDataChanged?.invoke()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCT_ID", product.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    fun setGridMode(grid: Boolean) {
        if (isGridMode != grid) {
            isGridMode = grid
            notifyDataSetChanged()
        }
    }

    companion object {
        fun formatPriceDT(price: Double): String {
            val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = if (price % 1.0 == 0.0) 0 else 2
            }.format(price)
            return "$formatted DT"
        }
    }
}