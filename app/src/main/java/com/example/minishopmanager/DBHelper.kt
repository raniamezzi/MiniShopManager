package com.example.minishopmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class OrderItem(
    val id: Int,
    val orderNumber: String,
    val date: String,
    val total: Double,
    val address: String,
    val paymentMethod: String,
    val itemsSummary: String,
    val status: String = "Delivered"
)

class DBHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mini_shop_manager.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_CART = "panier"
        private const val TABLE_FAVORITES = "favoris"
        private const val TABLE_ORDERS = "commandes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CART (
                product_id INTEGER PRIMARY KEY,
                quantity INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_FAVORITES (
                product_id INTEGER PRIMARY KEY
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_number TEXT NOT NULL,
                date TEXT NOT NULL,
                total REAL NOT NULL,
                address TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                items_summary TEXT NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    // ==================== CART ====================

    fun addToCart(productId: Int, quantityToAdd: Int = 1) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT quantity FROM $TABLE_CART WHERE product_id = ?",
            arrayOf(productId.toString())
        )
        if (cursor.moveToFirst()) {
            val currentQty = cursor.getInt(0)
            val values = ContentValues().apply {
                put("quantity", currentQty + quantityToAdd)
            }
            db.update(TABLE_CART, values, "product_id = ?", arrayOf(productId.toString()))
        } else {
            val values = ContentValues().apply {
                put("product_id", productId)
                put("quantity", quantityToAdd)
            }
            db.insert(TABLE_CART, null, values)
        }
        cursor.close()
    }

    fun updateCartQuantity(productId: Int, newQuantity: Int) {
        val db = writableDatabase
        if (newQuantity <= 0) {
            db.delete(TABLE_CART, "product_id = ?", arrayOf(productId.toString()))
        } else {
            val values = ContentValues().apply {
                put("quantity", newQuantity)
            }
            db.update(TABLE_CART, values, "product_id = ?", arrayOf(productId.toString()))
        }
    }

    fun removeFromCart(productId: Int) {
        val db = writableDatabase
        db.delete(TABLE_CART, "product_id = ?", arrayOf(productId.toString()))
    }

    fun clearCart() {
        val db = writableDatabase
        db.delete(TABLE_CART, null, null)
    }

    fun getCartItems(): List<CartItem> {
        val allProducts = JsonHelper.loadProducts(context).associateBy { it.id }
        val items = mutableListOf<CartItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT product_id, quantity FROM $TABLE_CART", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val qty = cursor.getInt(1)
            val product = allProducts[id]
            if (product != null) {
                items.add(CartItem(product, qty))
            }
        }
        cursor.close()
        return items
    }

    fun getCartCount(): Int {
        var total = 0
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(quantity) FROM $TABLE_CART", null)
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getCartSubtotal(): Double {
        return getCartItems().sumOf { it.product.price * it.quantity }
    }

    // ==================== WISHLIST / FAVORITES ====================

    fun isFavorite(productId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_FAVORITES WHERE product_id = ?",
            arrayOf(productId.toString())
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun addFavorite(productId: Int) {
        if (!isFavorite(productId)) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("product_id", productId)
            }
            db.insert(TABLE_FAVORITES, null, values)
        }
    }

    fun removeFavorite(productId: Int) {
        val db = writableDatabase
        db.delete(TABLE_FAVORITES, "product_id = ?", arrayOf(productId.toString()))
    }

    fun toggleFavorite(productId: Int): Boolean {
        return if (isFavorite(productId)) {
            removeFavorite(productId)
            false
        } else {
            addFavorite(productId)
            true
        }
    }

    fun getFavoriteIds(): List<Int> {
        val ids = mutableListOf<Int>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT product_id FROM $TABLE_FAVORITES", null)
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0))
        }
        cursor.close()
        return ids
    }

    // ==================== ORDERS ====================

    fun placeOrder(
        total: Double,
        address: String,
        paymentMethod: String,
        itemsSummary: String
    ): String {
        val db = writableDatabase
        val orderNum = "#MSM-${(1000..9999).random()}"
        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())

        val values = ContentValues().apply {
            put("order_number", orderNum)
            put("date", dateStr)
            put("total", total)
            put("address", address)
            put("payment_method", paymentMethod)
            put("items_summary", itemsSummary)
            put("status", "Confirmed")
        }
        db.insert(TABLE_ORDERS, null, values)
        clearCart()
        return orderNum
    }

    fun getOrders(): List<OrderItem> {
        val orders = mutableListOf<OrderItem>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, order_number, date, total, address, payment_method, items_summary, status FROM $TABLE_ORDERS ORDER BY id DESC",
            null
        )
        while (cursor.moveToNext()) {
            orders.add(
                OrderItem(
                    id = cursor.getInt(0),
                    orderNumber = cursor.getString(1),
                    date = cursor.getString(2),
                    total = cursor.getDouble(3),
                    address = cursor.getString(4),
                    paymentMethod = cursor.getString(5),
                    itemsSummary = cursor.getString(6),
                    status = cursor.getString(7)
                )
            )
        }
        cursor.close()
        return orders
    }
}