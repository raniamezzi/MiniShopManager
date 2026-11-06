package com.example.minishopmanager

data class Product(
    val id: Int = 0,
    val name: String = "Produit inconnu",
    val brand: String = "Marque inconnue",
    val price: Double = 0.0,
    val category: String = "Divers",
    val description: String = "Aucune description disponible.",
    val imageResId: Int // Référence de l'image
)