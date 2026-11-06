package com.example.minishopmanager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProducts)
        val productList = lireProduitsDepuisJSON()

        if (productList.isEmpty()) {
            Toast.makeText(this, "Aucun produit disponible", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductAdapter(this, productList)
    }

    private fun lireProduitsDepuisJSON(): List<Product> {
        val list = mutableListOf<Product>()

        val images = listOf(
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_lock_silent_mode_off,
            android.R.drawable.ic_menu_recent_history,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_btn_speak_now
        )

        try {
            val jsonString = assets.open("produits.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONObject(jsonString).optJSONArray("produits")
                ?: return emptyList()

            for (i in 0 until jsonArray.length()) {
                // Un produit mal formé est ignoré sans bloquer les autres
                val item = jsonArray.optJSONObject(i) ?: continue

                val img = if (i < images.size) images[i] else android.R.drawable.ic_menu_agenda

                list.add(
                    Product(
                        id = item.optInt("id", i + 1),
                        name = item.optString("nom", "Produit inconnu").ifBlank { "Produit inconnu" },
                        brand = item.optString("marque", "Marque inconnue").ifBlank { "Marque inconnue" },
                        price = item.optDouble("prix", 0.0).let { if (it.isNaN()) 0.0 else it },
                        category = item.optString("categorie", "Divers").ifBlank { "Divers" },
                        description = item.optString("description", "Aucune description disponible.")
                            .ifBlank { "Aucune description disponible." },
                        imageResId = img
                    )
                )
            }
        } catch (e: Exception) {
            // Fichier absent, illisible ou JSON invalide : on évite le crash
            Log.e("CatalogActivity", "Erreur de lecture de produits.json", e)
            Toast.makeText(this, "Erreur de chargement du catalogue", Toast.LENGTH_SHORT).show()
        }

        return list
    }
}