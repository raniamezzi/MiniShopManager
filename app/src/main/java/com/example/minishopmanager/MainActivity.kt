package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Récupération du bouton par son ID
        val btnNext = findViewById<Button>(R.id.btnNext)

        // Action au clic sur le bouton
        btnNext.setOnClickListener {
            // 1. Affichage du Toast
            Toast.makeText(this, "Bonjour Rania Mezzi !", Toast.LENGTH_SHORT).show()

            // 2. Navigation vers ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Log du cycle de vie
        Log.d("LIFECYCLE", "onCreate appelé")
    }
}