package com.example.minishopmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartItem>,
    private val dbHelper: DBHelper,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivCartItemImage)
        val tvName: TextView = itemView.findViewById(R.id.tvCartItemName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val btnMinus: TextView = itemView.findViewById(R.id.btnCartMinus)
        val tvQty: TextView = itemView.findViewById(R.id.tvCartQty)
        val btnPlus: TextView = itemView.findViewById(R.id.btnCartPlus)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnCartRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvName.text = item.product.name
        holder.tvPrice.text = ProductAdapter.formatPriceDT(item.product.price * item.quantity)
        holder.tvQty.text = item.quantity.toString()

        val resId = JsonHelper.getDrawableId(context, item.product.image)
        holder.ivImage.setImageResource(if (resId != 0) resId else R.drawable.ic_app_logo)

        holder.btnMinus.setOnClickListener {
            dbHelper.updateCartQuantity(item.product.id, item.quantity - 1)
            onCartUpdated()
        }

        holder.btnPlus.setOnClickListener {
            dbHelper.updateCartQuantity(item.product.id, item.quantity + 1)
            onCartUpdated()
        }

        holder.btnRemove.setOnClickListener {
            dbHelper.removeFromCart(item.product.id)
            onCartUpdated()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}