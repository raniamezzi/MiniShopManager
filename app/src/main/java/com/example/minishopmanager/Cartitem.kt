package com.example.minishopmanager

import java.util.Locale

data class CartItem(
    val produitId: Int,
    val nom: String,
    val prix: Double,
    val quantite: Int
) {
    val total: Double get() = prix * quantite
}


fun formatTnd(montant: Double): String =
    String.format(Locale.FRANCE, "%,.2f TND", montant)