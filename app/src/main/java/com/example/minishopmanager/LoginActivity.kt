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
            val username = etUsername.text.toString()
            val editor = prefs.edit()

            // Sauvegarde le nom pour la session actuelle (affiché sur la MainActivity)
            editor.putString("CURRENT_USER", username)

            // Gère l'option "Se souvenir de moi"
            if (cbRemember.isChecked) {
                editor.putString("USER", username)
                editor.putBoolean("REMEMBER", true)
            } else {
                // Si non coché, supprime les identifiants enregistrés pour le prochain démarrage
                editor.remove("USER")
                editor.remove("REMEMBER")
            }

            editor.apply()

            // Ouvrir la page d'accueil (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}