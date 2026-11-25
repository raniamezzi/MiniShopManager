package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class OnboardingActivity : AppCompatActivity() {

    private data class Slide(
        val iconRes: Int,
        val badge: String,
        val title: String,
        val description: String
    )

    private val slides = listOf(
        Slide(
            iconRes = R.drawable.ic_app_logo,
            badge = "CURATED TECH",
            title = "Discover Modern Tech Essentials",
            description = "Browse flagship smartphones, studio headphones, and smart accessories with transparent DT pricing."
        ),
        Slide(
            iconRes = R.drawable.ic_sparkle,
            badge = "SMART SHOPPING",
            title = "Built-In AI Shopping Assistant",
            description = "Tap Ask AI anytime to compare specs, find the best value under your budget, or get instant gift picks."
        ),
        Slide(
            iconRes = R.drawable.ic_cart,
            badge = "FAST CHECKOUT",
            title = "Save Favorites & Track Orders",
            description = "Build your wishlist, manage quantities effortlessly, and review every order right from your profile."
        )
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val prefs = PrefsHelper(this)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)
        val ivIcon = findViewById<ImageView>(R.id.ivOnboardingIcon)
        val tvBadge = findViewById<TextView>(R.id.tvOnboardingBadge)
        val tvTitle = findViewById<TextView>(R.id.tvOnboardingTitle)
        val tvDesc = findViewById<TextView>(R.id.tvOnboardingDesc)
        val btnNext = findViewById<AppCompatButton>(R.id.btnOnboardingNext)
        val dots = listOf<View>(
            findViewById(R.id.dot0),
            findViewById(R.id.dot1),
            findViewById(R.id.dot2)
        )

        fun renderSlide(index: Int) {
            val slide = slides[index]
            ivIcon.setImageResource(slide.iconRes)
            tvBadge.text = slide.badge
            tvTitle.text = slide.title
            tvDesc.text = slide.description
            btnNext.text = if (index == slides.lastIndex) "Get Started" else "Next"

            val density = resources.displayMetrics.density
            dots.forEachIndexed { i, dot ->
                val params = dot.layoutParams
                if (i == index) {
                    params.width = (24 * density).toInt()
                    dot.setBackgroundResource(R.drawable.bg_pill_blue)
                } else {
                    params.width = (8 * density).toInt()
                    dot.setBackgroundResource(R.drawable.bg_pill_tint)
                }
                dot.layoutParams = params
            }
        }

        fun finishOnboarding() {
            prefs.setOnboardingCompleted(true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        renderSlide(0)

        tvSkip.setOnClickListener { finishOnboarding() }

        btnNext.setOnClickListener {
            if (currentIndex < slides.lastIndex) {
                currentIndex++
                renderSlide(currentIndex)
            } else {
                finishOnboarding()
            }
        }
    }
}