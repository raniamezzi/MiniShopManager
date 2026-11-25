package com.example.minishopmanager

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        bottomNav = findViewById(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            loadFragmentForTab(item.itemId)
            true
        }

        findViewById<LinearLayout>(R.id.fabAskAi).setOnClickListener {
            AiAssistantBottomSheet().show(supportFragmentManager, "AiAssistantBottomSheet")
        }

        if (savedInstanceState == null) {
            loadFragmentForTab(R.id.nav_home)
        }

        updateCartBadge()
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            updateCartBadge()
        }
    }

    fun switchTab(itemId: Int) {
        if (bottomNav.selectedItemId != itemId) {
            bottomNav.selectedItemId = itemId
        } else {
            loadFragmentForTab(itemId)
        }
    }

    fun updateCartBadge() {
        val totalItems = dbHelper.getCartItems().sumOf { it.quantity }
        val badge = bottomNav.getOrCreateBadge(R.id.nav_cart)
        if (totalItems > 0) {
            badge.isVisible = true
            badge.number = totalItems
            badge.backgroundColor = ContextCompat.getColor(this, R.color.brand_blue)
            badge.badgeTextColor = ContextCompat.getColor(this, R.color.white)
        } else {
            badge.isVisible = false
        }
    }

    private fun loadFragmentForTab(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_catalog -> CatalogFragment()
            R.id.nav_wishlist -> WishlistFragment()
            R.id.nav_cart -> CartFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}