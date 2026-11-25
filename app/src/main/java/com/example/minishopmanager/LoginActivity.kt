package com.example.minishopmanager

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = PrefsHelper(this)
        val btnBack = findViewById<ImageButton>(R.id.btnLoginBack)
        val etName = findViewById<EditText>(R.id.etLoginName)
        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSubmitLogin)

        // Pre-fill saved account details if the user registered earlier
        if (prefs.hasRegisteredAccount()) {
            etName.setText(prefs.userName)
            etEmail.setText(prefs.userEmail)
        }

        btnBack.setOnClickListener { finish() }

        btnSignIn.setOnClickListener {
            val enteredName = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || !email.contains("@")) {
                etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }
            if (password.length < 4) {
                etPassword.error = "Password must be at least 4 characters"
                return@setOnClickListener
            }

            // If an account with this email already exists, verify the password
            if (prefs.hasRegisteredAccount() && prefs.userEmail.equals(email, ignoreCase = true)) {
                if (prefs.userPassword != password) {
                    etPassword.error = "Incorrect password for this account"
                    return@setOnClickListener
                }
                if (enteredName.isNotEmpty()) {
                    prefs.userName = enteredName
                }
                prefs.isLoggedIn = true
                Toast.makeText(this, "Welcome back, ${prefs.userName}!", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            // Otherwise register & save the new account locally
            val finalName = enteredName.ifEmpty { "Shopper" }
            prefs.userName = finalName
            prefs.userEmail = email
            prefs.userPassword = password
            prefs.isLoggedIn = true

            Toast.makeText(this, "Account saved! Welcome, $finalName!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}