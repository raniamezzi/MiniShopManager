package com.example.minishopmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = PrefsHelper(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val centerGroup = findViewById<LinearLayout>(R.id.splashCenterGroup)
        centerGroup.alpha = 0f
        centerGroup.translationY = 24f
        centerGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(650)
            .setInterpolator(DecelerateInterpolator())
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            val nextIntent = if (prefs.isOnboardingCompleted()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(nextIntent)
            finish()
        }, 1600)
    }
}