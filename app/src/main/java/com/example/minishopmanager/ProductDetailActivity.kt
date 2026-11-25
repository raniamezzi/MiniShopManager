package com.example.minishopmanager

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var product: Product? = null
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        dbHelper = DBHelper(this)
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        product = JsonHelper.loadProducts(this).find { it.id == productId }

        val item = product ?: run {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnDetailBack)
        val btnWishlist = findViewById<ImageButton>(R.id.btnDetailWishlist)
        val ivImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvStock = findViewById<TextView>(R.id.tvDetailStock)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvStorage = findViewById<TextView>(R.id.tvSpecStorage)
        val tvBattery = findViewById<TextView>(R.id.tvSpecBattery)
        val tvWeight = findViewById<TextView>(R.id.tvSpecWeight)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val btnMinus = findViewById<TextView>(R.id.btnQtyMinus)
        val btnPlus = findViewById<TextView>(R.id.btnQtyPlus)
        val tvQty = findViewById<TextView>(R.id.tvDetailQty)
        val btnAddToCart = findViewById<AppCompatButton>(R.id.btnDetailAddToCart)

        tvCategory.text = item.category.uppercase()
        tvName.text = item.name
        tvPrice.text = ProductAdapter.formatPriceDT(item.price)
        tvDescription.text = item.description
        tvStorage.text = item.storage.ifBlank { "Standard" }
        tvBattery.text = item.battery.ifBlank { "All-day" }
        tvWeight.text = item.weight.ifBlank { "Light" }

        tvStock.text = if (item.stock > 0) "In Stock (${item.stock})" else "Out of Stock"
        btnAddToCart.isEnabled = item.stock > 0

        val resId = JsonHelper.getDrawableId(this, item.image)
        ivImage.setImageResource(if (resId != 0) resId else R.drawable.ic_app_logo)

        fun refreshHeartIcon() {
            btnWishlist.setImageResource(
                if (dbHelper.isFavorite(item.id)) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }
        refreshHeartIcon()

        btnBack.setOnClickListener { finish() }

        btnWishlist.setOnClickListener {
            if (dbHelper.isFavorite(item.id)) {
                dbHelper.removeFavorite(item.id)
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addFavorite(item.id)
                Toast.makeText(this, "Added to Wishlist", Toast.LENGTH_SHORT).show()
            }
            refreshHeartIcon()
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQty.text = quantity.toString()
            }
        }

        btnPlus.setOnClickListener {
            if (quantity < item.stock.coerceAtLeast(1)) {
                quantity++
                tvQty.text = quantity.toString()
            }
        }

        btnAddToCart.setOnClickListener {
            dbHelper.addToCart(item.id, quantity)
            Toast.makeText(this, "Added $quantity × ${item.name} to cart", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}