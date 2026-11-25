package com.example.minishopmanager

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val image: String,
    val description: String,
    val storage: String = "",
    val battery: String = "",
    val weight: String = "",
    val stock: Int = 10
)