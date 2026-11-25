package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class CheckoutActivity : AppCompatActivity() {

    private var selectedPayment = "Cash on Delivery"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PrefsHelper(this)
        if (!prefs.isLoggedIn) {
            Toast.makeText(this, "Sign in required to place an order", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_checkout)

        val dbHelper = DBHelper(this)
        val cartItems = dbHelper.getCartItems()

        if (cartItems.isEmpty()) {
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnCheckoutBack)
        val etAddress = findViewById<EditText>(R.id.etCheckoutAddress)
        val etPhone = findViewById<EditText>(R.id.etCheckoutPhone)
        val optCash = findViewById<LinearLayout>(R.id.optionPaymentCash)
        val optCard = findViewById<LinearLayout>(R.id.optionPaymentCard)
        val tvItemsList = findViewById<TextView>(R.id.tvCheckoutItemsList)
        val tvTotal = findViewById<TextView>(R.id.tvCheckoutTotal)
        val btnConfirm = findViewById<AppCompatButton>(R.id.btnConfirmOrder)

        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val delivery = if (subtotal >= 300.0) 0.0 else 7.0
        val grandTotal = subtotal + delivery

        val summaryLines = cartItems.joinToString("\n") {
            "${it.quantity}× ${it.product.name}  (${ProductAdapter.formatPriceDT(it.product.price * it.quantity)})"
        }
        tvItemsList.text = summaryLines
        tvTotal.text = ProductAdapter.formatPriceDT(grandTotal)

        btnBack.setOnClickListener { finish() }

        optCash.setOnClickListener {
            selectedPayment = "Cash on Delivery"
            optCash.setBackgroundResource(R.drawable.bg_payment_selected)
            optCard.setBackgroundResource(R.drawable.bg_payment_unselected)
        }

        optCard.setOnClickListener {
            selectedPayment = "Credit / e-Dinar Card"
            optCard.setBackgroundResource(R.drawable.bg_payment_selected)
            optCash.setBackgroundResource(R.drawable.bg_payment_unselected)
        }

        btnConfirm.setOnClickListener {
            val address = etAddress.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (address.isEmpty()) {
                etAddress.error = "Please enter your delivery address"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                etPhone.error = "Please enter a contact phone number"
                return@setOnClickListener
            }

            val fullAddress = "$address ($phone)"
            val compactSummary = cartItems.joinToString(", ") { "${it.quantity}× ${it.product.name}" }
            val orderNum = dbHelper.placeOrder(grandTotal, fullAddress, selectedPayment, compactSummary)

            AlertDialog.Builder(this)
                .setTitle("Order Confirmed!")
                .setMessage("Your order $orderNum for ${ProductAdapter.formatPriceDT(grandTotal)} has been placed.\n\nYou can track it anytime in your Profile tab.")
                .setCancelable(false)
                .setPositiveButton("Done") { _, _ -> finish() }
                .show()
        }
    }
}