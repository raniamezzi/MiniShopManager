package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val btnCatalog = findViewById<Button>(R.id.btnCatalog)
        val btnCart = findViewById<Button>(R.id.btnCart)

        // Nom d'utilisateur depuis SharedPreferences (avec valeur de repli)
        val prefs = getSharedPreferences("ShopPrefs", MODE_PRIVATE)
        val nom = (prefs.getString("CURRENT_USER", null)
            ?: prefs.getString("USER", null))
            ?.trim()
            .orEmpty()

        tvGreeting.text = if (nom.isNotEmpty()) "Bonjour $nom" else "Bonjour"

        btnCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogActivity::class.java))
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }
}