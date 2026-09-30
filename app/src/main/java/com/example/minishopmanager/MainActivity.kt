package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            Toast.makeText(this, "Bonjour Rania Mezzi !", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CatalogActivity::class.java)
            startActivity(intent)
        }
    }
}