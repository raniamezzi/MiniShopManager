package com.example.minishopmanager

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val listView = findViewById<ListView>(R.id.listViewProducts)

        val productList = listOf(
            Product("Téléphone", android.R.drawable.ic_menu_call),
            Product("Casque Bluetooth", android.R.drawable.ic_lock_silent_mode_off),
            Product("Montre connectée", android.R.drawable.ic_menu_recent_history),
            Product("Chargeur USB", android.R.drawable.ic_menu_compass),
            Product("PC", android.R.drawable.ic_menu_manage),
            Product("Haut-parleur", android.R.drawable.ic_btn_speak_now)
        )

        val adapter = ProductAdapter(this, productList)
        listView.adapter = adapter
    }
}