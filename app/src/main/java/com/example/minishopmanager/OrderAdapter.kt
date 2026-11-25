package com.example.minishopmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private var orders: List<OrderItem>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderNumber: TextView = itemView.findViewById(R.id.tvOrderNumber)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderNumber.text = order.orderNumber
        holder.tvOrderStatus.text = order.status
        holder.tvOrderItems.text = order.itemsSummary
        holder.tvOrderDate.text = "${order.date} • ${order.paymentMethod}"
        holder.tvOrderTotal.text = ProductAdapter.formatPriceDT(order.total)
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderItem>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}