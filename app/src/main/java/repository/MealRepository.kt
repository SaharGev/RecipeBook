package com.example.recipebook.repository

import com.example.recipebook.api.Meal
import com.example.recipebook.api.MealApi

class MealRepository {

    suspend fun searchMeals(query: String): List<Meal> {
        return try {
            MealApi.service.searchMeals(query).meals ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRandomMeals(count: Int = 10): List<Meal> {
        return try {
            val results = mutableListOf<Meal>()
            repeat(count) {
                val meal = MealApi.service.getRandomMeal().meals?.firstOrNull()
                if (meal != null) results.add(meal)
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMealById(id: String): Meal? {
        return try {
            MealApi.service.getMealById(id).meals?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}