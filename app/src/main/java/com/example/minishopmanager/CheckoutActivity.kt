package com.example.minishopmanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CheckoutActivity : AppCompatActivity() {

    companion object {
        // Taux de TVA appliqué aux prix du catalogue (considérés hors taxes)
        private const val TVA_RATE = 0.19
    }

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        dbHelper = DBHelper(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCart)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvTva = findViewById<TextView>(R.id.tvTva)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val rgPayment = findViewById<RadioGroup>(R.id.rgPayment)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        // Lecture du panier SQLite (liste vide en cas de problème, pour éviter tout crash)
        val items = try {
            dbHelper.getPanier()
        } catch (e: Exception) {
            Log.e("CheckoutActivity", "Erreur de lecture du panier", e)
            Toast.makeText(this, "Impossible de lire le panier", Toast.LENGTH_SHORT).show()
            emptyList()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CartAdapter(items)

        // Calcul des totaux
        val sousTotal = items.sumOf { it.total }
        val tva = sousTotal * TVA_RATE
        val totalTtc = sousTotal + tva

        tvSubtotal.text = formatTnd(sousTotal)
        tvTva.text = formatTnd(tva)
        tvTotal.text = formatTnd(totalTtc)

        // Panier vide : message + bouton désactivé
        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            btnConfirm.isEnabled = false
            btnConfirm.alpha = 0.5f
        }

        btnConfirm.setOnClickListener {
            val modePaiement = when (rgPayment.checkedRadioButtonId) {
                R.id.rbCash -> "paiement à la livraison"
                else -> "carte bancaire"
            }

            try {
                dbHelper.viderPanier()
                Toast.makeText(
                    this,
                    "Commande confirmée (${modePaiement}) : ${formatTnd(totalTtc)}. Merci pour votre achat !",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                Log.e("CheckoutActivity", "Erreur lors de la validation de la commande", e)
                Toast.makeText(this, "Erreur lors de la validation de la commande", Toast.LENGTH_SHORT).show()
            }
        }
    }
}