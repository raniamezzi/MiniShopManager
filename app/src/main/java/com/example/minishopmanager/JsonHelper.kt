package com.example.minishopmanager

import android.content.Context
import org.json.JSONArray

object JsonHelper {

    fun loadProducts(context: Context): List<Product> {
        val list = mutableListOf<Product>()
        try {
            val jsonStr = context.assets.open("produits.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Product(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        price = obj.getDouble("price"),
                        image = obj.optString("image", ""),
                        description = obj.optString("description", ""),
                        storage = obj.optString("storage", ""),
                        battery = obj.optString("battery", ""),
                        weight = obj.optString("weight", ""),
                        stock = obj.optInt("stock", 10)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getDrawableId(context: Context, imageName: String): Int {
        val cleanName = imageName.substringBeforeLast(".").trim()
        if (cleanName.isEmpty()) return 0
        return context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }
}