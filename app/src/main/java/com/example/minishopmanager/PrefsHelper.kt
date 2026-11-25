package com.example.minishopmanager

import android.content.Context

class PrefsHelper(context: Context) {
    private val prefs = context.getSharedPreferences("mini_shop_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "Guest User") ?: "Guest User"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "guest@minishop.tn") ?: "guest@minishop.tn"
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userPassword: String
        get() = prefs.getString("user_password", "") ?: ""
        set(value) = prefs.edit().putString("user_password", value).apply()

    fun hasRegisteredAccount(): Boolean =
        prefs.contains("user_email") && prefs.contains("user_password")

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_done", false)

    fun setOnboardingCompleted(done: Boolean) {
        prefs.edit().putBoolean("onboarding_done", done).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    // Only ends the active session; keeps user_name, user_email, and user_password saved
    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
    }
}