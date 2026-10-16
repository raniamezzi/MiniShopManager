package com.example.minishopmanager

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val listView = findViewById<ListView>(R.id.listViewProducts)
        val productList = lireProduitsDepuisJSON()

        val adapter = ProductAdapter(this, productList)
        listView.adapter = adapter
    }

    private fun lireProduitsDepuisJSON(): List<Product> {
        val list = mutableListOf<Product>()
        val inputStream = assets.open("produits.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("produits")

        val images = listOf(
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_lock_silent_mode_off,
            android.R.drawable.ic_menu_recent_history,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_btn_speak_now
        )

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getString("nom")
            val img = if (i < images.size) images[i] else android.R.drawable.ic_menu_agenda
            list.add(Product(name, img))
        }

        return list
    }
}