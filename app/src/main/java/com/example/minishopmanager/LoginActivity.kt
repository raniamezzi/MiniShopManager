package com.example.minishopmanager

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        prefs = getSharedPreferences("ShopPrefs", MODE_PRIVATE)

        // Charger l'utilisateur si "Se souvenir de moi" était coché
        etUsername.setText(prefs.getString("USER", ""))
        cbRemember.isChecked = prefs.getBoolean("REMEMBER", false)

        btnLogin.setOnClickListener {
            val editor = prefs.edit()
            if (cbRemember.isChecked) {
                editor.putString("USER", etUsername.text.toString())
                editor.putBoolean("REMEMBER", true)
            } else {
                editor.clear()
            }
            editor.apply()

            // Ouvrir la page d'accueil ou le catalogue
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}