package com.example.minishopmanager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatalogFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: ProductAdapter
    private lateinit var rvCatalog: RecyclerView
    private lateinit var tvResultsCount: TextView
    private lateinit var tvSortLabel: TextView
    private lateinit var btnToggleViewMode: ImageButton

    private var allProducts: List<Product> = emptyList()
    private var selectedCategory = "All"
    private var searchQuery = ""
    private var sortOrder = 0 // 0 = Default, 1 = Low to High, 2 = High to Low
    private var isGrid = true

    private val categories = listOf("All", "Smartphones", "Audio", "Laptops", "Wearables", "Accessories")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        dbHelper = DBHelper(ctx)
        allProducts = JsonHelper.loadProducts(ctx)

        rvCatalog = view.findViewById(R.id.rvCatalogProducts)
        tvResultsCount = view.findViewById(R.id.tvResultsCount)
        tvSortLabel = view.findViewById(R.id.tvSortLabel)
        btnToggleViewMode = view.findViewById(R.id.btnToggleViewMode)
        val etSearch = view.findViewById<EditText>(R.id.etCatalogSearch)
        val btnSort = view.findViewById<LinearLayout>(R.id.btnSortPrice)
        val chipContainer = view.findViewById<LinearLayout>(R.id.catalogCategoryChips)

        rvCatalog.layoutManager = GridLayoutManager(ctx, 2)
        adapter = ProductAdapter(allProducts, dbHelper, isGridMode = true)
        rvCatalog.adapter = adapter

        renderCategoryChips(chipContainer)
        applyFilters()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSort.setOnClickListener {
            sortOrder = (sortOrder + 1) % 3
            tvSortLabel.text = when (sortOrder) {
                1 -> "Price: Low → High"
                2 -> "Price: High → Low"
                else -> "Sort: Default"
            }
            applyFilters()
        }

        btnToggleViewMode.setOnClickListener {
            isGrid = !isGrid
            rvCatalog.layoutManager = if (isGrid) {
                GridLayoutManager(ctx, 2)
            } else {
                LinearLayoutManager(ctx)
            }
            btnToggleViewMode.setImageResource(
                if (isGrid) R.drawable.ic_list_view else R.drawable.ic_grid_view
            )
            adapter.setGridMode(isGrid)
            rvCatalog.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            applyFilters()
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
                    (8 * density).toInt(),
                    (16 * density).toInt(),
                    (8 * density).toInt()
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
                    applyFilters()
                }
            }
            container.addView(chip)
        }
    }

    private fun applyFilters() {
        var filtered = allProducts

        if (selectedCategory != "All") {
            filtered = filtered.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        filtered = when (sortOrder) {
            1 -> filtered.sortedBy { it.price }
            2 -> filtered.sortedByDescending { it.price }
            else -> filtered
        }

        tvResultsCount.text = "${filtered.size} product${if (filtered.size == 1) "" else "s"}"
        adapter.updateProducts(filtered)
    }
}