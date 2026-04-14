package com.example.recipebook.utils

import android.content.Context

object RecentItemsHelper {

    fun saveRecentRecipe(context: Context, recipeId: Int, uid: String) {
        val prefs = context.getSharedPreferences("recent_recipes_$uid", Context.MODE_PRIVATE)
        val currentIds = prefs.getString("ids", "") ?: ""
        val idsList = currentIds
            .split(",")
            .filter { it.isNotBlank() && it != recipeId.toString() }
            .toMutableList()
        idsList.add(0, recipeId.toString())
        prefs.edit().putString("ids", idsList.take(10).joinToString(",")).apply()
    }

    fun saveRecentBook(context: Context, bookId: Int, uid: String) {
        val prefs = context.getSharedPreferences("recent_books_$uid", Context.MODE_PRIVATE)
        val currentIds = prefs.getString("ids", "") ?: ""
        val idsList = currentIds
            .split(",")
            .filter { it.isNotBlank() && it != bookId.toString() }
            .toMutableList()
        idsList.add(0, bookId.toString())
        prefs.edit().putString("ids", idsList.take(10).joinToString(",")).apply()
    }

    fun getRecentRecipeIds(context: Context, uid: String): List<Int> {
        val prefs = context.getSharedPreferences("recent_recipes_$uid", Context.MODE_PRIVATE)
        val idsString = prefs.getString("ids", "") ?: ""
        return idsString.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun getRecentBookIds(context: Context, uid: String): List<Int> {
        val prefs = context.getSharedPreferences("recent_books_$uid", Context.MODE_PRIVATE)
        val idsString = prefs.getString("ids", "") ?: ""
        return idsString.split(",").mapNotNull { it.toIntOrNull() }
    }
}