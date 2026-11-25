package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedCategory = "All"

    private val categories = listOf("All", "Smartphones", "Audio", "Laptops", "Wearables", "Accessories")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        dbHelper = DBHelper(ctx)
        val prefs = PrefsHelper(ctx)
        allProducts = JsonHelper.loadProducts(ctx)

        val tvGreeting = view.findViewById<TextView>(R.id.tvWelcomeGreeting)
        tvGreeting.text = if (prefs.isLoggedIn) "Hello, ${prefs.userName}" else "Welcome back"

        view.findViewById<ImageButton>(R.id.btnHomeCart).setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_cart)
        }

        view.findViewById<LinearLayout>(R.id.homeSearchBar).setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_catalog)
        }

        view.findViewById<TextView>(R.id.btnBannerExplore).setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_catalog)
        }

        view.findViewById<TextView>(R.id.tvSeeAllProducts).setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_catalog)
        }

        val rvHome = view.findViewById<RecyclerView>(R.id.rvHomeProducts)
        rvHome.layoutManager = GridLayoutManager(ctx, 2)
        adapter = ProductAdapter(allProducts, dbHelper, isGridMode = true)
        rvHome.adapter = adapter

        val chipContainer = view.findViewById<LinearLayout>(R.id.homeCategoryChips)
        renderCategoryChips(chipContainer)
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            filterByCategory(selectedCategory)
        }
    }

    private fun renderCategoryChips(container: LinearLayout) {
        container.removeAllViews()
        val density = resources.displayMetrics.density

        categories.forEach { category ->
            val chip = TextView(requireContext()).apply {
                text = category
                textSize = 13f
                setPadding(
                    (16 * density).toInt(),
                    (9 * density).toInt(),
                    (16 * density).toInt(),
                    (9 * density).toInt()
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (8 * density).toInt()
                }

                if (category == selectedCategory) {
                    setBackgroundResource(R.drawable.bg_pill_blue)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    setBackgroundResource(R.drawable.bg_pill_white)
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }

                setOnClickListener {
                    selectedCategory = category
                    renderCategoryChips(container)
                    filterByCategory(category)
                }
            }
            container.addView(chip)
        }
    }

    private fun filterByCategory(category: String) {
        val filtered = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category.equals(category, ignoreCase = true) }
        }
        adapter.updateProducts(filtered)
    }
}