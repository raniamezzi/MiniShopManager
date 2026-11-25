package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: CartAdapter
    private lateinit var rvCart: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var summaryCard: LinearLayout
    private lateinit var tvClearCart: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        dbHelper = DBHelper(ctx)

        rvCart = view.findViewById(R.id.rvCartItems)
        emptyState = view.findViewById(R.id.cartEmptyState)
        summaryCard = view.findViewById(R.id.cartSummaryCard)
        tvClearCart = view.findViewById(R.id.tvClearCart)
        tvSubtotal = view.findViewById(R.id.tvCartSubtotal)
        tvDelivery = view.findViewById(R.id.tvCartDelivery)
        tvTotal = view.findViewById(R.id.tvCartTotal)

        rvCart.layoutManager = LinearLayoutManager(ctx)
        adapter = CartAdapter(emptyList(), dbHelper) {
            refreshCart()
        }
        rvCart.adapter = adapter

        tvClearCart.setOnClickListener {
            dbHelper.clearCart()
            refreshCart()
        }

        view.findViewById<TextView>(R.id.btnCartShopNow).setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_catalog)
        }

        view.findViewById<AppCompatButton>(R.id.btnProceedCheckout).setOnClickListener {
            val prefs = PrefsHelper(ctx)
            if (!prefs.isLoggedIn) {
                Toast.makeText(ctx, "Please sign in to proceed to checkout", Toast.LENGTH_SHORT).show()
                startActivity(Intent(ctx, LoginActivity::class.java))
            } else {
                startActivity(Intent(ctx, CheckoutActivity::class.java))
            }
        }

        refreshCart()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            refreshCart()
        }
    }

    private fun refreshCart() {
        val items = dbHelper.getCartItems()
        (activity as? MainActivity)?.updateCartBadge()

        if (items.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvCart.visibility = View.GONE
            summaryCard.visibility = View.GONE
            tvClearCart.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvCart.visibility = View.VISIBLE
            summaryCard.visibility = View.VISIBLE
            tvClearCart.visibility = View.VISIBLE

            adapter.updateItems(items)

            val subtotal = items.sumOf { it.product.price * it.quantity }
            val deliveryFee = if (subtotal >= 300.0) 0.0 else 7.0
            val grandTotal = subtotal + deliveryFee

            tvSubtotal.text = ProductAdapter.formatPriceDT(subtotal)
            tvDelivery.text = if (deliveryFee == 0.0) "Free" else ProductAdapter.formatPriceDT(deliveryFee)
            tvTotal.text = ProductAdapter.formatPriceDT(grandTotal)
        }
    }
}