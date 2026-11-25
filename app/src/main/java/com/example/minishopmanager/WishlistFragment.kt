package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WishlistFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: ProductAdapter
    private lateinit var rvWishlist: RecyclerView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var tvSubtitle: TextView
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        dbHelper = DBHelper(ctx)
        allProducts = JsonHelper.loadProducts(ctx)

        rvWishlist = view.findViewById(R.id.rvWishlist)
        emptyLayout = view.findViewById(R.id.layoutEmptyWishlist)
        tvSubtitle = view.findViewById(R.id.tvWishlistSubtitle)

        rvWishlist.layoutManager = GridLayoutManager(ctx, 2)
        adapter = ProductAdapter(emptyList(), dbHelper, isGridMode = true) {
            refreshWishlist()
        }
        rvWishlist.adapter = adapter

        view.findViewById<View>(R.id.btnExploreFromWishlist)?.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_catalog)
        }

        refreshWishlist()
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            refreshWishlist()
        }
    }

    private fun refreshWishlist() {
        val favIds = dbHelper.getFavoriteIds()
        val favProducts = allProducts.filter { favIds.contains(it.id) }

        tvSubtitle.text = "${favProducts.size} saved ${if (favProducts.size == 1) "item" else "items"}"

        if (favProducts.isEmpty()) {
            rvWishlist.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            rvWishlist.visibility = View.VISIBLE
            adapter.updateProducts(favProducts)
        }
    }
}