package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.api.Meal
import com.example.recipebook.repository.MealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MealRepository()

    fun searchMeals(query: String, callback: (List<Meal>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val meals = repository.searchMeals(query)
            callback(meals)
        }
    }

    fun getRandomMeals(count: Int = 10, callback: (List<Meal>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val meals = repository.getRandomMeals(count)
            callback(meals)
        }
    }

    fun getMealById(id: String, callback: (Meal?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val meal = repository.getMealById(id)
            callback(meal)
        }
    }
}