package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileFragment : Fragment() {

    private lateinit var prefs: PrefsHelper
    private lateinit var dbHelper: DBHelper
    private lateinit var tvInitials: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnAuth: TextView
    private lateinit var tvNoOrders: TextView
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        prefs = PrefsHelper(ctx)
        dbHelper = DBHelper(ctx)

        tvInitials = view.findViewById(R.id.tvAvatarInitials)
        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        btnAuth = view.findViewById(R.id.btnProfileAuthAction)
        tvNoOrders = view.findViewById(R.id.tvNoOrdersYet)
        val switchDark = view.findViewById<SwitchCompat>(R.id.switchDarkMode)
        val rvOrders = view.findViewById<RecyclerView>(R.id.rvProfileOrders)

        rvOrders.layoutManager = LinearLayoutManager(ctx)
        orderAdapter = OrderAdapter(emptyList())
        rvOrders.adapter = orderAdapter

        switchDark.isChecked = prefs.isDarkMode()
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnAuth.setOnClickListener {
            if (prefs.isLoggedIn) {
                prefs.logout()
                refreshProfile()
            } else {
                startActivity(Intent(ctx, LoginActivity::class.java))
            }
        }

        refreshProfile()
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            refreshProfile()
        }
    }

    private fun refreshProfile() {
        if (prefs.isLoggedIn) {
            val name = prefs.userName
            tvName.text = name
            tvEmail.text = prefs.userEmail
            tvInitials.text = name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "U" }
            btnAuth.text = "Log Out"
            btnAuth.setBackgroundResource(R.drawable.bg_pill_danger)
            btnAuth.setTextColor(requireContext().getColor(R.color.status_danger))

            val orders = dbHelper.getOrders()
            if (orders.isEmpty()) {
                tvNoOrders.text = "No orders placed yet"
                tvNoOrders.visibility = View.VISIBLE
                orderAdapter.updateOrders(emptyList())
            } else {
                tvNoOrders.visibility = View.GONE
                orderAdapter.updateOrders(orders)
            }
        } else {
            tvName.text = "Guest User"
            tvEmail.text = "Sign in to personalize your store"
            tvInitials.text = "GU"
            btnAuth.text = "Sign In"
            btnAuth.setBackgroundResource(R.drawable.bg_pill_blue)
            btnAuth.setTextColor(requireContext().getColor(R.color.white))

            tvNoOrders.text = "Sign in to view your order history"
            tvNoOrders.visibility = View.VISIBLE
            orderAdapter.updateOrders(emptyList())
        }
    }
}