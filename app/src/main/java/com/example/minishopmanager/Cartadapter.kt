package com.example.minishopmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val items: List<CartItem>
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCartName)
        val tvDetail: TextView = itemView.findViewById(R.id.tvCartDetail)
        val tvTotal: TextView = itemView.findViewById(R.id.tvCartTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.nom
        holder.tvDetail.text = "${item.quantite} × ${formatTnd(item.prix)}"
        holder.tvTotal.text = formatTnd(item.total)
    }
}